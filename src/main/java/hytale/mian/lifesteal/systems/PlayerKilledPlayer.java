package hytale.mian.lifesteal.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import hytale.mian.lifesteal.LSConfig;
import hytale.mian.lifesteal.storage.LSComponent;

import javax.annotation.Nonnull;

public class PlayerKilledPlayer extends DeathSystems.OnDeathSystem {
    private final Config<LSConfig> config;

    public PlayerKilledPlayer(Config<LSConfig> config){
        this.config = config;
    }

    public Query getQuery() {
        return Archetype.of(Player.getComponentType());
    }

    public void onComponentAdded(@Nonnull Ref ref, @Nonnull DeathComponent component, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        Damage deathInfo = component.getDeathInfo();
        DamageCause deathCause = component.getDeathCause();
        if (deathCause == DamageCause.PHYSICAL || deathCause == DamageCause.PROJECTILE) {
            if (deathInfo != null) {
                Damage.Source var9 = deathInfo.getSource();
                if (var9 instanceof Damage.EntitySource) {
                    Damage.EntitySource entitySource = (Damage.EntitySource)var9;
                    Ref<EntityStore> sourceRef = entitySource.getRef();
                    if (!sourceRef.isValid()) {
                        return;
                    }

                    Player attacker = (Player)store.getComponent(sourceRef, Player.getComponentType());
                    if (attacker == null) {
                        return;
                    }

                    int stealAmount = config.get().getStealAmount();
                    attacker.toHolder().ensureAndGetComponent(LSComponent.getComponentType()).addHealthDifference(stealAmount);
                    ((LSComponent)ref.getStore().ensureAndGetComponent(ref, LSComponent.getComponentType())).addHealthDifference(-stealAmount);
                    return;
                }
            }

        }
    }
}