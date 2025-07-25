package com.minecraftquietus.quietus.core;

import com.minecraftquietus.quietus.core.skill.Skill;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.function.Supplier;

public class QuietusRegistries {
    
    /* Making the skills registry and key for the skill registry */
    public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "skills"));
    public static final Registry<Skill> SKILL_REGISTRY = 
        new RegistryBuilder<>(SKILL_REGISTRY_KEY)
        .sync(true)
        .defaultKey(ResourceLocation.fromNamespaceAndPath(MODID, "none"))
        .maxId(256)
        .create();

    /* Register SKILL_REGISTRY */
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(SKILL_REGISTRY);
    }
}
