package com.darkmattrmaestro.laserstone_fixes;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaserstoneFixesQuilt implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Laserstone Fixes QUILT");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Laserstone Fixes QUILT Initialized!");
	}
}

