package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.item.QuietusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID,bus = EventBusSubscriber.Bus.MOD)
public class DataGenerator {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new QuietusBlockTagProvider(packOutput, lookupProvider);
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new QuietusItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter()));

        generator.addProvider(true, new QuietusModelProvider(packOutput));
       //event.createProvider(QuietusModelProvider::new);

    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new QuietusBlockTagProvider(packOutput, lookupProvider);
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new QuietusItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter()));

        generator.addProvider(true, new QuietusModelProvider(packOutput));

    }
}
