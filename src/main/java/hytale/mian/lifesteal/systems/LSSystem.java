package hytale.mian.lifesteal.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.auth.IAuthCredentialStore;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.Ban;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.InfiniteBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.TimedBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.system.ModelSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.storage.LSComponent;
import hytale.mian.lifesteal.storage.LSComponents;
import hytale.mian.lifesteal.util.Accessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class LSSystem extends EntityTickingSystem {
    private static final String KEY = "Lifesteal";

    public void applyModifier(Ref<EntityStore> ref, CommandBuffer buffer){
        ((EntityStatMap)buffer.ensureAndGetComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()))
                .putModifier(DefaultEntityStatTypes.getHealth(), KEY, new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE,
                        ((LSComponent)buffer.ensureAndGetComponent(ref, LSComponents.get().LS_COMPONENT)).getHealthDifference()));
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk chunk, Store store, CommandBuffer buffer) {
        Ref<EntityStore> entityRef = chunk.getReferenceTo(index);

        var healthComponent = (LSComponent) buffer.ensureAndGetComponent(entityRef, LSComponents.get().LS_COMPONENT);
        var modifier = ((EntityStatMap) buffer.ensureAndGetComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType())).getModifier(DefaultEntityStatTypes.getHealth(), KEY);

        if(modifier == null || ((StaticModifier) modifier).getAmount() != healthComponent.getHealthDifference())
            applyModifier(entityRef, buffer);

        if(buffer.getComponent(entityRef, DeathComponent.getComponentType()) == null){
            healthComponent.setBanned(false);
            ((EntityStatMap)buffer.ensureAndGetComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType())).maximizeStatValue(DefaultEntityStatTypes.getHealth());
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }

    @Override
    public @NotNull Set<Dependency> getDependencies() {
        return Set.of(new SystemDependency(Order.AFTER, ReduceMaxHealth.class));
    }
}
