package net.bmjo.armortip.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

public class ArmortipUtil {
    public static final int SIZE = 48;
    public static final int MARGIN = 6;

    public static boolean isTipItem(ItemStack itemStack) {
        return itemStack.get(DataComponentTypes.EQUIPPABLE) != null;
    }

    public static boolean shouldExtend() {
        ItemStack focusedItem = getFocusedItem();
        return focusedItem != null && isTipItem(focusedItem);
    }

    @Nullable
    public static ItemStack getFocusedItem() {
        if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<? extends ScreenHandler> handledScreen && handledScreen instanceof ISlotScreen islotScreen)
            if (handledScreen.getScreenHandler().getCursorStack().isEmpty() && islotScreen.getFocusedSlot() != null && islotScreen.getFocusedSlot().hasStack())
                return islotScreen.getFocusedSlot().getStack();
        return null;
    }
}
