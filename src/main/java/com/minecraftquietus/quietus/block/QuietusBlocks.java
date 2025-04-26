package com.minecraftquietus.quietus.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusBlocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "quietus" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Creates a new Block with the id "quietus:example_block", combining the namespace and path
    // edit 2025.04.24 0.10.42: Kevin Sheng: made the example block higher friction and insta break
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instabreak().friction(1.2f).jumpFactor(2.0f));

    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
