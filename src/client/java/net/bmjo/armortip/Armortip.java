package net.bmjo.armortip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Armortip implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ArmortipUtil::tick);
	}
}