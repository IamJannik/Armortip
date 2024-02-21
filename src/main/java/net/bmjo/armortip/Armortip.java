package net.bmjo.armortip;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Armortip implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("armortip");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Armortip user!");
	}
}