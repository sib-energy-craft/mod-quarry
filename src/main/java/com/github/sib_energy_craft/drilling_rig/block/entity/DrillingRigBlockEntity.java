package com.github.sib_energy_craft.drilling_rig.block.entity;

import com.github.sib_energy_craft.drilling_rig.block.DrillingRigBlock;
import com.github.sib_energy_craft.drilling_rig.load.Entities;
import com.github.sib_energy_craft.drilling_rig.screen.DrillingRigScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.drilling_rig");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DrillingRigScreenHandler(syncId, playerInventory, toolInventory, miningInventory, propertyMap,
                ScreenHandlerContext.create(world, pos));
    }
}
