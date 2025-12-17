package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.util.ISlotScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenMixin implements ISlotScreen {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Override
    public @Nullable Slot getHoveredSlot() {
        return hoveredSlot;
    }
}
