package com.quietus.item.tool;

import javax.annotation.Nullable;

import com.quietus.item.property.QuietusProjectileProperty;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

/**
 * General interface for Quietus' ammo items.
 * Uses net.minecraft.world.item.ProjectileItem for the capability 
 * to shoot the projectile from minecraft:dispenser block.
 */
public interface IQuietusAmmoItem extends ProjectileItem {
    public Projectile createProjectile(Level level, ItemStack ammoItem, LivingEntity owner, @Nullable ItemStack firedFromWeapon);

    public QuietusProjectileProperty getProjectileProperty();
}
