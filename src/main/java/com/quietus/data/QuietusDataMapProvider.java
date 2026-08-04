package com.quietus.data;

import com.quietus.item.QuietusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

/** Generates NeoForge data-map entries for Quietus gameplay integrations. */
public final class QuietusDataMapProvider extends DataMapProvider {
    public QuietusDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        compostables.add(QuietusItems.MOLD.getKey(), new Compostable(0.65F), false);
        compostables.add(QuietusItems.MOLD_BOWL.getKey(), new Compostable(0.85F), false);
        compostables.add(QuietusItems.MOLD_BUCKET.getKey(), new Compostable(1.0F), false);
    }
}
