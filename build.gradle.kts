import groovy.json.JsonOutput

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
    id("run-hytale")
}

group = findProperty("pluginGroup") as String
version = findProperty("pluginVersion") as String
description = findProperty("pluginDescription") as String

var game_build = findProperty("gameBuild") as String
var hytalePath = System.getProperty("user.home") + "/AppData/Roaming/Hytale/install/release/package/game/${game_build}/Server/HytaleServer.jar"
var modtale_id = findProperty("modtale_id") as String
var curseforge_id = findProperty("curseforge_id") as String
var orbis_id = findProperty("orbis_id") as String
var release_channel = findProperty("release_channel") as String

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Hytale Server API (provided by server at runtime)
    compileOnly(files(hytalePath))
    
    // Common dependencies (will be bundled in JAR)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:24.1.0")
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Configure server testing
runHytale {
    // Using Paper server as placeholder for testing the runServer functionality
    jarPath = System.getProperty("user.home") + "/AppData/Roaming/Hytale/install/release/package/game/${game_build}/Server"
    assetsPath = System.getProperty("user.home") + "/AppData/Roaming/Hytale/install/release/package/game/${game_build}"
}

tasks {
    // Configure Java compilation
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }
    
    // Configure resource processing
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        // Replace placeholders in manifest.json
        val props = mapOf(
            "group" to project.group,
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)
        
        filesMatching("manifest.json") {
            expand(props)
        }
    }
    
    // Configure ShadowJar (bundle dependencies)
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        
        // Relocate dependencies to avoid conflicts
        relocate("com.google.gson", "hytale.mian.libs.gson")
        
        // Minimize JAR size (removes unused classes)
        minimize()
    }
    
    // Configure tests
    test {
        useJUnitPlatform()
    }
    
    // Make build depend on shadowJar
    build {
        dependsOn(shadowJar)
    }
}

tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes to all platforms"

    dependsOn(
        "publishToModTale",
        "publishToCurseForge"
    )
}

tasks.register<Exec>("publishToModTale") {
    group = "publishing"
    description = "Publishes the built jar to Modtale.net."

    dependsOn(tasks.named("shadowJar"))

    val curlExe = if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        "C:/Windows/System32/curl.exe"
    } else {
        "curl"
    }

    doFirst {
        val jarFile = layout.buildDirectory
            .file("libs/${project.name}-${project.version}.jar")
            .get()
            .asFile

        if (!jarFile.exists()) {
            throw GradleException("Error: Jar file not found: $jarFile")
        }

        val apiKey = project.findProperty("modtale_token") as String
        val projectId = modtale_id

        if (apiKey.isNullOrBlank() || projectId.isNullOrBlank()) {
            throw GradleException("Error: modtale_token and modtale_id var is required.")
        }


        val changelogFile = project.file("changelog.md")

        if (!changelogFile.exists()) {
            throw GradleException("changelog.md not found")
        }

        val changelogText = changelogFile
            .takeIf { it.exists() }
            ?.readText(Charsets.UTF_8)
            ?: "Release ${project.version}"

        commandLine(
            curlExe,
            "-X", "POST",
            "https://api.modtale.net/api/v1/projects/$projectId/versions",
            "-H", "X-MODTALE-KEY: $apiKey",
            "-F", "file=@${jarFile.absolutePath}",
            "-F", "versionNumber=${project.version}",
            // RELEASE | BETA | ALPHA
            "-F", "channel=$release_channel",
            // Comma-separated list (Release 1.0, Release 1.1)
            "-F", "gameVersions=1.0-SNAPSHOT",
            // Markdown string
            "-F", "changelog=$changelogText"
        )
    }
}


// doesn't work for now
tasks.register<Exec>("publishToCurseForge") {
    group = "publishing"
    description = "Publishes the built Hytale mod to CurseForge."
    dependsOn("shadowJar")

    val curlExe = if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        "C:/Windows/System32/curl.exe"
    } else {
        "curl"
    }

    doFirst {
        val apiToken = project.findProperty("curseforge_token") as String

        val jarFile = layout.buildDirectory
            .file("libs/${project.name}-${project.version}.jar")
            .get()
            .asFile

        val changelogFile = project.file("changelog.md")

        if (apiToken.isNullOrBlank() || curseforge_id.isNullOrBlank()) {
            throw GradleException("curseforge_token and curseforge_id are required.")
        }

        if (!jarFile.exists()) {
            throw GradleException("Jar file not found: $jarFile")
        }

        val changelogText = changelogFile
            .takeIf { it.exists() }
            ?.readText(Charsets.UTF_8)
            ?: "Release ${project.version}"


        val metadata = mapOf(
            "changelog" to changelogText,
            "changelogType" to "markdown",
            "releaseType" to release_channel.lowercase(),
            "gameVersions" to listOf("Early Access")
        )
        val metadataJson = JsonOutput.toJson(metadata)


        commandLine(
            curlExe,
            "-X", "POST",
            "-H", "X-Api-Token: $apiToken",

            "-F", "metadata=$metadataJson;type=application/json",
            "https://hytale.curseforge.com/api/projects/$curseforge_id/upload-file",
        )
    }
}

fun String.jsonEscape(): String =
    this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "")
        .replace("\n", "\\n")

//tasks.register<Exec>("publishToOrbis") {
//    group = "publishing"
//    description = "Publishes the built Hytale mod to Orbis."
//    dependsOn("shadowJar")
//
//    val curlExe = if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
//        "C:/Windows/System32/curl.exe"
//    } else {
//        "curl"
//    }
//
//    doFirst {
//        val apiToken = System.getenv("ORBIS_API_TOKEN")
//
//        val jarFile = layout.buildDirectory
//            .file("libs/${project.name}-${project.version}.jar")
//            .get()
//            .asFile
//
//        val changelogFile = project.file("changelog.md")
//
//        if (apiToken.isNullOrBlank() || orbis_id.isNullOrBlank()) {
//            throw GradleException("ORBIS_API_TOKEN and ORBIS_PROJECT_ID are required.")
//        }
//
//        if (!jarFile.exists()) {
//            throw GradleException("Jar file not found: $jarFile")
//        }
//
//        val changelogText = changelogFile
//            .takeIf { it.exists() }
//            ?.readText(Charsets.UTF_8)
//            ?: "Release ${project.version}"
//
//        commandLine(
//            curlExe,
//            "-X", "POST",
//            "https://minecraft.curseforge.com/api/v1/projects/$orbis_id/upload-file",
//            "-H", "X-Api-Token: $apiToken",
//
//            // File upload
//            "-F", "file=@${jarFile.absolutePath}",
//
//            // Metadata
//            "-F", "changelog=$changelogText",
//            "-F", "changelogType=markdown",
//            "-F", "releaseType=${release_channel}",
//
//            // Hytale game version ID(s)
//            "-F", "gameVersions=Early Access"
//        )
//    }
//}

// Configure Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
