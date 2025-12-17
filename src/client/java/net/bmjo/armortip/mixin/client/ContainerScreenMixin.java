package net.bmjo.armortip.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.bmjo.armortip.client.gui.ArmortipRenderer;
import net.bmjo.armortip.client.gui.tooltip.ArmortipPositioner;
import net.bmjo.armortip.client.gui.tooltip.LeftTooltipPositioner;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenMixin {
    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/ResourceLocation;)V", shift = At.Shift.AFTER))
    public void renderArmorTip(GuiGraphics guiGraphics, int i, int j, CallbackInfo ci, @Local ItemStack itemStack) {
        if (ArmortipUtil.isTipItem(itemStack)) {
            if (FabricLoader.getInstance().isModLoaded("legendarytooltips"))
                ArmortipRenderer.renderArmorTip(guiGraphics, itemStack, i, j, Minecraft.getInstance().player, LeftTooltipPositioner.INSTANCE, true);
            else
                ArmortipRenderer.renderArmorTip(guiGraphics, itemStack, i, j, Minecraft.getInstance().player, ArmortipPositioner.INSTANCE, false);
        }
    }
}
