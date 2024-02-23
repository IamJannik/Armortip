package net.bmjo.armortip.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class LeftTooltipPositioner implements TooltipPositioner {
    public static final TooltipPositioner INSTANCE = new LeftTooltipPositioner();

    private LeftTooltipPositioner() {

    }

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int mouseX, int mouseY, int width, int height) {
        Vector2i start = new Vector2i(mouseX, mouseY).add(-12, -12);
        start.add(-width, 0);
        this.preventOverflow(screenHeight, start, width, height);
        return start;
    }

    private void preventOverflow(int guiHeight, Vector2i start, int width, int height) {
        if (start.x < 4)
            start.x = start.x + width + 24;

        int m = height + 3;
        if (start.y + m > guiHeight)
            start.y = guiHeight - m;
    }


}