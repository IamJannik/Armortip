package net.bmjo.armortip.client.gui.tooltip;

import net.bmjo.armortip.client.gui.ArmortipRenderer;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ArmortipPositioner implements TooltipPositioner {
    public static final TooltipPositioner INSTANCE = new ArmortipPositioner();

    private ArmortipPositioner() {

    }

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int mouseX, int mouseY, int width, int height) {
        int offset = getOffset();
        Vector2i start = new Vector2i(mouseX + offset + ArmortipRenderer.MARGIN, mouseY).add(12, -12);
        this.preventOverflow(screenWidth, screenHeight, start, width, height, offset);
        return start;
    }

    private void preventOverflow(int screenWidth, int screenHeight, Vector2i pos, int width, int height, int offset) {
        if (pos.x + width > screenWidth)
            pos.x = Math.max(pos.x - 24 - (width + offset + ArmortipRenderer.MARGIN), offset);

        int i = height + 3;
        if (pos.y + i > screenHeight)
            pos.y = screenHeight - i;
    }

    private static int getOffset() {
        ItemStack itemStack = ArmortipUtil.getFocusedItem();
        if (itemStack == null)
            return 0;
        return getOffset(Screen.getTooltipFromItem(MinecraftClient.getInstance(), itemStack), itemStack.getTooltipData());
    }

    private static int getOffset(List<Text> text, Optional<TooltipData> tooltips) {
        List<TooltipComponent> list = text.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
        tooltips.ifPresent((datax) -> list.add(1, TooltipComponent.of(datax)));
        return getOffset(list);
    }

    private static int getOffset(List<TooltipComponent> components) {
        int offset = 0;

        for (TooltipComponent tooltipComponent : components) {
            int k = tooltipComponent.getWidth(MinecraftClient.getInstance().textRenderer);
            if (k > offset)
                offset = k;
        }
        return offset;
    }
}