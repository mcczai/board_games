package net.mcczai.cardduel.client;

import net.mcczai.cardduel.items.ICard;
import net.mcczai.cardduel.resources.CommonCardIndex;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

//用在客户端的Tooltip实现
public record CardTooltip(ItemStack card, ICard iCard, CommonCardIndex cardIndex) implements TooltipComponent {
}
