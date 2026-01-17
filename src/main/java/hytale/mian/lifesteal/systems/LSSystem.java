package hytale.mian.lifesteal.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.InfiniteBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.storage.LSComponent;
import hytale.mian.lifesteal.storage.LSComponents;
import hytale.mian.lifesteal.util.Accessor;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public class LSSystem extends EntityTickingSystem {
    private static final String KEY = "Lifesteal";

    public void applyModifier(Ref<EntityStore> ref, CommandBuffer buffer){
        ((EntityStatMap)buffer.ensureAndGetComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType()))
                .putModifier(DefaultEntityStatTypes.getHealth(), KEY, new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE,
                        ((LSComponent)buffer.ensureAndGetComponent(ref, LSComponents.get().LS_COMPONENT)).getHealthDifference()));
    }

    public void checkIfShouldBeBanned(Ref<EntityStore> ref, CommandBuffer buffer){
        PlayerRef player = (PlayerRef) buffer.getComponent(ref, PlayerRef.getComponentType());
        if(player != null){
            if(Lifesteal.config.get().banAtMin() && Lifesteal.config.get().hasMin() && ((LSComponent)buffer.getComponent(ref, LSComponent.getComponentType())).getHealthDifference() <= Lifesteal.config.get().getMinAmount()){
                HytaleBanProvider banProvider = Accessor.getPublic(AccessControlModule.get(), "banProvider");

                UUID ownerUUID = Options.getOptionSet().valueOf(Options.OWNER_UUID);
                UUID uuid = ((UUIDComponent) buffer.getComponent(ref, UUIDComponent.getComponentType())).getUuid();

                String reason = I18nModule.get().getMessage(player.getLanguage(), "server.lifesteal.banned");
                InfiniteBan ban = new InfiniteBan(uuid, ownerUUID, Instant.now(), reason);
                banProvider.modify((banMap) -> {
                    banMap.put(uuid, ban);
                    return true;
                });

                LSComponent healthComponent = (LSComponent) buffer.getComponent(ref, LSComponents.get().LS_COMPONENT);
                healthComponent.setHealthDifference(Lifesteal.config.get().getStartAmount()); // reset
                player.getPacketHandler().disconnect(reason);
            }
        }
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk chunk, Store store, CommandBuffer buffer) {
        Ref<EntityStore> entityRef = chunk.getReferenceTo(index);

        checkIfShouldBeBanned(entityRef, buffer);

        var healthComponent = (LSComponent) buffer.getComponent(entityRef, LSComponents.get().LS_COMPONENT);
        var modifier = ((EntityStatMap) buffer.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType())).getModifier(DefaultEntityStatTypes.getHealth(), KEY);

        if(modifier == null || ((StaticModifier) modifier).getAmount() != healthComponent.getHealthDifference())
            applyModifier(entityRef, buffer);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(LSComponents.get().LS_COMPONENT, Query.not(DeathComponent.getComponentType()));
    }
}
