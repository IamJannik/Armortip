package net.bmjo.armortip.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ArmorTooltipData(ItemStack itemStack) implements TooltipComponent {
}