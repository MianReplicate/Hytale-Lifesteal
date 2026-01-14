package hytale.mian.lifesteal;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import hytale.mian.lifesteal.storage.LSComponent;
import hytale.mian.lifesteal.storage.LSComponents;
import hytale.mian.lifesteal.systems.LSSystem;
import hytale.mian.lifesteal.systems.OnEntityKilled;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.logging.Logger;

public class Lifesteal extends JavaPlugin {
    public static final Logger LOGGER = Logger.getLogger("Lifesteal");
    private final Config<LSConfig> config;

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
        this.getEntityStoreRegistry().registerSystem(new LSSystem(config));
        this.getEntityStoreRegistry().registerSystem(new OnEntityKilled(config));
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> event.getPlayerRef().getStore().ensureComponent(event.getPlayerRef(), LSComponent.getComponentType()));
    }
}
