package com.minecraftquietus.quietus.core;

import com.minecraftquietus.quietus.core.skill.Skill;
import com.minecraftquietus.quietus.skilltree.SkillPoint;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusRegistries {
    
    /* Making the skills registry and key for the skill registry */
    public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "skills"));
    public static final Registry<Skill> SKILL_REGISTRY = 
        new RegistryBuilder<>(SKILL_REGISTRY_KEY)
        .sync(true)
        .defaultKey(Identifier.fromNamespaceAndPath(MODID, "none"))
        .maxId(256)
        .create();

    /* Making the SkillTreeNode skill point nodes registry and key for the skill point nodes registry */
    public static final ResourceKey<Registry<SkillPoint>> SKILL_POINT_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "skill_point"));
    public static final Registry<SkillPoint> SKILL_POINT_REGISTRY = 
        new RegistryBuilder<>(SKILL_POINT_REGISTRY_KEY)
        .sync(true)
        .defaultKey(Identifier.fromNamespaceAndPath(MODID, "none"))
        .maxId(1024)
        .create();

    /* Register SKILL_REGISTRY */
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(SKILL_REGISTRY);
        event.register(SKILL_POINT_REGISTRY);
    }

    /* public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SKILL_NODE_REGISTRY_KEY, SkillPoint.CODEC);
    } */
}
