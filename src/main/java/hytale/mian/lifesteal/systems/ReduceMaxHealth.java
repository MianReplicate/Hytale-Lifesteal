package hytale.mian.lifesteal.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.storage.LSComponent;

import javax.annotation.Nonnull;
import java.awt.*;

// Is added when a player dies
public class ReduceMaxHealth extends DeathSystems.OnDeathSystem {
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

                    attacker.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(amountEarned);

                    if (amountEarned == 0) {
                        NotificationUtil.sendNotification(
                                ((PlayerRef) store.getComponent(sourceRef, PlayerRef.getComponentType())).getPacketHandler(),
                                Message.translation("server.lifesteal.nothing_to_gain").color(Color.red)
                                // put some icon here for heart
                        );
                    } else {
                        NotificationUtil.sendNotification(
                                ((PlayerRef) store.getComponent(sourceRef, PlayerRef.getComponentType())).getPacketHandler(),
                                Message.translation("server.lifesteal.earned_max_health").param("number", amountEarned).color(Color.GREEN),
                                Message.translation("server.lifesteal.new_max_health").param("old_max", max).param("new_max", max + amountEarned).color(Color.PINK)
                                // put some icon here for heart
                        );
                    }
                }

                if (victim != null){
                    if(attacker != null && !Lifesteal.config.get().canLoseHealthOnPlayerDeath())
                        return;
                    if(attacker == null && !Lifesteal.config.get().canLoseHealthOnEntityDeath())
                        return;
                    victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(-stealAmount);
                }
            } else {
                Player victim = (Player) store.getComponent(ref, Player.getComponentType());
                if(victim != null){
                    if(!Lifesteal.config.get().canLoseHealthOnEnvDeath())
                        return;
                    victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(-Lifesteal.config.get().getStealAmount());
                }
            }
        }
    }

//    @Override
//    public void onComponentRemoved(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent component, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
//        Damage deathInfo = component.getDeathInfo();
//        DamageCause deathCause = component.getDeathCause();
//        if (deathCause == DamageCause.PHYSICAL || deathCause == DamageCause.PROJECTILE) {
//            if (deathInfo != null) {
//                Damage.Source var9 = deathInfo.getSource();
//                if (var9 instanceof Damage.EntitySource) {
//                    Damage.EntitySource entitySource = (Damage.EntitySource)var9;
//                    Ref<EntityStore> sourceRef = entitySource.getRef();
//                    if (!sourceRef.isValid()) {
//                        return;
//                    }
//
//                    Player attacker = (Player)store.getComponent(sourceRef, Player.getComponentType());
//                    Player victim = (Player)store.getComponent(ref, Player.getComponentType());
//
//                    float stealAmount = Lifesteal.config.get().getStealAmount();
//                    boolean canEarnLifeFromNonPlayers = Lifesteal.config.get().canEarnLifeFromNonPlayers();
//
//                    LSComponent lsComponent = victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType());
//                    float oldAmount = lsComponent.getHealthDifference();
//
//                    if(victim != null && attacker != null)
//                        victim.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(-stealAmount);
//
//                    EntityStatValue health = victim.toHolder().ensureAndGetComponent(EntityStatsModule.get().getEntityStatMapComponentType()).get(DefaultEntityStatTypes.getHealth());
//                    float max = health.getMax();
//                    float newAmount = max <= health.getMin() ? health.getMin() : lsComponent.getHealthDifference();
//                    boolean onlySignChange = Math.abs(newAmount) == oldAmount || Math.abs(oldAmount) == newAmount;
//                    float amountEarned = onlySignChange ? Math.abs(newAmount) * 2 : Math.abs(newAmount - oldAmount);
//
//                    if(attacker != null && (victim != null || canEarnLifeFromNonPlayers)){
//                        attacker.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(amountEarned);
//
//                        NotificationUtil.sendNotification(
//                                ((PlayerRef)store.getComponent(sourceRef, PlayerRef.getComponentType())).getPacketHandler(),
//                                Message.translation("lifesteal.earned_max_health").color(Color.GREEN),
//                                Message.translation("lifesteal.new_max_health").param("").color(Color.PINK)
//                        );
//                        attacker.sendMessage(Message.translation("lifesteal.earned_max_health"));
//                    }
//                }
//            }
//        }
//    }
}