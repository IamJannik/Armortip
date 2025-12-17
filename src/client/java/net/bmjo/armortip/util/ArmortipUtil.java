package net.bmjo.armortip.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.Nullable;

public class ArmortipUtil {
    protected static final int SIZE = 24;
    public static final int WIDTH = SIZE;
    public static final int HEIGHT = SIZE * 2;
    public static final int MARGIN = 6;
    public static int ticks;

    public static boolean isTipItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof Equipable || itemStack.getItem() instanceof SmithingTemplateItem || itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipable;
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
