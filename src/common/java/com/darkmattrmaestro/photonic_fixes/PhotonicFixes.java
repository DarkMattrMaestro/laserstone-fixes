package com.darkmattrmaestro.photonic_fixes;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.common.ModInit;

import com.darkmattrmaestro.photonic_fixes.Constants;

public class PhotonicFixes implements ModInit {
    @Override
    public void onInit() {
        Constants.LOGGER.info("Photonic Fixes Initialized!");
    }
}
