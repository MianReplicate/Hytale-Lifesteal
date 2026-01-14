import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * Custom Gradle plugin for automated Hytale server testing.
 * 
 * Usage:
 *   runHytale {
 *       jarUrl = "https://example.com/hytale-server.jar"
 *   }
 *   
 *   ./gradlew runServer
 */
open class RunHytalePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create extension for configuration
        val extension = project.extensions.create("runHytale", RunHytaleExtension::class.java)

        // Register the runServer task
        val runTask: TaskProvider<RunServerTask> = project.tasks.register(
            "runServer", 
            RunServerTask::class.java
        ) {
            jarPath.set(extension.jarPath)
            assetsPath.set(extension.assetsPath)
            group = "hytale"
            description = "Downloads and runs the Hytale server with your plugin"
        }

        // Make runServer depend on shadowJar (build plugin first)
        project.tasks.findByName("shadowJar")?.let {
            runTask.configure {
                dependsOn(it)
            }
        }
    }
}

/**
 * Extension for configuring the RunHytale plugin.
 */
open class RunHytaleExtension {
    var jarPath: String = "https://example.com/hytale-server.jar"
    var assetsPath: String = ""
}

/**
 * Task that sets up, and runs a Hytale server with the plugin.
 */
open class RunServerTask : DefaultTask() {

    @Input
    val jarPath = project.objects.property(String::class.java)

    @Input
    val assetsPath = project.objects.property(String::class.java)

    @TaskAction
    fun run() {
        // Create directories
        val runDir = File(project.projectDir, "run").apply { mkdirs() }
        val modsDir = File(runDir, "mods").apply { mkdirs() }
        val jarFile = File(runDir, "server.jar")
        val cachedJar = File(jarPath.get(), "HytaleServer.jar")

        // Copy server JAR to run directory

        cachedJar.copyTo(jarFile, overwrite = true)

        // Copy plugin JAR to plugins folder
        project.tasks.findByName("shadowJar")?.outputs?.files?.firstOrNull()?.let { shadowJar ->
            val targetFile = File(modsDir, shadowJar.name)
            shadowJar.copyTo(targetFile, overwrite = true)
            println("Plugin copied to: ${targetFile.absolutePath}")
        } ?: run {
            println("WARNING: Could not find shadowJar output")
        }

        println("Starting Hytale server...")
        println("Press Ctrl+C to stop the server")

        // Check if debug mode is enabled
        val debugMode = project.hasProperty("debug")
        val javaArgs = mutableListOf<String>()
        
        if (debugMode) {
            javaArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            println("Debug mode enabled. Connect debugger to port 5005")
        }
        
        javaArgs.addAll(listOf("-jar", jarFile.name))
        javaArgs.addAll(listOf("--assets", assetsPath.get() + "/Assets.zip"))
        javaArgs.add("--allow-op")

        // Start the server process
        val process = ProcessBuilder("java", *javaArgs.toTypedArray())
            .directory(runDir)
            .start()

        // Handle graceful shutdown
        project.gradle.buildFinished {
            if (process.isAlive) {
                println("\nStopping server...")
                process.destroy()
            }
        }

        // Forward stdout to console
        Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }
        }.start()

        // Forward stderr to console
        Thread {
            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { System.err.println(it) }
            }
        }.start()

        // Forward stdin to server (for commands)
        Thread {
            System.`in`.bufferedReader().useLines { lines ->
                lines.forEach {
                    process.outputStream.write((it + "\n").toByteArray())
                    process.outputStream.flush()
                }
            }
        }.start()

        // Wait for server to exit
        val exitCode = process.waitFor()
        println("Server exited with code $exitCode")
    }
}
