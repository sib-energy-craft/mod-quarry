package com.github.sib_energy_craft.drilling_rig.block.entity;

import com.github.sib_energy_craft.containers.CleanEnergyContainer;
import com.github.sib_energy_craft.drilling_rig.block.AbstractDrillingRigBlock;
import com.github.sib_energy_craft.drilling_rig.tags.DrillingRigTags;
import com.github.sib_energy_craft.energy_api.Energy;
import com.github.sib_energy_craft.energy_api.EnergyOffer;
import com.github.sib_energy_craft.energy_api.consumer.EnergyConsumer;
import com.github.sib_energy_craft.pipes.api.ItemSupplier;
import com.github.sib_energy_craft.pipes.utils.PipeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public abstract class AbstractDrillingRigBlockEntity<B extends AbstractDrillingRigBlock> extends BlockEntity
        implements EnergyConsumer, ItemSupplier {
    public static final int INVENTORY_SIZE = 9;

    protected final SimpleInventory inventory;
    protected CleanEnergyContainer energyContainer;

    protected final B block;
    protected float chargedHardness;


    public AbstractDrillingRigBlockEntity(@NotNull BlockEntityType<?> blockEntityType,
                                            @NotNull BlockPos blockPos,
                                            @NotNull BlockState blockState,
                                            @NotNull B block) {
        super(blockEntityType, blockPos, blockState);
        this.inventory = new SimpleInventory(INVENTORY_SIZE);
        this.inventory.addListener(sender -> AbstractDrillingRigBlockEntity.this.markDirty());
        this.energyContainer = new CleanEnergyContainer(Energy.ZERO, block.getMaxCharge());
        this.block = block;
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory.stacks);
        this.energyContainer = CleanEnergyContainer.readNbt(nbt);
        this.chargedHardness = nbt.getFloat("ChargedHardness");
    }

    @Override
    protected void writeNbt(@NotNull NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory.stacks);
        this.energyContainer.writeNbt(nbt);
        nbt.putFloat("ChargedHardness", this.chargedHardness);
    }

    @Override
    public boolean isConsumeFrom(@NotNull Direction direction) {
        return true;
    }

    @Override
    public void receiveOffer(@NotNull EnergyOffer energyOffer) {
        final var energyLevel = block.getEnergyLevel();
        if (energyOffer.getEnergyAmount().compareTo(energyLevel.toBig) > 0) {
            if (energyOffer.acceptOffer()) {
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.breakBlock(pos, false);
                    return;
                }
            }
        }
        energyContainer.receiveOffer(energyOffer);
        markDirty();
    }

    /**
     * Method called when block of this entity is placed in the world.<br/>
     * As argument method accept charge of item, that used as basic block entity charge.
     *
     * @param charge item charge
     */
    public void onPlaced(int charge) {
        this.energyContainer.add(charge);
    }

    public static <T extends AbstractDrillingRigBlock> void simpleDrillingTick(
            @NotNull World world,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull AbstractDrillingRigBlockEntity<T> blockEntity) {
        if (world.isClient) {
            return;
        }
        var hasEnergy = blockEntity.energyContainer.hasEnergy();
        var changed = false;
        var worked = state.get(AbstractDrillingRigBlock.WORKING);
        var working = false;
        var fulled = state.get(AbstractDrillingRigBlock.FULL);
        var full = false;

        if (blockEntity.energyContainer.hasEnergy()) {
            var underLineBlockStatePos = getUnderlineBlock(world, pos);
            if(underLineBlockStatePos != null) {
                var underLineBlockState = underLineBlockStatePos.getLeft();
                var underLineBlockPos = underLineBlockStatePos.getRight();
                float hardness = underLineBlockState.getHardness(world, underLineBlockPos);
                if(hardness <= blockEntity.chargedHardness) {
                    var miningItem = underLineBlockState.getBlock().asItem();
                    var miningItemStack = new ItemStack(miningItem, 1);
                    if(miningItemStack.isEmpty() || blockEntity.inventory.canInsert(miningItemStack)) {
                        blockEntity.chargedHardness -= hardness;
                        world.breakBlock(underLineBlockPos, false);
                        if (!miningItemStack.isEmpty()) {
                            blockEntity.inventory.addStack(miningItemStack);
                        }
                    } else {
                        full = true;
                    }
                } else {
                    blockEntity.energyContainer.subtract(blockEntity.block.getEnergyPerMine());
                    blockEntity.chargedHardness += blockEntity.block.getEnergyToHardnessRate();
                }
                changed = true;
                working = true;
            }
            blockEntity.markDirty();
        }

        var stateChanged = false;
        if (worked != working) {
            state = state.with(AbstractDrillingRigBlock.WORKING, working);
            stateChanged = true;
        }
        if (fulled != full) {
            state = state.with(AbstractDrillingRigBlock.FULL, full);
            stateChanged = true;
        }
        if(stateChanged) {
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }
        if (hasEnergy != blockEntity.energyContainer.hasEnergy() || changed) {
            markDirty(world, pos, state);
        }
    }

    @Nullable
    private static Pair<BlockState, BlockPos> getUnderlineBlock(@NotNull World world, @NotNull BlockPos blockPos) {
        while (blockPos.getY() > world.getBottomY()) {
            blockPos = blockPos.down();
            var blockState = world.getBlockState(blockPos);
            if(blockState == null || blockState.isAir() || blockState.getHardness(world, blockPos) < 0 ||
                    DrillingRigTags.isDrillingRigIgnore(blockState)) {
                continue;
            }
            return new Pair<>(blockState, blockPos);
        }
        return null;
    }

    @Override
    public @NotNull List<ItemStack> canSupply(@NotNull Direction direction) {
        return Collections.unmodifiableList(inventory.stacks);
    }

    @Override
    public boolean supply(@NotNull ItemStack requested, @NotNull Direction direction) {
        return PipeUtils.supply(inventory, requested);
    }

    @Override
    public void returnStack(@NotNull ItemStack requested, @NotNull Direction direction) {
        inventory.addStack(requested);
    }
}
