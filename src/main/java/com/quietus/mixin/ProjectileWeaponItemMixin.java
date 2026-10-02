package com.quietus.mixin;

import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.quietus.combat.ProjectileVolleyBalance;
import com.quietus.item.property.QuietusProjectileProperty;
import com.quietus.item.tool.IQuietusAmmoItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
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

    @WrapOperation(
            method = "shoot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;"
            )
    )
    private Projectile quietus$applyAmmoVelocityMult(
            Projectile projectileEntity,
            ServerLevel level,
            ItemStack projectileStack,
            Consumer<Projectile> shootAction,
            Operation<Projectile> original
    ) {
        Consumer<Projectile> effectiveShootAction = shootAction;
        if (((Object) this instanceof BowItem || (Object) this instanceof CrossbowItem)
                && projectileStack.getItem() instanceof IQuietusAmmoItem quietusAmmoItem) {
            QuietusProjectileProperty property = quietusAmmoItem.getProjectileProperty();
            if (property != null) {
                float velocityMult = property.velocityMult();
                if (velocityMult != 1.0F) {
                    effectiveShootAction = spawned -> {
                        shootAction.accept(spawned);
                        spawned.setDeltaMovement(spawned.getDeltaMovement().scale(velocityMult));
                    };
                }
            }
        }
        return original.call(projectileEntity, level, projectileStack, effectiveShootAction);
    }
}

