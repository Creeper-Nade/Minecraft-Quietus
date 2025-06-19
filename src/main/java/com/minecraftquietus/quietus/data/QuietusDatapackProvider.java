package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.enchantment.QuietusEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class QuietusDatapackProvider extends DatapackBuiltinEntriesProvider {
    public QuietusDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Quietus.MODID));
    }

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, QuietusEnchantments::bootstrap);
}
