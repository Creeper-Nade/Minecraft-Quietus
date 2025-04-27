package com.minecraftquietus.quietus.effects.spelunker;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class OreColorRegistry {
    // Default colors for common ores (customize as needed)
    private static final Map<Block, Integer> ORE_COLORS = new HashMap<>();
    static {
        ORE_COLORS.put(Blocks.COAL_ORE, 0x363636);    // Dark gray
        ORE_COLORS.put(Blocks.DEEPSLATE_COAL_ORE, 0x363636);
        ORE_COLORS.put(Blocks.GOLD_ORE, 0xFFD700);    // Gold
        ORE_COLORS.put(Blocks.DEEPSLATE_GOLD_ORE, 0xFFD700);
        ORE_COLORS.put(Blocks.NETHER_GOLD_ORE, 0xFFD700);
        ORE_COLORS.put(Blocks.COPPER_ORE,0xE77B57);
        ORE_COLORS.put(Blocks.DEEPSLATE_COPPER_ORE,0xE77B57);
        ORE_COLORS.put(Blocks.EMERALD_ORE,0x41F384);
        ORE_COLORS.put(Blocks.DEEPSLATE_EMERALD_ORE,0x41F384);
        ORE_COLORS.put(Blocks.IRON_ORE,0xFEDEC8);
        ORE_COLORS.put(Blocks.DEEPSLATE_IRON_ORE,0xFEDEC8);
        ORE_COLORS.put(Blocks.LAPIS_ORE,0x5A82E2);
        ORE_COLORS.put(Blocks.DEEPSLATE_LAPIS_ORE,0x5A82E2);
        ORE_COLORS.put(Blocks.REDSTONE_ORE,0xFF0000);
        ORE_COLORS.put(Blocks.DEEPSLATE_REDSTONE_ORE,0xFF0000);
        ORE_COLORS.put(Blocks.NETHER_QUARTZ_ORE,0xFFFFFF);
        ORE_COLORS.put(Blocks.DIAMOND_ORE, 0x00FFFF);  // Cyan
        ORE_COLORS.put(Blocks.DEEPSLATE_DIAMOND_ORE, 0x00FFFF);
        ORE_COLORS.put(Blocks.ANCIENT_DEBRIS,0x7E6059);
        // Add modded ores here
    }

    // Get color for a block (default: red)
    public static int getColor(Block block) {
        return ORE_COLORS.getOrDefault(block, 0xFF0000);
    }
}
