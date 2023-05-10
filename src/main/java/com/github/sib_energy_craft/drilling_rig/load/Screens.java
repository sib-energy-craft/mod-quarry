package com.github.sib_energy_craft.drilling_rig.load;

import com.github.sib_energy_craft.drilling_rig.screen.DrillingRigScreen;
import com.github.sib_energy_craft.drilling_rig.screen.DrillingRigScreenHandler;
import com.github.sib_energy_craft.energy_api.utils.Identifiers;
import com.github.sib_energy_craft.sec_utils.load.ModRegistrar;
import net.minecraft.screen.ScreenHandlerType;

import static com.github.sib_energy_craft.sec_utils.utils.ScreenUtils.register;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public final class Screens implements ModRegistrar {
    public static final ScreenHandlerType<DrillingRigScreenHandler> DRILLING_RIG;

    static {
        DRILLING_RIG = register(Identifiers.of("drilling_rig"), DrillingRigScreenHandler::new, DrillingRigScreen::new);
    }

}
