package com.quietus.entity.monster;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** The vanilla skeleton family appearance used by a specialized skeleton role. */
public enum SkeletonAppearance {
    SKELETON,
    STRAY,
    BOGGED,
    PARCHED;

    public static SkeletonAppearance byId(int id) {
        SkeletonAppearance[] values = values();
        return id >= 0 && id < values.length ? values[id] : SKELETON;
    }

    public static SkeletonAppearance fromEntityType(EntityType<?> type) {
        if (type == EntityType.STRAY) {
            return STRAY;
        }
        if (type == EntityType.BOGGED) {
            return BOGGED;
        }
        if (type == EntityType.PARCHED) {
            return PARCHED;
        }
        return SKELETON;
    }

    public static boolean isVanillaSkeletonVariant(EntityType<?> type) {
        return type == EntityType.SKELETON
                || type == EntityType.STRAY
                || type == EntityType.BOGGED
                || type == EntityType.PARCHED;
    }

    /** Mirrors the biomes in which vanilla directly spawns its skeleton variants. */
    public static SkeletonAppearance fromBiome(Holder<Biome> biome) {
        if (biome.is(Biomes.DESERT)) {
            return PARCHED;
        }
        if (biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.ICE_SPIKES)) {
            return STRAY;
        }
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return BOGGED;
        }
        return SKELETON;
    }
}
