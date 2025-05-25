package com.minecraftquietus.quietus.item.weapons;

import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.minecraftquietus.quietus.util.mana.ManaComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MagicalWeapon<T extends Projectile> extends Item {
    private final Supplier<EntityType<T>> projectileType;
    private final int manaCost;
    private final int cooldown;
    private final float velocity;

    public MagicalWeapon(Properties properties, Supplier<EntityType<T>> projectileType,
                          int manaCost, int cooldown, float damage, float velocity) {
        super(properties);
        this.projectileType = projectileType;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.velocity = velocity;
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
        projectile.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
        //projectile.setDamage(damage);
        return projectile;
    }

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
