package com.github.sib_energy_craft.drilling_rig.block;

import com.github.sib_energy_craft.drilling_rig.block.entity.AbstractDrillingRigBlockEntity;
import com.github.sib_energy_craft.energy_api.Energy;
import com.github.sib_energy_craft.energy_api.EnergyLevel;
import com.github.sib_energy_craft.energy_api.items.ChargeableItem;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public abstract class AbstractDrillingRigBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty WORKING = BooleanProperty.of("working");

    @Getter
    private final EnergyLevel energyLevel;
    @Getter
    private final int maxCharge;
    @Getter
    private final float energyToHardnessRate;
    @Getter
    private final Energy energyPerMine;

    public AbstractDrillingRigBlock(@NotNull Settings settings,
                                    @NotNull EnergyLevel energyLevel,
                                    int maxCharge,
                                    float energyToHardnessRate,
                                    @NotNull Energy energyPerMine) {
        super(settings);
        this.energyLevel = energyLevel;
        this.maxCharge = maxCharge;
        this.energyToHardnessRate = energyToHardnessRate;
        this.energyPerMine = energyPerMine;
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(WORKING, false));
    }

    @Override
    public void onPlaced(@NotNull World world,
                         @NotNull BlockPos pos,
                         @NotNull BlockState state,
                         @Nullable LivingEntity placer,
                         @NotNull ItemStack itemStack) {
        if (world.getBlockEntity(pos) instanceof AbstractDrillingRigBlockEntity<?> entity) {
            final var item = itemStack.getItem();
            if (!itemStack.isEmpty() && (item instanceof ChargeableItem chargeableItem)) {
                entity.onPlaced(chargeableItem.getCharge(itemStack));
            }
        }
    }

    @Override
    public @NotNull ActionResult onUse(@NotNull BlockState state,
                                       @NotNull World world,
                                       @NotNull BlockPos pos,
                                       @NotNull PlayerEntity player,
                                       @NotNull Hand hand,
                                       @NotNull BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        this.openScreen(world, pos, player);
        return ActionResult.CONSUME;
    }

    @Override
    public void onStateReplaced(@NotNull BlockState state,
                                @NotNull World world,
                                @NotNull BlockPos pos,
                                @NotNull BlockState newState,
                                boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AbstractDrillingRigBlockEntity<?>) {
            world.updateComparators(pos, this);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasComparatorOutput(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(@NotNull BlockState state,
                                   @NotNull World world,
                                   @NotNull BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    public @NotNull BlockRenderType getRenderType(@NotNull BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(@NotNull StateManager.Builder<Block, BlockState> builder) {
        builder.add(WORKING, FACING);
    }

    protected abstract void openScreen(@NotNull World world,
                                       @NotNull BlockPos pos,
                                       @NotNull PlayerEntity player);


    @Nullable
    protected static <T extends BlockEntity,
            E extends AbstractDrillingRigBlockEntity> BlockEntityTicker<T> checkType(
            @NotNull World world,
            @NotNull BlockEntityType<T> givenType,
            @NotNull BlockEntityType<E> expectedType) {
        return world.isClient ? null : checkType(givenType, expectedType,
                AbstractDrillingRigBlockEntity::simpleDrillingTick);
    }
}