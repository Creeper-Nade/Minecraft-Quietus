package com.minecraftquietus.quietus.item.weapons;

import com.minecraftquietus.quietus.entity.projectiles.magic.MagicalProjectile;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MagicalWeapon<T extends MagicalProjectile> extends Item {
    private final Supplier<EntityType<T>> projectileType;
    private final int manaCost;
    private final int cooldown;
    private final float velocity;
    private final float gravity ;
    private final float knockback;
    private final float base_damage;
    private final int life_span;


    public MagicalWeapon(Properties properties, Supplier<EntityType<T>> projectileType,
                         int manaCost, int cooldown, float velocity,
                         float gravity, float knockback, float base_damage,
                         int life_span) {
        super(properties);
        this.projectileType = projectileType;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.velocity = velocity;
        this.gravity = gravity;
        this.knockback = knockback;
        this.base_damage= base_damage;
        this.life_span= life_span;
    }


    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }


    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Server-side logic
        if(player instanceof ServerPlayer serverPlayer)
        {
            if (player.getCooldowns().isOnCooldown(stack) || !hasEnoughMana(player)) {
                return InteractionResult.FAIL;
            }
            // Spawn projectile
            T projectile = createProjectile(level, player);
            fireProjectile(level, projectile, player);
            // Consume resources
            postFireActions(serverPlayer, stack);
        }
        // Return result with server-side animation
        return InteractionResult.SUCCESS_SERVER;
    }

    protected T createProjectile(Level level, LivingEntity shooter) {
        T projectile = projectileType.get().create(level, EntitySpawnReason.LOAD);
        projectile.setOwner(shooter);
        projectile.setPos(shooter.getEyePosition());
        projectile.ConfigProjectile(gravity,knockback,base_damage,life_span);
        //projectile.setDamage(damage);
        return projectile;
    }

/*
    protected void configureProjectile(T projectile, LivingEntity shooter, ItemStack staff) {
        projectile.setOwner(shooter);
        projectile.setPos(shooter.getEyePosition());
        projectile.setGravity(gravity);
        projectile.setKnockback(knockback);
        projectile.setInitialVelocity(velocity);
        projectile.setBase_damage(base_damage);
        configurator.configure(projectile, shooter, staff);
    }*/

    protected void fireProjectile(Level level,T projectile, LivingEntity shooter) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(),
                0.0F, velocity, 1.0F);
        level.addFreshEntity(projectile);
    }

    protected void postFireActions(ServerPlayer player, ItemStack stack) {
        player.getData(QuietusAttachments.MANA_ATTACHMENT).RemoveMana(manaCost, player);
        player.getCooldowns().addCooldown(stack, cooldown);
        /*
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.5F);*/
    }


    private boolean hasEnoughMana(Player player) {
        // Access your mana capability here
        return  player.getData(QuietusAttachments.MANA_ATTACHMENT).getMana() >= manaCost;
    }

}
