package com.github.sib_energy_craft.drilling_rig.screen;

import com.github.sib_energy_craft.drilling_rig.block.entity.AbstractDrillingRigBlockEntity;
import com.github.sib_energy_craft.drilling_rig.block.entity.DrillingRigProperties;
import com.github.sib_energy_craft.energy_api.screen.ChargeSlot;
import com.github.sib_energy_craft.energy_api.tags.CoreTags;
import com.github.sib_energy_craft.sec_utils.screen.SlotsScreenHandler;
import com.github.sib_energy_craft.sec_utils.screen.slot.*;
import com.github.sib_energy_craft.sec_utils.utils.TagUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public abstract class AbstractDrillingRigScreenHandler extends SlotsScreenHandler {
    private static final DrillingRigScreenButton[] BUTTONS = DrillingRigScreenButton.values();

    private final Inventory miningInventory;
    private final PropertyDelegate propertyDelegate;
    protected final World world;
    protected final SlotGroupsMeta slotGroupsMeta;
    protected final ScreenHandlerContext context;

    protected AbstractDrillingRigScreenHandler(@NotNull ScreenHandlerType<?> type,
                                               int syncId,
                                               @NotNull PlayerInventory playerInventory) {
        this(type,
                syncId,
                playerInventory,
                new SimpleInventory(2),
                new SimpleInventory(9),
                new ArrayPropertyDelegate(6),
                ScreenHandlerContext.EMPTY
        );
    }

    protected AbstractDrillingRigScreenHandler(@NotNull ScreenHandlerType<?> type,
                                               int syncId,
                                               @NotNull PlayerInventory playerInventory,
                                               @NotNull Inventory toolInventory,
                                               @NotNull Inventory miningInventory,
                                               @NotNull PropertyDelegate propertyDelegate,
                                               @NotNull ScreenHandlerContext context) {
        super(type, syncId);
        checkSize(toolInventory, AbstractDrillingRigBlockEntity.TOOLS_INVENTORY_SIZE);
        checkSize(miningInventory, AbstractDrillingRigBlockEntity.MINING_INVENTORY_SIZE);
        checkDataCount(propertyDelegate, 6);
        this.miningInventory = miningInventory;
        this.propertyDelegate = propertyDelegate;
        this.world = playerInventory.player.world;
        this.context = context;
        this.slotGroupsMeta = buildSlots(playerInventory, toolInventory, miningInventory);
        this.addProperties(propertyDelegate);
    }

    private @NotNull SlotGroupsMeta buildSlots(@NotNull PlayerInventory playerInventory,
                                               @NotNull Inventory toolInventory,
                                               @NotNull Inventory miningInventory) {
        int globalSlotIndex = 0;
        var slotGroupsBuilder = SlotGroupsMetaBuilder.builder();

        int quickAccessSlots = 9;
        {
            var slotQuickAccessGroupBuilder = SlotGroupMetaBuilder.builder(SlotTypes.QUICK_ACCESS);
            for (int i = 0; i < quickAccessSlots; ++i) {
                slotQuickAccessGroupBuilder.addSlot(globalSlotIndex++, i);
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
            }
            var quickAccessSlotGroup = slotQuickAccessGroupBuilder.build();
            slotGroupsBuilder.add(quickAccessSlotGroup);
        }

        {
            var slotPlayerGroupBuilder = SlotGroupMetaBuilder.builder(SlotTypes.PLAYER_INVENTORY);
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    int index = j + i * 9 + quickAccessSlots;
                    slotPlayerGroupBuilder.addSlot(globalSlotIndex++, index);
                    this.addSlot(new Slot(playerInventory, index, 8 + j * 18, 84 + i * 18));
                }
            }
            var playerSlotGroup = slotPlayerGroupBuilder.build();
            slotGroupsBuilder.add(playerSlotGroup);
        }

        {
            var slotToolsGroupBuilder = SlotGroupMetaBuilder.builder(DrillingSlotTypes.TOOLS);

            slotToolsGroupBuilder.addSlot(globalSlotIndex++, AbstractDrillingRigBlockEntity.TOOL_SLOT);
            var toolSlot = new Slot(toolInventory, AbstractDrillingRigBlockEntity.TOOL_SLOT, 80, 17);
            this.addSlot(toolSlot);

            slotToolsGroupBuilder.addSlot(globalSlotIndex++, AbstractDrillingRigBlockEntity.CHARGE_SLOT);
            var chargeSlot = new ChargeSlot(toolInventory, AbstractDrillingRigBlockEntity.CHARGE_SLOT, 80, 53, false);
            this.addSlot(chargeSlot);

            var toolSlotGroup = slotToolsGroupBuilder.build();
            slotGroupsBuilder.add(toolSlotGroup);
        }

        {
            var slotMiningGroupBuilder = SlotGroupMetaBuilder.builder(DrillingSlotTypes.MINING);

            for (int i = 0; i < miningInventory.size(); ++i) {
                slotMiningGroupBuilder.addSlot(globalSlotIndex++, i);
                this.addSlot(new Slot(miningInventory, i, 116 + (i % 3) * 18, 17 + (i / 3) * 18));
            }
            var miningSlotGroup = slotMiningGroupBuilder.build();
            slotGroupsBuilder.add(miningSlotGroup);
        }

        return slotGroupsBuilder.build();
    }

    @Override
    public boolean canUse(@NotNull PlayerEntity player) {
        return this.miningInventory.canPlayerUse(player);
    }

    /**
     * Get charge progress status
     *
     * @return charge progress
     */
    public int getChargeProgress() {
        int i = getCharge();
        int j = getMaxCharge();
        if (j == 0 || i == 0) {
            return 0;
        }
        return i * 13 / j;
    }

    /**
     * Get extractor charge
     *
     * @return charge
     */
    public int getCharge() {
        return propertyDelegate.get(DrillingRigProperties.CHARGE.ordinal());
    }

    /**
     * Get start position
     *
     * @return start position
     */
    public Vector2i getStartPosition() {
        int x = propertyDelegate.get(DrillingRigProperties.START_POSITION_X.ordinal());
        int y = propertyDelegate.get(DrillingRigProperties.START_POSITION_Y.ordinal());
        return new Vector2i(x, y);
    }

    /**
     * Get size
     *
     * @return size
     */
    public Vector2i getSize() {
        int x = propertyDelegate.get(DrillingRigProperties.WIDTH.ordinal());
        int y = propertyDelegate.get(DrillingRigProperties.HEIGHT.ordinal());
        return new Vector2i(x, y);
    }

    /**
     * Get extractor max charge
     *
     * @return max charge
     */
    public int getMaxCharge() {
        return propertyDelegate.get(DrillingRigProperties.MAX_CHARGE.ordinal());
    }

    @NotNull
    @Override
    public ItemStack quickMove(@NotNull PlayerEntity player, int index) {
        var itemStack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasStack()) {
            var slotStack = slot.getStack();
            itemStack = slotStack.copy();
            var slotMeta = this.slotGroupsMeta.getByGlobalSlotIndex(index);
            if(slotMeta != null) {
                var slotType = slotMeta.getSlotType();
                if (slotType == DrillingSlotTypes.TOOLS || slotType == DrillingSlotTypes.MINING) {
                    if (!insertItem(slotGroupsMeta, slotStack, SlotTypes.QUICK_ACCESS, SlotTypes.PLAYER_INVENTORY)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    var toolGlobalIndex = this.slotGroupsMeta.getGlobalIndexByLocal(DrillingSlotTypes.TOOLS,
                            AbstractDrillingRigBlockEntity.TOOL_SLOT);
                    if (!TagUtils.hasTag(ItemTags.PICKAXES, slotStack) || toolGlobalIndex == null ||
                            !insertItem(slotStack, toolGlobalIndex, toolGlobalIndex + 1, false)) {
                        var inserted = false;
                        if (CoreTags.isChargeable(itemStack)) {
                            var chargeGlobalIndex = this.slotGroupsMeta.getGlobalIndexByLocal(DrillingSlotTypes.TOOLS,
                                    AbstractDrillingRigBlockEntity.CHARGE_SLOT);
                            if (chargeGlobalIndex != null &&
                                    insertItem(slotStack, chargeGlobalIndex, chargeGlobalIndex + 1, false)) {
                                inserted = true;
                            }
                        }
                        if (!inserted) {
                            if (slotType == SlotTypes.QUICK_ACCESS) {
                                if (!insertItem(slotGroupsMeta, slotStack, SlotTypes.PLAYER_INVENTORY)) {
                                    return ItemStack.EMPTY;
                                }
                            } else if (slotType == SlotTypes.PLAYER_INVENTORY) {
                                if (!insertItem(slotGroupsMeta, slotStack, SlotTypes.QUICK_ACCESS)) {
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
            }
            slot.onQuickTransfer(slotStack, itemStack);
            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, slotStack);
        }
        return itemStack;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        var rigScreenButton = BUTTONS[id];
        if(rigScreenButton == DrillingRigScreenButton.MOVE_RIGHT) {
            moveLeftRight(false);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.MOVE_LEFT) {
            moveLeftRight(true);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.MOVE_UP) {
            moveUpDown(true);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.MOVE_DOWN) {
            moveUpDown(false);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.WIDTH_UP) {
            changeWidth(true);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.WIDTH_DOWN) {
            changeWidth(false);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.HEIGHT_UP) {
            changeHeight(true);
            return true;
        }
        if(rigScreenButton == DrillingRigScreenButton.HEIGHT_DOWN) {
            changeHeight(false);
            return true;
        }
        return false;
    }

    private void moveLeftRight(boolean moveLeft) {
        var oldPosition = propertyDelegate.get(DrillingRigProperties.START_POSITION_X.ordinal());
        propertyDelegate.set(DrillingRigProperties.START_POSITION_X.ordinal(), moveLeft ? oldPosition - 1 : oldPosition + 1);
        this.context.run((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof AbstractDrillingRigBlockEntity<?> drillingRigBlockEntity) {
                drillingRigBlockEntity.moveLeftRight(moveLeft);
            }
        });
    }

    private void moveUpDown(boolean moveUp) {
        var oldPosition = propertyDelegate.get(DrillingRigProperties.START_POSITION_Y.ordinal());
        propertyDelegate.set(DrillingRigProperties.START_POSITION_Y.ordinal(), moveUp ? oldPosition - 1 : oldPosition + 1);
        this.context.run((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof AbstractDrillingRigBlockEntity<?> drillingRigBlockEntity) {
                drillingRigBlockEntity.moveUpDown(moveUp);
            }
        });
    }

    private void changeWidth(boolean increase) {
        var old = propertyDelegate.get(DrillingRigProperties.WIDTH.ordinal());
        propertyDelegate.set(DrillingRigProperties.WIDTH.ordinal(), increase ? old + 1 : old - 1);
        this.context.run((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof AbstractDrillingRigBlockEntity<?> drillingRigBlockEntity) {
                drillingRigBlockEntity.changeWidth(increase);
            }
        });
    }

    private void changeHeight(boolean increase) {
        var old = propertyDelegate.get(DrillingRigProperties.HEIGHT.ordinal());
        propertyDelegate.set(DrillingRigProperties.HEIGHT.ordinal(), increase ? old + 1 : old - 1);
        this.context.run((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof AbstractDrillingRigBlockEntity<?> drillingRigBlockEntity) {
                drillingRigBlockEntity.changeHeight(increase);
            }
        });
    }

}

