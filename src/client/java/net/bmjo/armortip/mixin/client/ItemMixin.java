package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.gui.tooltip.ArmorTooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "getTooltipData", at = @At("RETURN"), cancellable = true)
    private void addMyAmazingComponent(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (cir.getReturnValue().isPresent()) {
            return;
        }
        cir.setReturnValue(Optional.of(new ArmorTooltipData(stack)));
    }
}
