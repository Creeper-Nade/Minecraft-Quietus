package com.minecraftquietus.quietus.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
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

    public static class Items {
        //this tag is for pure mage-class weapon
        public static final TagKey<Item> MAGIC_WEAPON= createTag("magic_weapon");
        //this tag is for all weapon that fires custom projectile, which has crit chance (e.g. sword that fires projectile)
        public static final TagKey<Item> PROJECTILE_FIRING_WEAPON= createTag("projectile_firing_weapon");
        //this tag is for any item that consumes mana (e.g. healing wand)
        public static final TagKey<Item> MAGIC_ENCHANTABLE = createTag("magic_enchantable");
        

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, name));
        }
    }

    public static class Entity{
        public static final TagKey<EntityType<?>> MAGIC_PROJECTILE= createTag("magic_projectile");
        public static final TagKey<EntityType<?>> BOSS_MONSTER= createTag("boss_monster");

        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, name));
        }
    }
}
