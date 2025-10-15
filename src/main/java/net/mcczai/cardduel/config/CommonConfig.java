package net.mcczai.cardduel.config;

import net.mcczai.cardduel.config.common.OtherConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    public static ForgeConfigSpec init(){
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        OtherConfig.init(builder);
        return builder.build();
    }
}
