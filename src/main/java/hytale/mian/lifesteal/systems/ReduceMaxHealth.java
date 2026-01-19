package hytale.mian.lifesteal.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.IAuthCredentialStore;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.Ban;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.InfiniteBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.TimedBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.storage.LSComponent;
import hytale.mian.lifesteal.storage.LSComponents;
import hytale.mian.lifesteal.util.Accessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

// Is added when a player dies
public class ReduceMaxHealth extends DeathSystems.OnDeathSystem {
    public void checkIfShouldBeBanned(Ref<EntityStore> ref, CommandBuffer buffer){
        PlayerRef player = (PlayerRef) buffer.getComponent(ref, PlayerRef.getComponentType());
        if(player != null){
            LSComponent healthComponent = (LSComponent) buffer.ensureAndGetComponent(ref, LSComponents.get().LS_COMPONENT);
            UUID uuid = ((UUIDComponent)buffer.getComponent(ref, UUIDComponent.getComponentType())).getUuid();

            float currentHp = healthComponent.getHealthDifference();
            float minimum = Lifesteal.config.get().getMinAmount();
            if(Lifesteal.config.get().banAtMin() && currentHp <= minimum){
                healthComponent.setHealthDifference(Lifesteal.config.get().getStartAmount()); // reset
                healthComponent.setBanned(true);
                healthComponent.setLostHealth(false);

                HytaleBanProvider banProvider = Accessor.getPublic(AccessControlModule.get(), "banProvider");

                AtomicReference credentialStore = Accessor.getPublic(ServerAuthManager.getInstance(), "credentialStore");
                IAuthCredentialStore iAuthCredentialStore = (IAuthCredentialStore) credentialStore.get();
                UUID ownerUUID = iAuthCredentialStore.getProfile();

                String reason = I18nModule.get().getMessage(player.getLanguage(), "server.lifesteal.banned");

                Ban ban = Lifesteal.config.get().hasAutoRevive() ?
                        new TimedBan(uuid, ownerUUID, Instant.now(), Instant.now().plusSeconds(Lifesteal.config.get().getAutoRevive()), reason) :
                        new InfiniteBan(uuid, ownerUUID, Instant.now(), reason);
                banProvider.modify((banMap) -> {
                    banMap.put(uuid, ban);
                    return true;
                });

                CompletableFuture.runAsync(() -> player.getPacketHandler().disconnect(reason));
            }
        }
    }

    public Query getQuery() {
        return Query.any();
    }

    public void onComponentAdded(@Nonnull Ref ref, @Nonnull DeathComponent component, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        Damage deathInfo = component.getDeathInfo();
        if (deathInfo != null) {
            Damage.Source source = deathInfo.getSource();
            if (source instanceof Damage.EntitySource) {
                Damage.EntitySource entitySource = (Damage.EntitySource) source;
                Ref<EntityStore> sourceRef = entitySource.getRef();
                if (!sourceRef.isValid()) {
                    return;
                }

                Player attacker = (Player) store.getComponent(sourceRef, Player.getComponentType());
                Player victim = (Player) store.getComponent(ref, Player.getComponentType());

                float stealAmount = Lifesteal.config.get().getStealAmount();
                boolean canEarnLifeFromNonPlayers = Lifesteal.config.get().canEarnLifeFromNonPlayers();

                float amountEarned;
                if (victim != null) {
                    EntityStatValue health = victim.toHolder().ensureAndGetComponent(EntityStatsModule.get().getEntityStatMapComponentType()).get(DefaultEntityStatTypes.getHealth());
                    float allowableDifference = health.getMax() - health.getMin();
                    amountEarned = Lifesteal.config.get().canEarnLifeRegardlessIfEntityDoesntHaveEnough() ? stealAmount : Math.min(stealAmount, allowableDifference);
                } else {
                    amountEarned = stealAmount;
                }

                if (attacker != null && (victim != null || canEarnLifeFromNonPlayers)) {
                    EntityStatValue health = attacker.toHolder().ensureAndGetComponent(EntityStatsModule.get().getEntityStatMapComponentType()).get(DefaultEntityStatTypes.getHealth());
                    float max = health.getMax();

                    boolean earnedAnything = attacker.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(amountEarned);

                    if (amountEarned == 0 || !earnedAnything) {
                        EventTitleUtil.showEventTitleToPlayer(
                                ((PlayerRef) store.getComponent(sourceRef, PlayerRef.getComponentType())),
                                Message.translation("server.lifesteal.nothing_to_gain").color(Color.red),
                                Message.translation("server.lifesteal.nothing_to_gain_rip").color(Color.red),
                                true
                        );
                    } else {
                        EventTitleUtil.showEventTitleToPlayer(
                                ((PlayerRef) store.getComponent(sourceRef, PlayerRef.getComponentType())),
                                Message.translation("server.lifesteal.earned_max_health").param("number", amountEarned).color(Color.GREEN),
                                Message.translation("server.lifesteal.new_max_health").param("old_max", max).param("new_max", max + amountEarned).color(Color.PINK),
                                true
                        );
                    }
                }

                if (victim != null){
                    if(attacker != null && !Lifesteal.config.get().canLoseHealthOnPlayerDeath())
                        return;
                    if(attacker == null && !Lifesteal.config.get().canLoseHealthOnEntityDeath())
                        return;
                    LSComponent healthComp = victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType());
                    healthComp.setLostHealth(true);
                }
            } else {
                Player victim = (Player) store.getComponent(ref, Player.getComponentType());
                if(victim != null){
                    if(!Lifesteal.config.get().canLoseHealthOnEnvDeath())
                        return;
                    LSComponent healthComp = victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType());
                    healthComp.setLostHealth(true);
                }
            }
        }
    }

    @Override
    public void onComponentRemoved(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent component, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        Player victim = (Player) store.getComponent(ref, Player.getComponentType());
        if(victim != null){
            LSComponent healthComp = victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType());
            if(healthComp.hasLostHealth() && !healthComp.isBanned()){
                Lifesteal.LOGGER.info("lost heealth whoopsies");
                healthComp.setLostHealth(false);

                EntityStatValue health = victim.toHolder().ensureAndGetComponent(EntityStatsModule.get().getEntityStatMapComponentType()).get(DefaultEntityStatTypes.getHealth());
                float max = health.getMax();

                float amount = Lifesteal.config.get().getStealAmount();
                EventTitleUtil.showEventTitleToPlayer(
                        ((PlayerRef) store.getComponent(ref, PlayerRef.getComponentType())),
                        Message.translation("server.lifesteal.lost_max_health").param("number", amount).color(Color.RED),
                        Message.translation("server.lifesteal.new_max_health").param("old_max", max).param("new_max", max - amount).color(Color.PINK),
                        true
                );
                healthComp.addHealthDifference(-amount);
                checkIfShouldBeBanned(ref, commandBuffer);
            }
        }
    }
}