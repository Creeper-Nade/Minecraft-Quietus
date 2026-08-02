package com.quietus.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.quietus.combat.ProjectileVolleyBalance;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Covers vanilla Multishot and weapons that use the vanilla firing routine. */
@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
    @ModifyExpressionValue(
            method = "shoot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;"
            )
    )
    private Projectile quietus$applyVolleyBudget(Projectile projectile,
                                                  @Local(argsOnly = true) List<ItemStack> projectiles) {
        return ProjectileVolleyBalance.apply(projectile, ProjectileVolleyBalance.countProjectiles(projectiles));
    }
}
