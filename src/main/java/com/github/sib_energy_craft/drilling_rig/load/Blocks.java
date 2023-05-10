package com.github.sib_energy_craft.drilling_rig.load;

import com.github.sib_energy_craft.drilling_rig.block.DrillingRigBlock;
import com.github.sib_energy_craft.energy_api.Energy;
import com.github.sib_energy_craft.energy_api.EnergyLevel;
import com.github.sib_energy_craft.energy_api.utils.Identifiers;
import com.github.sib_energy_craft.sec_utils.common.Identified;
import com.github.sib_energy_craft.sec_utils.load.ModRegistrar;
import com.github.sib_energy_craft.sec_utils.utils.BlockUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;


/**
 * @since 0.0.1
 * @author sibmaks
 */
public final class Blocks implements ModRegistrar {

    public static final Identified<DrillingRigBlock> DRILLING_RIG;

    static {
        var drillSettings = AbstractBlock.Settings
                .of(Material.METAL)
                .strength(1.5F)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL);

        var drillingBlock = new DrillingRigBlock(drillSettings, EnergyLevel.L3, 1024, 0.05f, Energy.of(8));
        DRILLING_RIG = BlockUtils.register(Identifiers.of("drilling_rig"), drillingBlock);
    }
}
