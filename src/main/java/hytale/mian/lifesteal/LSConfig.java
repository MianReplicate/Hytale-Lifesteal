package hytale.mian.lifesteal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LSConfig {
    public static final BuilderCodec<LSConfig> CODEC = BuilderCodec.<LSConfig>builder(LSConfig.class, LSConfig::new)
            .append(new KeyedCodec<Float>("StartAmount", Codec.FLOAT),
                    (config, integer, extraInfo) -> config.startAmount = integer,
                    (config, extraInfo) -> config.startAmount)
            .add()
            .append(new KeyedCodec<Float>("StealAmount", Codec.FLOAT),
                    (config, integer, extraInfo) -> config.stealAmount = integer,
                    (config, extraInfo) -> config.stealAmount)
            .add()
            .append(new KeyedCodec<Float>("CapAmount", Codec.FLOAT),
                    (config, integer, extraInfo) -> config.capAmount = integer,
                    (config, extraInfo) -> config.capAmount)
            .add()
            .append(new KeyedCodec<Float>("MinAmount", Codec.FLOAT),
                    (config, integer, extraInfo) -> config.minAmount = integer,
                    (config, extraInfo) -> config.minAmount)
            .add()
            .append(new KeyedCodec<Boolean>("BanAtMin", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.banAtMin = aBoolean,
                    (config, extraInfo) -> config.banAtMin)
            .add()
            .append(new KeyedCodec<Boolean>("EarnLifeFromNonPlayers", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.earnLifeFromNonPlayers = aBoolean,
                    (config, extraInfo) -> config.earnLifeFromNonPlayers)
            .add()
            .append(new KeyedCodec<Boolean>("CanEarnLifeFromEntitiesEvenIfTheyDontHaveEnough", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.earnLifeEvenIfOtherEntityDoesntHaveEnough = aBoolean,
                    (config, extraInfo) -> config.earnLifeEvenIfOtherEntityDoesntHaveEnough)
            .add()
            .build();

    private float minAmount = -100;
    private float capAmount = 100;
    private float startAmount = 0;
    private float stealAmount = 10;
    private boolean earnLifeFromNonPlayers = false;
    private boolean banAtMin = false;
    private boolean earnLifeEvenIfOtherEntityDoesntHaveEnough = false;

    public LSConfig(){

    }

    public boolean hasCap(){
        return capAmount != -1;
    }

    public boolean hasMin(){
        return minAmount != -1;
    }

    public float getMinAmount(){
        return minAmount;
    }

    public float getCapAmount(){
        return capAmount;
    }

    public float getStealAmount(){
        return stealAmount;
    }

    public float getStartAmount(){
        return startAmount;
    }

    public boolean banAtMin(){
        return banAtMin;
    }

    public boolean canEarnLifeFromNonPlayers(){
        return earnLifeFromNonPlayers;
    }

    public boolean canEarnLifeRegardlessIfEntityDoesntHaveEnough(){
        return earnLifeEvenIfOtherEntityDoesntHaveEnough;
    }
}
