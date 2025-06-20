package com.minecraftquietus.quietus.util.Damage;

import com.minecraftquietus.quietus.Quietus;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.item.enchantment.Enchantment;

public class QuietusDamageType {
    public static final ResourceKey<DamageType> MAGIC_PROJECTILE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Quietus.MODID, "magic_projectile_damage"));
    public static final ResourceKey<DamageType> MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Quietus.MODID, "magic_projectile_damage_bypass_invincible_frame"));

    public static void bootstrap(BootstrapContext<DamageType> context)
    {
        register(context,MAGIC_PROJECTILE_DAMAGE,new DamageType(MAGIC_PROJECTILE_DAMAGE.location().toString(),
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1f,
                DamageEffects.HURT,
                DeathMessageType.DEFAULT));
        register(context,MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME,new DamageType(MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME.location().toString(),
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1f,
                DamageEffects.HURT,
                DeathMessageType.DEFAULT));
    }

    private static void register(BootstrapContext<DamageType> registry,ResourceKey<DamageType> dmg, DamageType damageType) {
        registry.register(dmg, damageType);
    }
}
