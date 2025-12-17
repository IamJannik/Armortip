package net.bmjo.armortip.mixin.client;

import net.bmjo.armortip.mixin.annotation.ConditionalMixin;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
@ConditionalMixin(modId = "legendarytooltips", applyIfPresent = false)
public class GuiGraphicsMixin {
    // from bytecode
    @ModifyVariable(method = "renderTooltipInternal", ordinal = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I", shift = At.Shift.AFTER))
    private int setWidth(int width) {
        return ArmortipUtil.shouldExtend() ? width + ArmortipUtil.WIDTH + ArmortipUtil.MARGIN * 2 : width;
    }

    @ModifyVariable(method = "renderTooltipInternal", ordinal = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I", shift = At.Shift.AFTER))
    private int setHeight(int height) {
        return ArmortipUtil.shouldExtend() ? Math.max(height, ArmortipUtil.HEIGHT) : height;
    }
}
