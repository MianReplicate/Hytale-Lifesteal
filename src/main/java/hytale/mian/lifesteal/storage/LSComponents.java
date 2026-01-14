package hytale.mian.lifesteal.storage;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.systems.LSSystem;

public class LSComponents {
    public final ComponentType<EntityStore, LSComponent> LS_COMPONENT;
    private static LSComponents instance;

    public LSComponents(Lifesteal lifesteal){
        instance = this;

        LS_COMPONENT = lifesteal.getEntityStoreRegistry().registerComponent(LSComponent.class, "Lifesteal", LSComponent.CODEC);
    }

    public static LSComponents get(){
        return instance;
    }
}