package com.minecraftquietus.quietus.entity;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.function.Supplier;

import com.minecraftquietus.quietus.entity.monster.Bowslinger;
import com.minecraftquietus.quietus.entity.monster.Paraboler;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class QuietusEntityTypes {

    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(MODID);

    public static final Supplier<EntityType<Bowslinger>> BOWSLINGER = ENTITY_TYPES.register("bowslinger", () -> EntityType.Builder.of(Bowslinger::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F).ridingOffset(-0.7F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "bowslinger"))));
    public static final Supplier<EntityType<Paraboler>> PARABOLER = ENTITY_TYPES.register("paraboler", () -> EntityType.Builder.of(Paraboler::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F).ridingOffset(-0.7F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "paraboler"))));


    public static void register (IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
