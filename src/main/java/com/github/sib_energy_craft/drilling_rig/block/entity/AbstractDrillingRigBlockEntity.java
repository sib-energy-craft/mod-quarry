package com.github.sib_energy_craft.drilling_rig.block.entity;

import com.github.sib_energy_craft.containers.CleanEnergyContainer;
import com.github.sib_energy_craft.drilling_rig.block.AbstractDrillingRigBlock;
import com.github.sib_energy_craft.drilling_rig.tags.DrillingRigTags;
import com.github.sib_energy_craft.energy_api.Energy;
import com.github.sib_energy_craft.energy_api.EnergyOffer;
import com.github.sib_energy_craft.energy_api.consumer.EnergyConsumer;
import com.github.sib_energy_craft.energy_api.items.ChargeableItem;
import com.github.sib_energy_craft.pipes.api.ItemSupplier;
import com.github.sib_energy_craft.pipes.utils.PipeUtils;
import com.github.sib_energy_craft.sec_utils.screen.PropertyMap;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
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
        implements ExtendedScreenHandlerFactory, EnergyConsumer, ItemSupplier {
    public static final int TOOL_SLOT = 0;
    public static final int CHARGE_SLOT = 1;

    public static final int TOOLS_INVENTORY_SIZE = 2;
    public static final int MINING_INVENTORY_SIZE = 9;

    protected final SimpleInventory toolInventory;
    protected final SimpleInventory miningInventory;
    protected final PropertyMap<DrillingRigProperties> propertyMap;

    protected CleanEnergyContainer energyContainer;

    protected final B block;
    protected float chargedHardness;


    public AbstractDrillingRigBlockEntity(@NotNull BlockEntityType<?> blockEntityType,
                                            @NotNull BlockPos blockPos,
                                            @NotNull BlockState blockState,
                                            @NotNull B block) {
        super(blockEntityType, blockPos, blockState);

        this.toolInventory = new SimpleInventory(TOOLS_INVENTORY_SIZE);
        this.toolInventory.addListener(sender -> AbstractDrillingRigBlockEntity.this.markDirty());

        this.miningInventory = new SimpleInventory(MINING_INVENTORY_SIZE);
        this.miningInventory.addListener(sender -> AbstractDrillingRigBlockEntity.this.markDirty());

        this.energyContainer = new CleanEnergyContainer(Energy.ZERO, block.getMaxCharge());
        this.block = block;

        this.propertyMap = new PropertyMap<>(DrillingRigProperties.class);
        this.propertyMap.add(DrillingRigProperties.CHARGE, () -> this.energyContainer.getCharge().intValue());
        this.propertyMap.add(DrillingRigProperties.MAX_CHARGE, () -> this.energyContainer.getMaxCharge().intValue());
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        super.readNbt(nbt);

        var toolInventoryCompound = nbt.getCompound("ToolInventory");
        Inventories.readNbt(toolInventoryCompound, this.toolInventory.stacks);

        var miningInventoryCompound = nbt.getCompound("MiningInventory");
        Inventories.readNbt(miningInventoryCompound, this.miningInventory.stacks);

        this.energyContainer = CleanEnergyContainer.readNbt(nbt);
        this.chargedHardness = nbt.getFloat("ChargedHardness");
    }

    @Override
    protected void writeNbt(@NotNull NbtCompound nbt) {
        super.writeNbt(nbt);

        var toolInventoryCompound = new NbtCompound();
        Inventories.writeNbt(toolInventoryCompound, this.toolInventory.stacks);
        nbt.put("ToolInventory", toolInventoryCompound);

        var miningInventoryCompound = new NbtCompound();
        Inventories.writeNbt(miningInventoryCompound, this.miningInventory.stacks);
        nbt.put("MiningInventory", miningInventoryCompound);

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

        charge(blockEntity);

        if (blockEntity.energyContainer.hasEnergy()) {
            var underLineBlockStatePos = getUnderlineBlock(world, pos);
            if(underLineBlockStatePos != null) {
                var underLineBlockState = underLineBlockStatePos.getLeft();
                var underLineBlockPos = underLineBlockStatePos.getRight();
                float hardness = underLineBlockState.getHardness(world, underLineBlockPos);
                if(hardness <= blockEntity.chargedHardness) {
                    var miningItem = underLineBlockState.getBlock().asItem();
                    var miningItemStack = new ItemStack(miningItem, 1);
                    if(miningItemStack.isEmpty() || blockEntity.miningInventory.canInsert(miningItemStack)) {
                        blockEntity.chargedHardness -= hardness;
                        world.breakBlock(underLineBlockPos, false);
                        if (!miningItemStack.isEmpty()) {
                            blockEntity.miningInventory.addStack(miningItemStack);
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

    private static void charge(@NotNull AbstractDrillingRigBlockEntity<?> blockEntity) {
        var itemStack = blockEntity.toolInventory.getStack(CHARGE_SLOT);
        var item = itemStack.getItem();
        if (!itemStack.isEmpty() && (item instanceof ChargeableItem chargeableItem)) {
            int charge = chargeableItem.getCharge(itemStack);
            if (charge > 0) {
                int transferred = Math.min(charge, blockEntity.energyContainer.getFreeSpace().intValue());
                chargeableItem.discharge(itemStack, transferred);
                blockEntity.energyContainer.add(transferred);
            }
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
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
    }

    @Override
    public @NotNull List<ItemStack> canSupply(@NotNull Direction direction) {
        return Collections.unmodifiableList(miningInventory.stacks);
    }

    @Override
    public boolean supply(@NotNull ItemStack requested, @NotNull Direction direction) {
        return PipeUtils.supply(miningInventory, requested);
    }

    @Override
    public void returnStack(@NotNull ItemStack requested, @NotNull Direction direction) {
        miningInventory.addStack(requested);
    }
}
