package com.quietus.data;

import com.quietus.Quietus;
import com.quietus.item.QuietusItems;
import com.quietus.tags.QuietusTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class QuietusItemTagProvider extends ItemTagsProvider {
    public QuietusItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Quietus.MODID);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(QuietusTags.Items.AMETHYST_UPGRADE_HELMET_BASES)
                .add(net.minecraft.world.item.Items.IRON_HELMET)
                .add(QuietusItems.EXPOSED_IRON_HELMET.get())
                .add(QuietusItems.WEATHERED_IRON_HELMET.get())
                .add(QuietusItems.OXIDIZED_IRON_HELMET.get());

        this.tag(QuietusTags.Items.AMETHYST_UPGRADE_CHESTPLATE_BASES)
                .add(net.minecraft.world.item.Items.IRON_CHESTPLATE)
                .add(QuietusItems.EXPOSED_IRON_CHESTPLATE.get())
                .add(QuietusItems.WEATHERED_IRON_CHESTPLATE.get())
                .add(QuietusItems.OXIDIZED_IRON_CHESTPLATE.get());

        this.tag(QuietusTags.Items.AMETHYST_UPGRADE_LEGGINGS_BASES)
                .add(net.minecraft.world.item.Items.IRON_LEGGINGS)
                .add(QuietusItems.EXPOSED_IRON_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_IRON_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_IRON_LEGGINGS.get());

        this.tag(QuietusTags.Items.AMETHYST_UPGRADE_BOOTS_BASES)
                .add(net.minecraft.world.item.Items.IRON_BOOTS)
                .add(QuietusItems.EXPOSED_IRON_BOOTS.get())
                .add(QuietusItems.WEATHERED_IRON_BOOTS.get())
                .add(QuietusItems.OXIDIZED_IRON_BOOTS.get());

        this.tag(ItemTags.TRIMMABLE_ARMOR)

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
                .add(QuietusItems.EXPOSED_COPPER_BOOTS.get())
                .add(QuietusItems.WEATHERED_COPPER_BOOTS.get())
                .add(QuietusItems.OXIDIZED_COPPER_BOOTS.get())
                .add(QuietusItems.EXPOSED_IRON_BOOTS.get())
                .add(QuietusItems.WEATHERED_IRON_BOOTS.get())
                .add(QuietusItems.OXIDIZED_IRON_BOOTS.get())
                .add(QuietusItems.AMETHYST_BOOTS.get());

        this.tag(ItemTags.LEG_ARMOR)
                .add(QuietusItems.EXPOSED_COPPER_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_COPPER_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_COPPER_LEGGINGS.get())
                .add(QuietusItems.EXPOSED_IRON_LEGGINGS.get())
                .add(QuietusItems.WEATHERED_IRON_LEGGINGS.get())
                .add(QuietusItems.OXIDIZED_IRON_LEGGINGS.get())
                .add(QuietusItems.AMETHYST_LEGGINGS.get());

        this.tag(ItemTags.HEAD_ARMOR)
                .add(QuietusItems.EXPOSED_COPPER_HELMET.get())
                .add(QuietusItems.WEATHERED_COPPER_HELMET.get())
                .add(QuietusItems.OXIDIZED_COPPER_HELMET.get())
                .add(QuietusItems.EXPOSED_IRON_HELMET.get())
                .add(QuietusItems.WEATHERED_IRON_HELMET.get())
                .add(QuietusItems.OXIDIZED_IRON_HELMET.get())
                .add(QuietusItems.AMETHYST_HELMET.get());

        this.tag(ItemTags.CHEST_ARMOR)
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

        this.tag(QuietusTags.Items.GRAPPLING_HOOK)
                .add(QuietusItems.CHAIN_GRAPPLING_HOOK.get());

        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(QuietusTags.Items.PROJECTILE_FIRING_WEAPON)
                .addTag(QuietusTags.Items.GRAPPLING_HOOK);

        this.tag(ItemTags.VANISHING_ENCHANTABLE)
                .addTag(QuietusTags.Items.GRAPPLING_HOOK);

        this.tag(QuietusTags.Items.MAGIC_ENCHANTABLE)
                .addTag(QuietusTags.Items.MAGIC_WEAPON);
    }
}
