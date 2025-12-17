package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.gui.tooltip.ArmorTooltipData;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void addMyAmazingComponent(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (cir.getReturnValue().isPresent()) {
            return;
        }
        cir.setReturnValue(Optional.of(new ArmorTooltipData(stack)));
    }
}