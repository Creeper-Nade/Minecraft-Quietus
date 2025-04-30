package com.minecraftquietus.quietus.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusTags {
    public static class Blocks {
        public static final TagKey<Block> SPELUNKABLE_ORES = createTag("spelunkable_ores");
        /*public static final TagKey<Block> SPELUNKABLE_ORES = TagKey.create(
                // The registry key. The type of the registry must match the generic type of the tag.
                Registries.BLOCK,
                // The location of the tag. This example will put our tag at data/examplemod/tags/blocks/example_tag.json.
                ResourceLocation.fromNamespaceAndPath(MODID, "spelunkable_ores")
        );*/

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, name));
        }
    }
}
