package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.util.ISlotScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HandledScreen.class)
public class HandledScreenMixin implements ISlotScreen {

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Override
    public @Nullable Slot getFocusedSlot() {
        return focusedSlot;
    }
}
