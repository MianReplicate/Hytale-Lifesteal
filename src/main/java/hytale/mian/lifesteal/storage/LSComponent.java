package hytale.mian.lifesteal.storage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import hytale.mian.lifesteal.Lifesteal;
import org.jetbrains.annotations.Nullable;

public class LSComponent implements Component<EntityStore> {
    public static final BuilderCodec<LSComponent> CODEC;
    private float healthDifference;

    public static ComponentType<EntityStore, LSComponent> getComponentType(){
        return LSComponents.get().LS_COMPONENT;
    }

    protected LSComponent(){
        this(Lifesteal.config.get().getStartAmount());
    }

    public LSComponent(float healthDifference){
        this.healthDifference = healthDifference;
    }

    public void addHealthDifference(float healthDifference){
        setHealthDifference(this.healthDifference + healthDifference);
    }

    public void setHealthDifference(float healthDifference){
        float cap = Lifesteal.config.get().getCapAmount();
        float min = Lifesteal.config.get().getMinAmount();

        this.healthDifference = Math.max(Math.min(healthDifference, Lifesteal.config.get().hasCap() ? cap : Float.MAX_VALUE), Lifesteal.config.get().hasMin() ? min : Float.MIN_VALUE);
    }

    public float getHealthDifference(){
        return this.healthDifference;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new LSComponent(this.healthDifference);
    }

    static {
        CODEC = BuilderCodec.<LSComponent>builder(LSComponent.class, LSComponent::new)
                .append(new KeyedCodec<Float>("HealthDifference", Codec.FLOAT),
                        (lsComponent, health) -> lsComponent.healthDifference = health,
                        (lsComponent -> lsComponent.healthDifference))
                .add()
                .build();
    }
}
