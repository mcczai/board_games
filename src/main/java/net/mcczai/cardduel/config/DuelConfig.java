package net.mcczai.cardduel.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 对局规则配置（SERVER 类型，服务端权威）。
 * 注册于 CardduelMod 构造器；读取使用静态 ConfigValue.get()。
 */
public final class DuelConfig {

    /** 秘密区（陷阱）上限：默认 3，可配置 1-8 */
    public static ModConfigSpec.IntValue TRAP_ZONE_LIMIT;

    /** 对局回合上限：默认 50，可配置 10-200；达到上限按剩余血量判胜/平局 */
    public static ModConfigSpec.IntValue MAX_TURN_LIMIT;

    private DuelConfig() {
    }

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("duel");

        builder.comment("Maximum number of secret traps a player may place on their trap zone (1-8).");
        TRAP_ZONE_LIMIT = builder.defineInRange("trapZoneLimit", 3, 1, 8);

        builder.comment("Maximum number of rounds in a duel (10-200). When reached, the duel ends",
                "immediately and the winner is decided by remaining HP (equal HP = draw).",
                "WARNING: setting this too high can bloat duel state and save files — the discard",
                "pile keeps growing, and combined with card-generating/healing effects it can grow",
                "without bound, causing lag or save corruption. Keep it at 100 or below unless",
                "you really need it.");
        MAX_TURN_LIMIT = builder.defineInRange("maxTurnLimit", 50, 10, 200);

        builder.pop();
        return builder.build();
    }
}
