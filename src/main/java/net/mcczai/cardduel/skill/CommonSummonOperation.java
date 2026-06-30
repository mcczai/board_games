package net.mcczai.cardduel.skill;

import net.mcczai.cardduel.items.ICard;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

/**
 * 常规和召唤物相关事件，其中已有以下事件：
 * 死亡检查
 * 死亡事件
 * 伤害事件
 * 增加血量事件
 * 增加攻击力事件
 * 减少攻击力事件
 * @version 1.0
 * @author mcczai
 */
public class CommonSummonOperation {
    public static final int CommonHp = 1;
    public static final int CommonAtk = 0;

    /**
     * 用来判定此次攻击是否造成召唤物死亡
     * @param ATK 攻击方攻击力
     * @param HP 被攻击方血量
     * @return true为死亡，false为没死
     */
    public static boolean deathVerdict(int ATK, int HP) {
        return ATK >= HP;
    }

    /**
     * 普通死亡事件，给血量归零的召唤牌增加死亡标签
     * @param itemStack 死亡的召唤物卡牌
     */
    public static void commonDeath(@NotNull ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("death", true);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * 造成伤害事件
     * @param itemStack1 攻击召唤物
     * @param itemStack2 被攻击召唤物
     */
    public static void commonAttack(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
        ICard attacker = ICard.getICardOrNull(itemStack1);
        ICard defender = ICard.getICardOrNull(itemStack2);
        if (attacker == null || defender == null) {
            return;
        }
        int item1ATK = attacker.getATK(itemStack1);
        int item2HP = defender.getHP(itemStack2);
        if (deathVerdict(item1ATK, item2HP)) {
            defender.setHP(itemStack2, 0);
            commonDeath(itemStack2);
        } else {
            defender.setHP(itemStack2, item2HP - item1ATK);
        }
    }

    /**
     * 增加召唤物血量事件(无上限？)
     * @param itemStack 目标召唤物
     * @param hp 增加的血量
     */
    public static void commonTreat(@NotNull ItemStack itemStack, int hp) {
        ICard iCard = ICard.getICardOrNull(itemStack);
        if (iCard != null) {
            iCard.setHP(itemStack, iCard.getHP(itemStack) + hp);
        }
    }

    /**
     * 增加召唤物伤害事件
     * @param itemStack 目标召唤物
     * @param atk 增加的攻击力
     */
    public static void commonAtkUp(@NotNull ItemStack itemStack, int atk) {
        ICard iCard = ICard.getICardOrNull(itemStack);
        if (iCard != null) {
            iCard.setATK(itemStack, iCard.getATK(itemStack) + atk);
        }
    }

    /**
     * 减少召唤物伤害事件(最低为0)
     * @param itemStack 目标召唤物
     * @param atk 减少的攻击力
     */
    public static void commonAtkDown(@NotNull ItemStack itemStack, int atk) {
        ICard iCard = ICard.getICardOrNull(itemStack);
        if (iCard != null) {
            int currentAtk = iCard.getATK(itemStack);
            int newAtk = Math.max(currentAtk - atk, CommonAtk);
            iCard.setATK(itemStack, newAtk);
        }
    }

}
