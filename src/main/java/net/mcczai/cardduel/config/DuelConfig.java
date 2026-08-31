package net.mcczai.cardduel.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 对局规则配置（SERVER 类型，服务端权威）。
 * 注册于 CardduelMod 构造器；读取使用静态 ConfigValue.get()。
 */
public final class DuelConfig {

    /** 秘密区（陷阱）上限：默认 3，可配置 1-8 */
    public static ModConfigSpec.IntValue TRAP_ZONE_LIMIT;

    private DuelConfig() {
    }

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("duel");

        builder.comment("Maximum number of secret traps a player may place on their trap zone (1-8).");
        TRAP_ZONE_LIMIT = builder.defineInRange("trapZoneLimit", 3, 1, 8);

        builder.pop();
        return builder.build();
    }
}
