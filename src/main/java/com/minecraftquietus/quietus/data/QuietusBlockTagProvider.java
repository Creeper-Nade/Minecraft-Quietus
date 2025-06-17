package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.Quietus;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class QuietusBlockTagProvider extends BlockTagsProvider {
    public QuietusBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Quietus.MODID);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {

    }
}
