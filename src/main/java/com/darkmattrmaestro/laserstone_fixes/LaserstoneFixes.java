package com.darkmattrmaestro.laserstone_fixes;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.common.ModInit;

import com.darkmattrmaestro.laserstone_fixes.Constants;

public class LaserstoneFixes implements ModInit {
    @Override
    public void onInit() {
        Constants.LOGGER.info("Laserstone Fixes Initialized!");
    }
}
