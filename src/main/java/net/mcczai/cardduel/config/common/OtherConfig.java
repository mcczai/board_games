package net.mcczai.cardduel.config.common;


import net.minecraftforge.common.ForgeConfigSpec;

public class OtherConfig {
    public static ForgeConfigSpec.BooleanValue DEFAULT_PACK_DEBUG;

    public static void init(ForgeConfigSpec.Builder builder){
        builder.push("other");

        builder.comment("When enabled, the reload command will not overwrite the default model file under config");
        DEFAULT_PACK_DEBUG = builder.define("DefaultPackDebug",false);
    }
}
