package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.client.gui.ArmortipRenderer;
import net.bmjo.armortip.mixin.annotation.ConditionalMixin;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = DrawContext.class)
@ConditionalMixin(modId = "legendarytooltips", applyIfPresent = false)
public class DrawContextMixin {
    // from bytecode
    @ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", ordinal = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I", shift = At.Shift.AFTER))
    private int setWidth(int width) {
        return ArmortipUtil.shouldExtend() ? width + ArmortipRenderer.WIDTH + ArmortipRenderer.MARGIN : width;
    }

    @ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", ordinal = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I", shift = At.Shift.AFTER))
    private int setHeight(int height) {
        return ArmortipUtil.shouldExtend() ? Math.max(height, ArmortipRenderer.HEIGHT) : height;
    }
}
