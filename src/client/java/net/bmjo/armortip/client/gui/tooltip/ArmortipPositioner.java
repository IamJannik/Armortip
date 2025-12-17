package net.bmjo.armortip.client.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ArmortipPositioner implements ClientTooltipPositioner {
    public static final ClientTooltipPositioner INSTANCE = new ArmortipPositioner();

    private ArmortipPositioner() {

    }

    @Override
    public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int width, int height) {
        int offset = getOffset();
        Vector2i start = new Vector2i(mouseX + offset, mouseY).add(12, -12);
        this.preventOverflow(screenWidth, screenHeight, start, width, height, offset);
        return start;
    }

    private void preventOverflow(int screenWidth, int screenHeight, Vector2i pos, int width, int height, int offset) {
        if (pos.x + width > screenWidth)
            pos.x = Math.max(pos.x - 24 - (width + offset), offset);

        int i = height + 3;
        if (pos.y + i > screenHeight)
            pos.y = screenHeight - i;
    }

    private static int getOffset() {
        ItemStack itemStack = ArmortipUtil.getFocusedItem();
        if (itemStack == null)
            return 0;
        return getOffset(Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack), itemStack.getTooltipImage());
    }

    private static int getOffset(List<Component> text, Optional<TooltipComponent> tooltips) {
        var list = text.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        tooltips.ifPresent((data) -> list.add(1, ClientTooltipComponent.create(data)));
        return getOffset(list);
    }

    private static int getOffset(List<ClientTooltipComponent> components) {
        int offset = 0;

        for (var tooltipComponent : components) {
            int k = tooltipComponent.getWidth(Minecraft.getInstance().font);
            if (k > offset)
                offset = k;
        }
        return offset;
    }
}