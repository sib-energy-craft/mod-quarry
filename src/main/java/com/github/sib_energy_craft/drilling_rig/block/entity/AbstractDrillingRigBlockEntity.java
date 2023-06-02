package com.github.sib_energy_craft.drilling_rig.block.entity;

import com.github.sib_energy_craft.containers.CleanEnergyContainer;
import com.github.sib_energy_craft.drilling_rig.block.AbstractDrillingRigBlock;
import com.github.sib_energy_craft.drilling_rig.tags.DrillingRigTags;
import com.github.sib_energy_craft.energy_api.Energy;
import com.github.sib_energy_craft.energy_api.EnergyOffer;
import com.github.sib_energy_craft.energy_api.consumer.EnergyConsumer;
import com.github.sib_energy_craft.energy_api.items.ChargeableItem;
import com.github.sib_energy_craft.energy_tools.item.MiningDrillItem;
import com.github.sib_energy_craft.pipes.api.ItemSupplier;
import com.github.sib_energy_craft.pipes.utils.PipeUtils;
import com.github.sib_energy_craft.sec_utils.screen.PropertyMap;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.Collections;
import java.util.List;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public abstract class AbstractDrillingRigBlockEntity<B extends AbstractDrillingRigBlock> extends BlockEntity
        implements ExtendedScreenHandlerFactory, EnergyConsumer, ItemSupplier {
    public static final int MAX_OFFSET = 9;
    public static final int MAX_SIZE = 64;

    public static final int TOOL_SLOT = 0;
    public static final int CHARGE_SLOT = 1;

    public static final int TOOLS_INVENTORY_SIZE = 2;
    public static final int MINING_INVENTORY_SIZE = 9;

    protected final SimpleInventory toolInventory;
    protected final SimpleInventory miningInventory;
    protected final PropertyMap<DrillingRigProperties> propertyMap;

    protected CleanEnergyContainer energyContainer;

    protected final B block;
    protected int chargedTicks;
    private volatile boolean needRecalculate;

    private final Vector2i leftBottomOffset;
    private final Vector2i sizeRectangle;

    private final Vector3i startPosition;
    private final Vector3i endPosition;

    private final Vector3i currentPosition;


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
        this.leftBottomOffset = new Vector2i(0, 0);
        this.sizeRectangle = new Vector2i(1, 1);
        this.currentPosition = new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.startPosition = new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.endPosition = new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.recalculateMingArea(blockPos, blockState);

        this.propertyMap = new PropertyMap<>(DrillingRigProperties.class);
        this.propertyMap.add(DrillingRigProperties.CHARGE, () -> this.energyContainer.getCharge().intValue());
        this.propertyMap.add(DrillingRigProperties.MAX_CHARGE, () -> this.energyContainer.getMaxCharge().intValue());
        this.propertyMap.add(DrillingRigProperties.START_POSITION_X, () -> leftBottomOffset.x);
        this.propertyMap.add(DrillingRigProperties.START_POSITION_Y, () -> leftBottomOffset.y);
        this.propertyMap.add(DrillingRigProperties.WIDTH, () -> sizeRectangle.x);
        this.propertyMap.add(DrillingRigProperties.HEIGHT, () -> sizeRectangle.y);
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        super.readNbt(nbt);

        var toolInventoryCompound = nbt.getCompound("ToolInventory");
        Inventories.readNbt(toolInventoryCompound, this.toolInventory.stacks);

        var miningInventoryCompound = nbt.getCompound("MiningInventory");
        Inventories.readNbt(miningInventoryCompound, this.miningInventory.stacks);

        this.energyContainer = CleanEnergyContainer.readNbt(nbt);
        this.chargedTicks = nbt.getInt("ChargedHardness");

        this.leftBottomOffset.x = nbt.getInt("LeftBottomOffsetX");
        this.leftBottomOffset.y = nbt.getInt("LeftBottomOffsetY");

        this.sizeRectangle.x = nbt.getInt("SizeRectangleX");
        this.sizeRectangle.y = nbt.getInt("SizeRectangleY");

        this.needRecalculate = true;
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
        nbt.putInt("ChargedHardness", this.chargedTicks);

        nbt.putInt("LeftBottomOffsetX", leftBottomOffset.x);
        nbt.putInt("LeftBottomOffsetY", leftBottomOffset.y);

        nbt.putInt("SizeRectangleX", sizeRectangle.x);
        nbt.putInt("SizeRectangleY", sizeRectangle.y);
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
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) {
            return;
        }
        if(blockEntity.needRecalculate) {
            blockEntity.recalculateMingArea(pos, state);
            blockEntity.needRecalculate = false;
        }
        var hasEnergy = blockEntity.energyContainer.hasEnergy();
        var changed = false;
        var worked = state.get(AbstractDrillingRigBlock.WORKING);
        var working = false;
        var fulled = state.get(AbstractDrillingRigBlock.FULL);
        var full = false;

        chargeBlock(blockEntity);

        var toolStack = blockEntity.toolInventory.getStack(TOOL_SLOT);
        if (blockEntity.energyContainer.hasEnergy()) {
            chargeTool(blockEntity, toolStack);
        }
        if (!toolStack.isEmpty()) {
            var toolItem = toolStack.getItem();
            if (toolItem instanceof MiningDrillItem miningDrillItem) {
                var underLineBlockStatePos = getUnderlineBlock(world, blockEntity.currentPosition);
                if (underLineBlockStatePos != null) {
                    var underLineBlockState = underLineBlockStatePos.getLeft();
                    var underLineBlockPos = underLineBlockStatePos.getRight();
                    int ticksToBreak = (int) (calcTicksToBreak(underLineBlockState, world, underLineBlockPos, toolStack) *
                                                blockEntity.block.getMineSpeedMultiplier());
                    if (ticksToBreak <= blockEntity.chargedTicks) {
                        var builder = new LootContextParameterSet.Builder(serverWorld)
                                .add(LootContextParameters.ORIGIN, underLineBlockPos.toCenterPos())
                                .add(LootContextParameters.TOOL, toolStack);
                        var miningItemStacks = underLineBlockState.getDroppedStacks(builder);
                        boolean enoughSpace = miningItemStacks.stream()
                                .allMatch(it -> it.isEmpty() || blockEntity.miningInventory.canInsert(it));
                        if (enoughSpace) {
                            var requiredEnergy = miningDrillItem.getEnergyPerMine() * blockEntity.block.getEnergyPerMineMultiplier();
                            if (miningDrillItem.discharge(toolStack, (int) requiredEnergy)) {
                                working = true;
                                blockEntity.chargedTicks -= ticksToBreak;
                                world.breakBlock(underLineBlockPos, false);
                                goToNextBlock(blockEntity);
                                boolean canHarvest = canHarvest(underLineBlockState, toolStack);
                                for (var miningItemStack : miningItemStacks) {
                                    if (!miningItemStack.isEmpty() && canHarvest) {
                                        blockEntity.miningInventory.addStack(miningItemStack);
                                    }
                                }
                            }
                        } else {
                            full = true;
                        }
                    } else {
                        if(miningDrillItem.hasEnergy(toolStack)) {
                            blockEntity.chargedTicks++;
                            working = true;
                        }
                    }
                } else {
                    goToNextBlock(blockEntity);
                }
                changed = true;
            }
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
        if (stateChanged) {
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }
        if (hasEnergy != blockEntity.energyContainer.hasEnergy() || changed) {
            markDirty(world, pos, state);
        }
    }

    private static <T extends AbstractDrillingRigBlock> void goToNextBlock(
            @NotNull AbstractDrillingRigBlockEntity<T> blockEntity) {
        var currentPosition = blockEntity.currentPosition;
        var startPos = blockEntity.startPosition;
        var endPos = blockEntity.endPosition;
        if (currentPosition.x + 1 >= endPos.x) {
            if (currentPosition.z + 1 >= endPos.z) {
                currentPosition.x = startPos.x;
                currentPosition.z = startPos.z;
            } else {
                currentPosition.x = startPos.x;
                currentPosition.z++;
            }
        } else {
            currentPosition.x++;
        }
    }

    public void moveLeftRight(boolean moveLeft) {
        leftBottomOffset.x += moveLeft ? -1 : 1;
        if(leftBottomOffset.x < -MAX_OFFSET) {
            leftBottomOffset.x = -MAX_OFFSET;
        } else if(leftBottomOffset.x > MAX_OFFSET) {
            leftBottomOffset.x = MAX_OFFSET;
        }
        needRecalculate = true;
    }

    public void moveUpDown(boolean moveUp) {
        leftBottomOffset.y += moveUp ? -1 : 1;
        if(leftBottomOffset.y < -MAX_OFFSET) {
            leftBottomOffset.y = -MAX_OFFSET;
        } else if(leftBottomOffset.y > MAX_OFFSET) {
            leftBottomOffset.y = MAX_OFFSET;
        }
        needRecalculate = true;
    }

    public void changeWidth(boolean increase) {
        sizeRectangle.x += increase ? 1 : -1;
        if(sizeRectangle.x <= 0) {
            sizeRectangle.x = 1;
        } else if(sizeRectangle.x > MAX_SIZE) {
            sizeRectangle.x = MAX_SIZE;
        }
        needRecalculate = true;
    }

    public void changeHeight(boolean increase) {
        sizeRectangle.y += increase ? 1 : -1;
        if(sizeRectangle.y <= 0) {
            sizeRectangle.y = 1;
        } else if(sizeRectangle.y > MAX_SIZE) {
            sizeRectangle.y = MAX_SIZE;
        }
        needRecalculate = true;
    }

    protected void recalculateMingArea(@NotNull BlockPos blockPos,
                                       @NotNull BlockState blockState) {
        var facing = blockState.get(AbstractDrillingRigBlock.FACING);
        this.startPosition.set(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.endPosition.set(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if(facing == Direction.WEST) {
            this.startPosition.x += this.leftBottomOffset.y;
            this.startPosition.z += this.leftBottomOffset.x;
            this.endPosition.set(this.startPosition);
            this.endPosition.x += this.sizeRectangle.y;
            this.endPosition.z += this.sizeRectangle.x;
        } else if(facing == Direction.EAST) {
            this.endPosition.x -= this.leftBottomOffset.y - 1;
            this.endPosition.z -= this.leftBottomOffset.x - 1;
            this.startPosition.set(this.endPosition);
            this.startPosition.x -= this.sizeRectangle.y;
            this.startPosition.z -= this.sizeRectangle.x;
        } else if(facing == Direction.NORTH) {
            this.startPosition.x -= this.leftBottomOffset.x + this.sizeRectangle.x - 1;
            this.startPosition.z += this.leftBottomOffset.y;

            this.endPosition.x -= this.leftBottomOffset.x - 1;
            this.endPosition.z = this.startPosition.z + this.sizeRectangle.y;
        } else if(facing == Direction.SOUTH) {
            this.startPosition.x += this.leftBottomOffset.x;
            this.startPosition.z -= this.leftBottomOffset.y + this.sizeRectangle.y - 1;

            this.endPosition.x = this.startPosition.x + this.sizeRectangle.x;
            this.endPosition.z -= this.leftBottomOffset.y - 1;
        }

        this.currentPosition.set(this.startPosition);
    }

    protected static int calcTicksToBreak(@NotNull BlockState state,
                                          @NotNull BlockView world,
                                          @NotNull BlockPos pos,
                                          @NotNull ItemStack toolStack) {
        float hardness = state.getHardness(world, pos);
        if (hardness == -1.0f) {
            return 0;
        }
        int multiplier = canHarvest(state, toolStack) ? 30 : 100;
        return (int)(multiplier * hardness / getBlockBreakingSpeed(state, toolStack));
    }

    protected static boolean canHarvest(@NotNull BlockState state, @NotNull ItemStack toolStack) {
        return !state.isToolRequired() || toolStack.isSuitableFor(state);
    }

    public static float getBlockBreakingSpeed(@NotNull BlockState block,
                                              @NotNull ItemStack toolStack) {
        float speed = toolStack.getMiningSpeedMultiplier(block);
        if (speed > 1.0f) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, toolStack);
            if (i > 0 && !toolStack.isEmpty()) {
                speed += (float) (i * i + 1);
            }
        }
        return speed;
    }

    private static void chargeBlock(@NotNull AbstractDrillingRigBlockEntity<?> blockEntity) {
        var itemStack = blockEntity.toolInventory.getStack(CHARGE_SLOT);
        var item = itemStack.getItem();
        if (!itemStack.isEmpty() && (item instanceof ChargeableItem chargeableItem)) {
            int charge = chargeableItem.getCharge(itemStack);
            if (charge > 0) {
                int transferred = Math.min(
                        blockEntity.block.getEnergyLevel().to,
                        Math.min(charge, blockEntity.energyContainer.getFreeSpace().intValue())
                );
                chargeableItem.discharge(itemStack, transferred);
                blockEntity.energyContainer.add(transferred);
            }
        }
    }

    private static void chargeTool(@NotNull AbstractDrillingRigBlockEntity<?> blockEntity,
                                   @NotNull ItemStack toolItem) {
        var item = toolItem.getItem();
        if (!toolItem.isEmpty() && (item instanceof ChargeableItem chargeableItem)) {
            int freeSpace = chargeableItem.getFreeSpace(toolItem);
            if (freeSpace > 0) {
                int transferred = Math.min(
                        blockEntity.block.getEnergyLevel().to,
                        Math.min(freeSpace, blockEntity.energyContainer.getCharge().intValue())
                );
                chargeableItem.charge(toolItem, transferred);
                blockEntity.energyContainer.subtract(transferred);
            }
        }
    }

    @Nullable
    private static Pair<BlockState, BlockPos> getUnderlineBlock(@NotNull World world, @NotNull Vector3i currentPosition) {
        var blockPos = new BlockPos(currentPosition.x, currentPosition.y, currentPosition.z);
        while (blockPos.getY() > world.getBottomY()) {
            blockPos = blockPos.down();
            var blockState = world.getBlockState(blockPos);
            if(blockState == null || blockState.isAir() || blockState.getHardness(world, blockPos) < 0 ||
                    DrillingRigTags.isDrillingRigIgnore(blockState)) {
                continue;
            }
            if(DrillingRigTags.isDrillingRigSkip(blockState)) {
                return null;
            }
            return new Pair<>(blockState, blockPos);
        }
        return null;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
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
