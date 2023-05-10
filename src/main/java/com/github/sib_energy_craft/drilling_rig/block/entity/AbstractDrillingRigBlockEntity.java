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
import net.minecraft.item.Items;
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
    protected boolean working;

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
        var working = blockEntity.working;
        blockEntity.working = false;

        if (blockEntity.energyContainer.hasEnergy()) {
            var underLineBlockStatePos = getUnderlineBlock(world, pos);
            if(underLineBlockStatePos != null) {
                var underLineBlockState = underLineBlockStatePos.getLeft();
                var underLineBlockPos = underLineBlockStatePos.getRight();
                float hardness = underLineBlockState.getHardness(world, underLineBlockPos);
                if(hardness <= blockEntity.chargedHardness) {
                    blockEntity.chargedHardness -= hardness;
                    world.breakBlock(underLineBlockPos, false);
                    var item = underLineBlockState.getBlock().asItem();
                    if(item != null && Items.AIR != item) {
                        blockEntity.inventory.addStack(new ItemStack(item, 1));
                    }
                } else {
                    blockEntity.energyContainer.subtract(blockEntity.block.getEnergyPerMine());
                    blockEntity.chargedHardness += blockEntity.block.getEnergyToHardnessRate();
                }
                changed = true;
                blockEntity.working = true;
            }
            blockEntity.markDirty();
        }

        if (working != blockEntity.working) {
            state = state.with(AbstractDrillingRigBlock.WORKING, blockEntity.working);
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
