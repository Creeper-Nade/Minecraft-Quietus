package com.quietus.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import java.util.concurrent.CompletableFuture;

import static com.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID)
public class QuietusDataGenerator {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new QuietusBlockTagProvider(packOutput, lookupProvider);
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new QuietusItemTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new QuietusEnchantmentTagProvider(packOutput,lookupProvider));
        generator.addProvider(true, new QuietusDamageTypeTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new QuietusDatapackProvider(packOutput, lookupProvider));
        generator.addProvider(true, new QuietusModelProvider(packOutput));
       //event.createProvider(QuietusModelProvider::new);

    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new QuietusBlockTagProvider(packOutput, lookupProvider);
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new QuietusItemTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new QuietusEnchantmentTagProvider(packOutput,lookupProvider));
        generator.addProvider(true, new QuietusDamageTypeTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new QuietusDatapackProvider(packOutput, lookupProvider));
        generator.addProvider(true, new QuietusModelProvider(packOutput));

    }
}
