package com.minecraftquietus.quietus.data;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.enchantment.QuietusEnchantments;
import com.minecraftquietus.quietus.util.Damage.QuietusDamageType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class QuietusDatapackProvider extends DatapackBuiltinEntriesProvider {
    public QuietusDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Quietus.MODID));
    }

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, QuietusEnchantments::bootstrap)
            .add(Registries.DAMAGE_TYPE, QuietusDamageType::bootstrap);
}
