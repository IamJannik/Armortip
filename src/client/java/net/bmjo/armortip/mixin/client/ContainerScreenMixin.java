package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.client.gui.ArmortipRenderer;
import net.bmjo.armortip.client.gui.tooltip.ArmortipPositioner;
import net.bmjo.armortip.client.gui.tooltip.LeftTooltipPositioner;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class ContainerScreenMixin {
    @Shadow
    protected Slot focusedSlot;

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V", shift = At.Shift.AFTER))
    public void renderArmorTip(DrawContext context, int x, int y, CallbackInfo ci) {
        var itemStack = this.focusedSlot.getStack();
        if (ArmortipUtil.isTipItem(itemStack)) {
            if (FabricLoader.getInstance().isModLoaded("legendarytooltips"))
                ArmortipRenderer.renderArmorTip(context, itemStack, x, y, MinecraftClient.getInstance().player, LeftTooltipPositioner.INSTANCE, true);
            else
                ArmortipRenderer.renderArmorTip(context, itemStack, x, y, MinecraftClient.getInstance().player, ArmortipPositioner.INSTANCE, false);
        }
    }
}
