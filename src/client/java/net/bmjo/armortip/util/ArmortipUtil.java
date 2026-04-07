package net.bmjo.armortip.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.Nullable;

public class ArmortipUtil {
    public static final int SIZE = 48;
    public static final int MARGIN = 6;
    public static final int PADDING_X = 0;
    public static final int PADDING_Y = 124;
    public static int ticks;

    public static boolean isTipItem(ItemStack itemStack) {
        return itemStack.get(DataComponents.EQUIPPABLE) != null
                || itemStack.getItem() instanceof SmithingTemplateItem;
                //|| itemStack.has(DataComponents.PROVIDES_BANNER_PATTERNS)
                //|| itemStack.has(DataComponents.ENTITY_DATA);
    }

    public static boolean shouldExtend() {
        ItemStack focusedItem = getFocusedItem();
        return focusedItem != null && isTipItem(focusedItem);
    }

    @Nullable
    public static ItemStack getFocusedItem() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu> screen && screen instanceof ISlotScreen iSlotScreen)
            if (screen.getMenu().getCarried().isEmpty() && iSlotScreen.getHoveredSlot() != null && iSlotScreen.getHoveredSlot().hasItem())
                return iSlotScreen.getHoveredSlot().getItem();
        return null;
    }

    public static void tick(Minecraft minecraft) {
        ticks++;
    }
}
