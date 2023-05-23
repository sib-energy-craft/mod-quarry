package com.github.sib_energy_craft.drilling_rig.load.client;

import com.github.sib_energy_craft.drilling_rig.load.ScreenHandlers;
import com.github.sib_energy_craft.drilling_rig.screen.DrillingRigScreen;
import com.github.sib_energy_craft.sec_utils.load.DefaultClientModInitializer;

import static com.github.sib_energy_craft.sec_utils.utils.ScreenUtils.registerScreen;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public final class Screens implements DefaultClientModInitializer {

    static {
        registerScreen(ScreenHandlers.DRILLING_RIG, DrillingRigScreen::new);
    }

}
