package com.quietus.entity.projectiles;

import com.quietus.entity.projectiles.magic.SmallAmethystShardProjectile;
import com.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.quietus.entity.projectiles.misc.grapples.ChainGrapplingHookProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import com.quietus.entity.projectiles.magic.AmethystShardProjectile;

import static com.quietus.Quietus.MODID;
/* import static net.minecraft.data.worldgen.Pools.createKey;
import static net.minecraft.resources.ResourceKey.createRegistryKey; */

public class QuietusProjectiles {
    public static final DeferredRegister<EntityType<?>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE,MODID);

    // ResourceKeys for projectiles
    /*
    public static final ResourceKey<EntityType<?>> FIREBALL =
            createKey("fasd");
    public static final ResourceKey<EntityType<?>> FROST_BOLT =
            createKey("frost_bolt");
    public static final ResourceKey<EntityType<?>> LIGHTNING_SPARK =
            createKey("lightning_spark");*/

    // Base Projectile Type

    public static final Supplier<EntityType<AmethystShardProjectile>> AMETHYST_PROJECTILE =
            registerProjectile("amethyst_projectile", createKey("amethyst_projectile"), AmethystShardProjectile::new, 0.5F, 0.5f);
    public static final Supplier<EntityType<SmallAmethystShardProjectile>> SMALL_AMETHYST_PROJECTILE =
            registerProjectile("small_amethyst_projectile", createKey("small_amethyst_projectile"), SmallAmethystShardProjectile::new, 0.3F, 0.3f);
    public static final Supplier<EntityType<GrapplingHookProjectile>> CHAIN_GRAPPLING_HOOK_PROJECTILE =
            registerGrappleProjectile("chain_grappling_hook_projectile", createKey("chain_grappling_hook_projectile"), ChainGrapplingHookProjectile::new, 0.8F, 0.8f,0.5f);
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
                Identifier.fromNamespaceAndPath(MODID, name)
        );
    }

    private static <T extends QuietusProjectile> Supplier<EntityType<T>> registerProjectile(
            String name,
            ResourceKey<EntityType<?>> key,
            EntityType.EntityFactory<T> factory,
            float width, float height
    ) {
        return REGISTRAR.register(name, () ->
                EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(width, height)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(key)
        );
    }
    private static <T extends GrapplingHookProjectile> Supplier<EntityType<T>> registerGrappleProjectile(
            String name,
            ResourceKey<EntityType<?>> key,
            EntityType.EntityFactory<T> factory,
            float width, float height, float eyeHeight
    ) {
        return REGISTRAR.register(name, () ->
                EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(width, height)
                        .eyeHeight(eyeHeight)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(key)
        );
    }

    public static void register(IEventBus bus) {
        REGISTRAR.register(bus);
    }
}
