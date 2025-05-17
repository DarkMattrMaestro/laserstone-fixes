package com.darkmattrmaestro.laserstone_fixes;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;

import static com.darkmattrmaestro.laserstone_fixes.Constants.LOGGER;


public class LaserstoneFixesPuzzle implements ClientModInitializer {
    @Override
    public void onInit() {
        LOGGER.info("Initialized!");

    }
}
