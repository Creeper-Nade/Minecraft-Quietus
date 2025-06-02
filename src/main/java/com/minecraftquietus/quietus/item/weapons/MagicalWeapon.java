package com.minecraftquietus.quietus.item.weapons;

import com.minecraftquietus.quietus.entity.projectiles.magic.MagicalProjectile;
import com.minecraftquietus.quietus.item.QuietusItemProperties;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class MagicalWeapon<T extends MagicalProjectile> extends Item {
    private final Supplier<EntityType<T>> projectileType;
    private final float velocity;
    private final float gravity;
    private final float knockback;
    private final float base_damage;
    private final double base_crit_chance;
    private final int life_span;
    private final SoundEvent sound;


    public MagicalWeapon(Item.Properties properties, Supplier<EntityType<T>> projectileType,
                         float velocity,
                         float gravity, float knockback, float base_damage,
                         int life_span, double base_crit_chance, SoundEvent sound) {
        super((Item.Properties)properties);
        this.projectileType = projectileType;
        this.velocity = velocity;
        this.gravity = gravity;
        this.knockback = knockback;
        this.base_damage= base_damage;
        this.life_span= life_span;
        this.base_crit_chance=base_crit_chance;
        this.sound=sound;
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
        Vec3 eyepos= shooter.getEyePosition();
        projectile.setOwner(shooter);
        projectile.setPos(eyepos.x,eyepos.y-0.1,eyepos.z);
        projectile.ConfigProjectile(gravity,knockback,base_damage,life_span,base_crit_chance);
        //projectile.setDamage(damage);
        return projectile;
    }


    protected void fireProjectile(Level level,T projectile, LivingEntity shooter) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(),
                0.0F, velocity, 0.5F);
        level.addFreshEntity(projectile);
    }

    protected void postFireActions(ServerPlayer player, ItemStack stack) {
        // /player.getData(QuietusAttachments.MANA_ATTACHMENT).removeMana(manaCost, player);
        //player.getCooldowns().addCooldown(stack, cooldown);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }



}
