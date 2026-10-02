package com.quietus.item;

import javax.annotation.Nullable;

import com.quietus.entity.projectiles.IQuietusProjectile;
import com.quietus.item.property.QuietusProjectileProperty;
import com.quietus.item.tool.IQuietusAmmoItem;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QuietusArrowAmmoItem extends ArrowItem implements IQuietusAmmoItem {
    private QuietusProjectileProperty projectileProperty;

    public QuietusArrowAmmoItem(Item.Properties prop) {
        super(prop);
        if (prop instanceof QuietusItemProperties properties) {
            this.projectileProperty = properties.projectileProperty;
        }
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity owner, @Nullable ItemStack firedFromWeapon) {
        Projectile projectile = this.projectileProperty != null && this.projectileProperty.projectileType() != null
                ? this.projectileProperty.projectileType().create(level, EntitySpawnReason.LOAD)
                : null;
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setOwner(owner);
            arrow.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
            if (arrow instanceof IQuietusProjectile quietusProjectile) {
                quietusProjectile.configure(this.projectileProperty, itemStack);
            }
            return arrow;
        }
        return super.createArrow(level, itemStack, owner, firedFromWeapon);
    }

    @Override
    public Projectile createProjectile(Level level, ItemStack ammoItem, LivingEntity owner, @Nullable ItemStack firedFromWeapon) {
        return this.createArrow(level, ammoItem, owner, firedFromWeapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        Projectile projectile = this.projectileProperty != null && this.projectileProperty.projectileType() != null
                ? this.projectileProperty.projectileType().create(level, EntitySpawnReason.LOAD)
                : null;
        if (projectile != null) {
            projectile.setPos(position.x(), position.y(), position.z());
            if (projectile instanceof AbstractArrow arrow) {
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            }
            if (projectile instanceof IQuietusProjectile quietusProjectile) {
                quietusProjectile.configure(this.projectileProperty, itemStack);
            }
            return projectile;
        }
        return super.asProjectile(level, position, itemStack, direction);
    }

    @Override
    public QuietusProjectileProperty getProjectileProperty() {
        return this.projectileProperty;
    }
}
