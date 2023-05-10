package com.github.sib_energy_craft.drilling_rig.block.entity;

import com.github.sib_energy_craft.drilling_rig.block.DrillingRigBlock;
import com.github.sib_energy_craft.drilling_rig.load.Entities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public class DrillingRigBlockEntity extends AbstractDrillingRigBlockEntity<DrillingRigBlock> {
    public DrillingRigBlockEntity(@NotNull DrillingRigBlock block,
                                  @NotNull BlockPos blockPos,
                                  @NotNull BlockState blockState) {
        super(Entities.DRILLING_RIG, blockPos, blockState, block);
    }
}
