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
            .build();

    private int stealAmount = 10;

    public LSConfig(){

    }

    public int getStealAmount(){
        return stealAmount;
    }
}
