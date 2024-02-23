package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.client.gui.ArmortipRenderer;
import net.bmjo.armortip.client.gui.tooltip.ArmortipPositioner;
import net.bmjo.armortip.client.gui.tooltip.LeftTooltipPositioner;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HandledScreen.class)
public class ContainerScreenMixin {
    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderArmorTip(DrawContext drawContext, int mouseX, int mouseY, CallbackInfo ci, ItemStack itemStack) {
        if (ArmortipUtil.isTipItem(itemStack)) {
            if (FabricLoader.getInstance().isModLoaded("legendarytooltips"))
                ArmortipRenderer.renderArmorTip(drawContext, itemStack, mouseX, mouseY, MinecraftClient.getInstance().player, LeftTooltipPositioner.INSTANCE, true);
            else
                ArmortipRenderer.renderArmorTip(drawContext, itemStack, mouseX, mouseY, MinecraftClient.getInstance().player, ArmortipPositioner.INSTANCE, false);
        }
    }
}
