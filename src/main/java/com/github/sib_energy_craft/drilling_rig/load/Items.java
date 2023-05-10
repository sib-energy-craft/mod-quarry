package com.github.sib_energy_craft.drilling_rig.load;

import com.github.sib_energy_craft.drilling_rig.item.DrillingRigItem;
import com.github.sib_energy_craft.sec_utils.load.ModRegistrar;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;

import static com.github.sib_energy_craft.sec_utils.utils.ItemUtils.register;


/**
 * @since 0.0.1
 * @author sibmaks
 */
public final class Items implements ModRegistrar {
    public static final Item DRILLING_RIG;

    static {
        var commonSettings = new Item.Settings();

        var drillingRig = new DrillingRigItem<>(Blocks.DRILLING_RIG.entity(), commonSettings);
        DRILLING_RIG = register(ItemGroups.FUNCTIONAL, Blocks.DRILLING_RIG.identifier(), drillingRig);
    }
}
