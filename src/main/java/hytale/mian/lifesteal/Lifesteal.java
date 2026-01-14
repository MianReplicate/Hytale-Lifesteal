package hytale.mian.lifesteal;

/**
 * Main plugin class.
 * 
 * TODO: Implement your plugin logic here.
 * 
 * @author YourName
 * @version 1.0.0
 */
public class Lifesteal {

    private static Lifesteal instance;
    
    /**
     * Constructor - Called when plugin is loaded.
     */
    public Lifesteal() {
        instance = this;
        System.out.println("[TemplatePlugin] Plugin loaded!");
    }
    
    /**
     * Called when plugin is enabled.
     */
    public void onEnable() {
        System.out.println("[TemplatePlugin] Plugin enabled!");
        
        // TODO: Initialize your plugin here
        // - Load configuration
        // - Register event listeners
        // - Register commands
        // - Start services
    }
    
    /**
     * Called when plugin is disabled.
     */
    public void onDisable() {
        System.out.println("[TemplatePlugin] Plugin disabled!");
        
        // TODO: Cleanup your plugin here
        // - Save data
        // - Stop services
        // - Close connections
    }
    
    /**
     * Get plugin instance.
     */
    public static Lifesteal getInstance() {
        return instance;
    }
}
