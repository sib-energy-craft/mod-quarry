package com.github.sib_energy_craft.drilling_rig.load;

import com.github.sib_energy_craft.drilling_rig.screen.DrillingRigScreenHandler;
import com.github.sib_energy_craft.energy_api.utils.Identifiers;
import com.github.sib_energy_craft.sec_utils.load.DefaultModInitializer;
import net.minecraft.screen.ScreenHandlerType;

import static com.github.sib_energy_craft.sec_utils.utils.ScreenUtils.registerHandler;

/**
 * @since 0.0.5
 * @author sibmaks
 */
public final class ScreenHandlers implements DefaultModInitializer {
    public static final ScreenHandlerType<DrillingRigScreenHandler> DRILLING_RIG;

    static {
        DRILLING_RIG = registerHandler(Identifiers.of("drilling_rig"), DrillingRigScreenHandler::new);
    }

}
