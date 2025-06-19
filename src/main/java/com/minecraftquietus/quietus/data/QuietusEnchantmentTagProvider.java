package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.enchantment.QuietusEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

public class QuietusEnchantmentTagProvider extends EnchantmentTagsProvider {

    public QuietusEnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(EnchantmentTags.NON_TREASURE)
                .add(QuietusEnchantments.IMPACT)
                .add(QuietusEnchantments.HEX);
        this.tag(EnchantmentTags.TRADES_SWAMP_COMMON)
                .add(QuietusEnchantments.IMPACT)
                .add(QuietusEnchantments.HEX);
    }
}
