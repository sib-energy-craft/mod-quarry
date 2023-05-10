package com.github.sib_energy_craft.drilling_rig.item;

import com.github.sib_energy_craft.drilling_rig.block.AbstractDrillingRigBlock;
import com.github.sib_energy_craft.energy_api.items.ChargeableItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public class DrillingRigItem<T extends AbstractDrillingRigBlock> extends BlockItem implements ChargeableItem {

    public DrillingRigItem(@NotNull T block, @NotNull Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(@NotNull ItemStack stack,
                              @Nullable World world,
                              @NotNull List<Text> tooltip,
                              @NotNull TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        var block = getBlock();
        var maxCharge = block.getMaxCharge();
        var maxInput = block.getEnergyLevel().to;
        var textColor = Color.GRAY.getRGB();
        var textStyle = Style.EMPTY.withColor(textColor);
        tooltip.add(Text.translatable("attribute.name.sib_energy_craft.max_input_eu", maxInput)
                .setStyle(textStyle));
        tooltip.add(Text.translatable("attribute.name.sib_energy_craft.max_charge", maxCharge)
                .setStyle(textStyle));
    }

    @Override
    public AbstractDrillingRigBlock getBlock() {
        return (AbstractDrillingRigBlock) super.getBlock();
    }

    @Override
    public int getMaxCharge() {
        var block = getBlock();
        return block.getMaxCharge();
    }
}