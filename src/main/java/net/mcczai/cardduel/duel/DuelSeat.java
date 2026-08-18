package net.mcczai.cardduel.duel;

import net.minecraft.core.BlockPos;

/**
 * 玩家对局座位（DataAttachment 载体）。
 * 记录玩家所在牌桌的坐标与座位身份（房主/客人）。
 */
public record DuelSeat(BlockPos tablePos, boolean host) {
}
