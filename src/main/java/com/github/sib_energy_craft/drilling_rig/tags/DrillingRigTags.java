package com.github.sib_energy_craft.drilling_rig.tags;

import com.github.sib_energy_craft.energy_api.utils.Identifiers;
import com.github.sib_energy_craft.sec_utils.utils.TagUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.0.1
 * @author sibmaks
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrillingRigTags {
    /**
     * Drilling rig must ignore blocks tagged with this tag when looking for a block to mine
     */
    private static final TagKey<Block> DRILLING_RIG_IGNORE;
    /**
     * Drilling rig should skip position to mine in case if the underlying block is tagged with this tag
     */
    private static final TagKey<Block> DRILLING_RIG_SKIP;

    static {
        DRILLING_RIG_IGNORE = TagKey.of(RegistryKeys.BLOCK, Identifiers.of("drilling_rig_ignore"));
        DRILLING_RIG_SKIP = TagKey.of(RegistryKeys.BLOCK, Identifiers.of("drilling_rig_skip"));
    }

    /**
     * Check is block need to ignore on drilling rig mine
     *
     * @param blockState block to check
     * @return true - not mine block, false - otherwise
     */
    public static boolean isDrillingRigIgnore(@NotNull BlockState blockState) {
        return TagUtils.hasTag(DRILLING_RIG_IGNORE, blockState);
    }

    /**
     * Check is block need to skip on drilling rig mine
     *
     * @param blockState block to check
     * @return true - skip, false - otherwise
     */
    public static boolean isDrillingRigSkip(@NotNull BlockState blockState) {
        return TagUtils.hasTag(DRILLING_RIG_SKIP, blockState);
    }
}
