package com.minecraftquietus.quietus.entity.projectiles.magic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static net.minecraft.data.worldgen.Pools.createKey;
import static net.minecraft.resources.ResourceKey.createRegistryKey;

public class MagicProjRegistration {
    public static final DeferredRegister<EntityType<?>> PROJECTILE_ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE,MODID);

    // ResourceKeys for projectiles
    /*
    public static final ResourceKey<EntityType<?>> FIREBALL =
            createKey("fasd");
    public static final ResourceKey<EntityType<?>> FROST_BOLT =
            createKey("frost_bolt");
    public static final ResourceKey<EntityType<?>> LIGHTNING_SPARK =
            createKey("lightning_spark");*/

    // Base Projectile Type

    public static final Supplier<EntityType<amethystProjectile>> AMETHYST_PROJECTILE =
            registerProj("amethyst_projectile",createKey("amethyst_projectile_key"), amethystProjectile::new, 0.5F, 0.5f);
    /*

    public static final Supplier<EntityType<MagicalProjectile>> MAGIC_PROJECTILE =
            PROJECTILE_ENTITIES.register("magic_projectile", () ->
                    EntityType.Builder.<MagicalProjectile>of(
                            (type, level) -> new MagicalProjectile(type, level) {
                            },
                            MobCategory.MISC
                    ).sized(0.5F, 0.5F).build("magic_projectile")
            );*/


    private static ResourceKey<EntityType<?>> createKey(String name) {
        return ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MODID, name)
        );
    }

    private static <T extends MagicalProjectile> Supplier<EntityType<T>> registerProj(
            String name,
            ResourceKey<EntityType<?>> key,
            EntityType.EntityFactory<T> factory,
            float width, float height
    ) {
        return PROJECTILE_ENTITIES.register(name, () ->
                EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(width, height)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .build(key)
        );
    }

    public static void register(IEventBus bus) {
        PROJECTILE_ENTITIES.register(bus);
    }
}
