package com.darkmattrmaestro.laserstone_fixes.mixins;



import com.darkmattrmaestro.laserstone_fixes.LaserstoneFixesQuilt;
import finalforeach.cosmicreach.gamestates.MainMenu;
import finalforeach.cosmicreach.lwjgl3.Lwjgl3Launcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainMenu.class)
public class MainMenuMixinQuilt {
    @Inject(method = "create", at = @At("HEAD"))
    private void injected(CallbackInfo ci) {
        LaserstoneFixesQuilt.LOGGER.info("LaserstoneFixes QUILT mixin logged!");
    }
}