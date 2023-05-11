package com.github.sib_energy_craft.drilling_rig.load;

import com.github.sib_energy_craft.drilling_rig.block.entity.DrillingRigBlockEntity;
import com.github.sib_energy_craft.sec_utils.load.DefaultModInitializer;
import com.github.sib_energy_craft.sec_utils.utils.EntityUtils;
import net.minecraft.block.entity.BlockEntityType;


/**
 * @since 0.0.1
 * @author sibmaks
 */
public final class Entities implements DefaultModInitializer {
    public static final BlockEntityType<DrillingRigBlockEntity> DRILLING_RIG;

    static {
        DRILLING_RIG = EntityUtils.register(Blocks.DRILLING_RIG,
                (pos, state) -> new DrillingRigBlockEntity(Blocks.DRILLING_RIG.entity(), pos, state));
    }
}
