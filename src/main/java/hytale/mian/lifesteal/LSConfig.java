package hytale.mian.lifesteal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LSConfig {
    public static final BuilderCodec<LSConfig> CODEC = BuilderCodec.<LSConfig>builder(LSConfig.class, LSConfig::new)
            .append(new KeyedCodec<Float>("StartAmount", Codec.FLOAT),
                    (config, aFloat, extraInfo) -> config.startAmount = aFloat,
                    (config, extraInfo) -> config.startAmount)
            .add()
            .append(new KeyedCodec<Float>("StealAmount", Codec.FLOAT),
                    (config, aFloat, extraInfo) -> config.stealAmount = aFloat,
                    (config, extraInfo) -> config.stealAmount)
            .add()
            .append(new KeyedCodec<Float>("CapAmount", Codec.FLOAT),
                    (config, aFloat, extraInfo) -> config.capAmount = aFloat,
                    (config, extraInfo) -> config.capAmount)
            .add()
            .append(new KeyedCodec<Float>("MinAmount", Codec.FLOAT),
                    (config, aFloat, extraInfo) -> config.minAmount = aFloat,
                    (config, extraInfo) -> config.minAmount)
            .add()
            .append(new KeyedCodec<Boolean>("BanAtMin", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.banAtMin = aBoolean,
                    (config, extraInfo) -> config.banAtMin)
            .add()
            .append(new KeyedCodec<Integer>("AutoReviveAfterTime", Codec.INTEGER),
                    (config, integer, extraInfo) -> config.reviveAfter = integer,
                    (config, extraInfo) -> config.reviveAfter)
            .add()
            .append(new KeyedCodec<Boolean>("LoseHealthOnEnvDeath", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.loseHealthOnEnvDeath = aBoolean,
                    (config, extraInfo) -> config.loseHealthOnEnvDeath)
            .add()
            .append(new KeyedCodec<Boolean>("LoseHealthOnPlayerDeath", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.loseHealthOnPlayerDeath = aBoolean,
                    (config, extraInfo) -> config.loseHealthOnPlayerDeath)
            .add()
            .append(new KeyedCodec<Boolean>("LoseHealthOnEntityDeath", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.loseHealthOnEntityDeath = aBoolean,
                    (config, extraInfo) -> config.loseHealthOnEntityDeath)
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
    private int reviveAfter = -1;
    private boolean loseHealthOnEnvDeath = false;
    private boolean loseHealthOnPlayerDeath = true;
    private boolean loseHealthOnEntityDeath = false;
    private boolean earnLifeFromNonPlayers = false;
    private boolean banAtMin = true;
    private boolean earnLifeEvenIfOtherEntityDoesntHaveEnough = false;

    public LSConfig(){

    }

    public boolean hasAutoRevive(){
        return reviveAfter != -1;
    }

    public boolean hasCap(){
        return capAmount != -1;
    }

    public boolean hasMin(){
        return minAmount != -1;
    }

    public int getAutoRevive(){
        return reviveAfter;
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

    public boolean canLoseHealthOnEnvDeath(){
        return loseHealthOnEnvDeath;
    }

    public boolean canLoseHealthOnPlayerDeath(){
        return loseHealthOnPlayerDeath;
    }

    public boolean canLoseHealthOnEntityDeath(){
        return loseHealthOnEntityDeath;
    }

    public boolean canEarnLifeFromNonPlayers(){
        return earnLifeFromNonPlayers;
    }

    public boolean canEarnLifeRegardlessIfEntityDoesntHaveEnough(){
        return earnLifeEvenIfOtherEntityDoesntHaveEnough;
    }
}
