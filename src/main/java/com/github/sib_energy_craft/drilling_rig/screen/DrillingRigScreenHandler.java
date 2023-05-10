package com.github.sib_energy_craft.drilling_rig.screen;

import com.github.sib_energy_craft.drilling_rig.load.Screens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public class DrillingRigScreenHandler extends AbstractDrillingRigScreenHandler {

    public DrillingRigScreenHandler(int syncId,
                                    @NotNull PlayerInventory playerInventory,
                                    @NotNull Inventory toolInventory,
                                    @NotNull Inventory miningInventory,
                                    @NotNull PropertyDelegate propertyDelegate) {
        super(Screens.DRILLING_RIG, syncId, playerInventory, toolInventory, miningInventory, propertyDelegate);
    }

    public DrillingRigScreenHandler(int syncId,
                                    @NotNull PlayerInventory playerInventory,
                                    @NotNull PacketByteBuf packetByteBuf) {
        super(Screens.DRILLING_RIG, syncId, playerInventory);

    }
}
