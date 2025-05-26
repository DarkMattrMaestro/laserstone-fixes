package com.darkmattrmaestro.laserstone_fixes.configs;

import dev.crmodders.modmenu.api.ModMenuApi;
import dev.crmodders.modmenu.api.ConfigScreenFactory;

public class LaserstoneFixesModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LaserstoneFixesOptionMenu::new;
    }
}
