package net.bmjo.armortip;

import net.bmjo.armortip.client.gui.tooltip.ArmorTooltipComponent;
import net.bmjo.armortip.client.gui.tooltip.ArmorTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Armortip implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("armortip");
	@Override
	public void onInitializeClient() {
		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof ArmorTooltipData armorTooltipData) {
				return new ArmorTooltipComponent(armorTooltipData);
			}
			return null;
		});
	}
}