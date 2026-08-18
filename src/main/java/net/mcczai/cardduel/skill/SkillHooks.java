package net.mcczai.cardduel.skill;

import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.mcczai.cardduel.duel.DuelPlayerData;
import net.minecraft.world.item.ItemStack;

/**
 * P2 技能系统预留钩子（当前为空实现）。
 *
 * P2 阶段将在此接入技能注册表（SkillRegistry），按卡牌 JSON 的 skill 字段
 * 分发到具体技能实现，DuelEngine 的调用点无需改动。
 */
public final class SkillHooks {

    private SkillHooks() {
    }

    /** 回合开始时（MP 回满、抽牌之前） */
    public static void onTurnStart(DuelTableBlockEntity table, DuelPlayerData data) {
    }

    /** 回合结束时（切换行动方之前） */
    public static void onTurnEnd(DuelTableBlockEntity table, DuelPlayerData data) {
    }

    /** 抽到一张牌时 */
    public static void onDraw(DuelTableBlockEntity table, DuelPlayerData data, ItemStack card) {
    }

    /** 玩家受到伤害时（hp 已扣减，胜负判定之前） */
    public static void onPlayerDamaged(DuelTableBlockEntity table, DuelPlayerData data, int amount) {
    }

    /** 召唤卡上场时 */
    public static void onSummoned(DuelTableBlockEntity table, DuelPlayerData owner, int slot, ItemStack card) {
    }

    /** 召唤卡发起攻击时（伤害结算之前） */
    public static void onAttack(DuelTableBlockEntity table, DuelPlayerData attackerOwner, int attackerSlot,
                                ItemStack attackerCard, DuelPlayerData targetOwner, int targetSlot, ItemStack targetCard) {
    }

    /** 召唤卡死亡进入弃牌堆时（槽位已清空） */
    public static void onDeath(DuelTableBlockEntity table, DuelPlayerData owner, int slot, ItemStack card) {
    }
}
