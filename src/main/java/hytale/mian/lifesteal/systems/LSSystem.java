package hytale.mian.lifesteal.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import hytale.mian.lifesteal.LSConfig;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.storage.LSComponent;
import hytale.mian.lifesteal.storage.LSComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LSSystem extends EntityTickingSystem {
    private final Config<LSConfig> config;
    private static final String KEY = "Lifesteal";

    public LSSystem(Config<LSConfig> config) {
        this.config = config;
    }

    public void applyModifier(Ref<EntityStore> ref, CommandBuffer buffer){
        ((EntityStatMap)buffer.ensureAndGetComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()))
                .putModifier(DefaultEntityStatTypes.getHealth(), KEY, new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE,
                        ((LSComponent)buffer.ensureAndGetComponent(ref, LSComponents.get().LS_COMPONENT)).getHealthDifference()));
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk chunk, Store store, CommandBuffer buffer) {
        Ref<EntityStore> entityRef = chunk.getReferenceTo(index);

        var healthComponent = (LSComponent) buffer.getComponent(entityRef, LSComponents.get().LS_COMPONENT);
        var modifier = ((EntityStatMap) buffer.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType())).getModifier(DefaultEntityStatTypes.getHealth(), KEY);

        if(modifier == null || ((StaticModifier) modifier).getAmount() != healthComponent.getHealthDifference())
            applyModifier(entityRef, buffer);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType(), LSComponents.get().LS_COMPONENT);
    }
}
