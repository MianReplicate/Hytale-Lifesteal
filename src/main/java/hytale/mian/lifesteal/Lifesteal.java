package hytale.mian.lifesteal;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import hytale.mian.lifesteal.commands.LSCommand;
import hytale.mian.lifesteal.storage.LSComponent;
import hytale.mian.lifesteal.storage.LSComponents;
import hytale.mian.lifesteal.systems.LSSystem;
import hytale.mian.lifesteal.systems.ReduceMaxHealth;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

// TODO: weapon for lifestealing, ores and items ofc for gaining max health
// TODO: reviving (item)
public class Lifesteal extends JavaPlugin {
    public static final Logger LOGGER = Logger.getLogger("Lifesteal");
    public static Config<LSConfig> config;

    public Lifesteal(@NonNullDecl JavaPluginInit init) {
        super(init);
        config = this.withConfig("Lifesteal", LSConfig.CODEC);
    }

    @Override
    protected void setup() {
        LOGGER.info("In the process of stealing your soul!");

        super.setup();
        this.config.save();
        new LSComponents(this);
        this.getEntityStoreRegistry().registerSystem(new ReduceMaxHealth());
        this.getEntityStoreRegistry().registerSystem(new LSSystem());
        this.getCommandRegistry().registerCommand(new LSCommand());

        PermissionsModule.get().addGroupPermission("OP", Set.of("command.lifesteal.has_op"));
    }
}