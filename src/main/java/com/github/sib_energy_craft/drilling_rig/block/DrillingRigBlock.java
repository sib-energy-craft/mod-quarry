package com.github.sib_energy_craft.drilling_rig.block;

import com.github.sib_energy_craft.drilling_rig.block.entity.DrillingRigBlockEntity;
import com.github.sib_energy_craft.drilling_rig.load.Entities;
import com.github.sib_energy_craft.energy_api.EnergyLevel;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public class DrillingRigBlock extends AbstractDrillingRigBlock {
    public DrillingRigBlock(@NotNull Settings settings,
                            @NotNull EnergyLevel energyLevel,
                            int maxCharge,
                            float mineSpeedMultiplier,
                            float energyPerMineMultiplier) {
        super(settings, energyLevel, maxCharge, mineSpeedMultiplier, energyPerMineMultiplier);
    }

    @NotNull
    @Override
    public BlockEntity createBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DrillingRigBlockEntity(this, pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull World world,
                                                                  @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        return checkType(world, type, Entities.DRILLING_RIG);
    }
}
