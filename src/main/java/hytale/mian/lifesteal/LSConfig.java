package hytale.mian.lifesteal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LSConfig {
    public static final BuilderCodec<LSConfig> CODEC = BuilderCodec.<LSConfig>builder(LSConfig.class, LSConfig::new)
            .append(new KeyedCodec<Integer>("StealAmount", Codec.INTEGER),
                    (config, integer, extraInfo) -> config.stealAmount = integer,
                    (config, extraInfo) -> config.stealAmount)
            .add()
            .append(new KeyedCodec<Boolean>("EarnLifeFromNonPlayers", Codec.BOOLEAN),
                    (config, aBoolean, extraInfo) -> config.earnLifeFromNonPlayers = aBoolean,
                    (config, extraInfo) -> config.earnLifeFromNonPlayers)
            .add()
            .build();

    private int stealAmount = 10;
    private boolean earnLifeFromNonPlayers = false;

    public LSConfig(){

    }

    public int getStealAmount(){
        return stealAmount;
    }

    public boolean canEarnLifeFromNonPlayers(){
        return earnLifeFromNonPlayers;
    }
}
