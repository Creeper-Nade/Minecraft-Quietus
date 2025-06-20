package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.enchantment.QuietusEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class QuietusEnchantmentTagProvider extends EnchantmentTagsProvider {

    public QuietusEnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(EnchantmentTags.NON_TREASURE)
                .add(QuietusEnchantments.IMPACT)
                .add(QuietusEnchantments.HEX)
                .add(QuietusEnchantments.ACUPUNCTURE)
                .add(QuietusEnchantments.CONSERVATION);
        this.tag(EnchantmentTags.TRADES_SWAMP_COMMON)
                .add(QuietusEnchantments.HEX);
        this.tag(EnchantmentTags.TRADES_SAVANNA_COMMON)
                .add(QuietusEnchantments.ACUPUNCTURE);
        this.tag(EnchantmentTags.TRADES_SNOW_COMMON)
                .add(QuietusEnchantments.IMPACT);
        this.tag(EnchantmentTags.TRADES_SWAMP_SPECIAL)
                .add(QuietusEnchantments.CONSERVATION);
    }
}
