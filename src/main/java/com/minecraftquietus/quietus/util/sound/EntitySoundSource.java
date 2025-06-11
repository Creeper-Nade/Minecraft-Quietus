package com.minecraftquietus.quietus.util.sound;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.LivingEntity;


public final class EntitySoundSource {
    final static EntityType<?>[] PLAYER = {EntityType.PLAYER};
    final static EntityType<?>[] HOSTILE_MOBS = {
        EntityType.BLAZE,
        EntityType.BOGGED,
        EntityType.BREEZE,
        EntityType.CAVE_SPIDER,
        EntityType.CREAKING,
        EntityType.CREEPER,
        EntityType.DROWNED,
        EntityType.ELDER_GUARDIAN,
        EntityType.ENDER_DRAGON,
        EntityType.ENDERMAN,
        EntityType.ENDERMITE,
        EntityType.EVOKER,
        EntityType.GHAST,
        EntityType.GIANT,
        EntityType.GUARDIAN,
        EntityType.HOGLIN,
        EntityType.HUSK,
        EntityType.ILLUSIONER,
        EntityType.MAGMA_CUBE,
        EntityType.PHANTOM,
        EntityType.PIGLIN,
        EntityType.PIGLIN_BRUTE,
        EntityType.PILLAGER,
        EntityType.RAVAGER,
        EntityType.SHULKER,
        EntityType.SILVERFISH,
        EntityType.SKELETON,
        EntityType.SLIME,
        EntityType.SPIDER,
        EntityType.STRAY,
        EntityType.VEX,
        EntityType.VINDICATOR,
        EntityType.WARDEN,
        EntityType.WITCH,
        EntityType.WITHER,
        EntityType.WITHER_SKELETON,
        EntityType.ZOGLIN,
        EntityType.ZOMBIE,
        EntityType.ZOMBIE_VILLAGER,
        EntityType.ZOMBIFIED_PIGLIN
    };
    final static EntityType<?>[] NEUTRAL_MOBS = {
        EntityType.ARMADILLO,
        EntityType.AXOLOTL,
        EntityType.BEE,
        EntityType.CAMEL,
        EntityType.CAT,
        EntityType.CHICKEN,
        EntityType.COD,
        EntityType.COW,
        EntityType.DOLPHIN,
        EntityType.DONKEY,
        EntityType.FOX,
        EntityType.FROG,
        EntityType.GLOW_SQUID,
        EntityType.GOAT,
        EntityType.HOGLIN,
        EntityType.HORSE,
        EntityType.LLAMA,
        EntityType.MOOSHROOM,
        EntityType.MULE,
        EntityType.OCELOT,
        EntityType.PANDA,
        EntityType.PARROT,
        EntityType.PIG,
        EntityType.POLAR_BEAR,
        EntityType.PUFFERFISH,
        EntityType.RABBIT,
        EntityType.SALMON,
        EntityType.SHEEP,
        EntityType.SKELETON_HORSE,
        EntityType.SNIFFER,
        EntityType.SQUID,
        EntityType.STRIDER,
        EntityType.TADPOLE,
        EntityType.TRADER_LLAMA,
        EntityType.TROPICAL_FISH,
        EntityType.TURTLE,
        EntityType.WOLF,
        EntityType.ZOMBIE_HORSE
    };
    
    public static SoundSource of(LivingEntity entity) {
        for (EntityType<?> type : PLAYER) {
            if (entity.getType().equals(type)) return SoundSource.PLAYERS;
        }
        for (EntityType<?> type : HOSTILE_MOBS) {
            if (entity.getType().equals(type)) return SoundSource.HOSTILE;
        }
        for (EntityType<?> type : NEUTRAL_MOBS) {
            if (entity.getType().equals(type)) return SoundSource.NEUTRAL;
        }
        return SoundSource.PLAYERS;
    }
}
