package com.github.sib_energy_craft.drilling_rig.load;

import com.github.sib_energy_craft.drilling_rig.block.DrillingRigBlock;
import com.github.sib_energy_craft.energy_api.EnergyLevel;
import com.github.sib_energy_craft.energy_api.utils.Identifiers;
import com.github.sib_energy_craft.sec_utils.common.Identified;
import com.github.sib_energy_craft.sec_utils.load.DefaultModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

import static com.github.sib_energy_craft.sec_utils.utils.BlockUtils.register;


/**
 * @since 0.0.1
 * @author sibmaks
 */
public final class Blocks implements DefaultModInitializer {

    public static final Identified<DrillingRigBlock> DRILLING_RIG;

    static {
        var drillSettings = AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .strength(1.5F)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL);

        var drillingBlock = new DrillingRigBlock(drillSettings, EnergyLevel.L3, 1024,0.95f, 1.2f);
        DRILLING_RIG = register(Identifiers.of("drilling_rig"), drillingBlock);
    }
}
