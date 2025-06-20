package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.item.QuietusItems;
import com.minecraftquietus.tags.QuietusTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class QuietusItemTagProvider extends ItemTagsProvider {
    public QuietusItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, Quietus.MODID);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(QuietusItems.COPPER_HELMET.get())
                .add(QuietusItems.COPPER_CHESTPLATE.get())
                .add(QuietusItems.COPPER_LEGGINGS.get())
                .add(QuietusItems.COPPER_BOOTS.get())

                .add(QuietusItems.EXPOSED_COPPER_HELMET.get())
                .add(QuietusItems.EXPOSED_COPPER_CHESTPLATE.get())
                .add(QuietusItems.EXPOSED_COPPER_LEGGINGS.get())
                .add(QuietusItems.EXPOSED_COPPER_BOOTS.get())

                .add(QuietusItems.WEATHERED_COPPER_HELMET.get())
                .add(QuietusItems.WEATHERED_COPPER_CHESTPLATE.get())
                .add(QuietusItems.WEATHERED_COPPER_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_COPPER_BOOTS.get())

                .add(QuietusItems.OXIDIZED_COPPER_HELMET.get())
                .add(QuietusItems.OXIDIZED_COPPER_CHESTPLATE.get())
                .add(QuietusItems.OXIDIZED_COPPER_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_COPPER_BOOTS.get())

                .add(QuietusItems.EXPOSED_IRON_HELMET.get())
                .add(QuietusItems.EXPOSED_IRON_CHESTPLATE.get())
                .add(QuietusItems.EXPOSED_IRON_LEGGINGS.get())
                .add(QuietusItems.EXPOSED_IRON_BOOTS.get())

                .add(QuietusItems.WEATHERED_IRON_HELMET.get())
                .add(QuietusItems.WEATHERED_IRON_CHESTPLATE.get())
                .add(QuietusItems.WEATHERED_IRON_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_IRON_BOOTS.get())

                .add(QuietusItems.OXIDIZED_IRON_HELMET.get())
                .add(QuietusItems.OXIDIZED_IRON_CHESTPLATE.get())
                .add(QuietusItems.OXIDIZED_IRON_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_IRON_BOOTS.get())

                .add(QuietusItems.AMETHYST_HELMET.get())
                .add(QuietusItems.AMETHYST_CHESTPLATE.get())
                .add(QuietusItems.AMETHYST_LEGGINGS.get())
                .add(QuietusItems.AMETHYST_BOOTS.get());

        this.tag(ItemTags.FOOT_ARMOR)
                .add(QuietusItems.COPPER_BOOTS.get())
                .add(QuietusItems.EXPOSED_COPPER_BOOTS.get())
                .add(QuietusItems.WEATHERED_COPPER_BOOTS.get())
                .add(QuietusItems.OXIDIZED_COPPER_BOOTS.get())
                .add(QuietusItems.EXPOSED_IRON_BOOTS.get())
                .add(QuietusItems.WEATHERED_IRON_BOOTS.get())
                .add(QuietusItems.OXIDIZED_IRON_BOOTS.get())
                .add(QuietusItems.AMETHYST_BOOTS.get());

        this.tag(ItemTags.LEG_ARMOR)
                .add(QuietusItems.COPPER_LEGGINGS.get())
                .add(QuietusItems.EXPOSED_COPPER_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_COPPER_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_COPPER_LEGGINGS.get())
                .add(QuietusItems.EXPOSED_IRON_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_IRON_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_IRON_LEGGINGS.get())
                .add(QuietusItems.AMETHYST_LEGGINGS.get());

        this.tag(ItemTags.HEAD_ARMOR)
                .add(QuietusItems.COPPER_HELMET.get())
                .add(QuietusItems.EXPOSED_COPPER_HELMET.get())
                .add(QuietusItems.WEATHERED_COPPER_HELMET.get())
                .add(QuietusItems.OXIDIZED_COPPER_HELMET.get())
                .add(QuietusItems.EXPOSED_IRON_HELMET.get())
                .add(QuietusItems.WEATHERED_IRON_HELMET.get())
                .add(QuietusItems.OXIDIZED_IRON_HELMET.get())
                .add(QuietusItems.AMETHYST_HELMET.get());

        this.tag(ItemTags.CHEST_ARMOR)
                .add(QuietusItems.COPPER_CHESTPLATE.get())
                .add(QuietusItems.EXPOSED_COPPER_CHESTPLATE.get())
                .add(QuietusItems.WEATHERED_COPPER_CHESTPLATE.get())
                .add(QuietusItems.OXIDIZED_COPPER_CHESTPLATE.get())
                .add(QuietusItems.EXPOSED_IRON_CHESTPLATE.get())
                .add(QuietusItems.WEATHERED_IRON_CHESTPLATE.get())
                .add(QuietusItems.OXIDIZED_IRON_CHESTPLATE.get())
                .add(QuietusItems.AMETHYST_CHESTPLATE.get());

        this.tag(ItemTags.BOW_ENCHANTABLE)
                .add(QuietusItems.TRIPLEBOW.get())
                .add(QuietusItems.INSTABOW.get())
                .add(QuietusItems.INFINIBOW.get());

        this.tag(QuietusTags.Items.MAGIC_WEAPON)
                .add(QuietusItems.AMETHYST_STAFF.get());

        this.tag(QuietusTags.Items.PROJECTILE_FIRING_WEAPON)
                .addTag(QuietusTags.Items.MAGIC_WEAPON);

        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(QuietusTags.Items.PROJECTILE_FIRING_WEAPON);

        this.tag(QuietusTags.Items.MAGIC_ENCHANTABLE)
                .addTag(QuietusTags.Items.MAGIC_WEAPON);
    }
}
