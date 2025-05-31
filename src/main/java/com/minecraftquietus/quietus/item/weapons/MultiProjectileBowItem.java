package com.minecraftquietus.quietus.item.weapons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.item.QuietusItemProperties;
import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class MultiProjectileBowItem extends ProjectileWeaponItem {

    protected final int projectilesPerShot;
    protected final int projectileRange = 15;
    protected final TriFunction<Float,Integer,RandomSource,Float> xRotCalc;
    protected final TriFunction<Float,Integer,RandomSource,Float> yRotCalc;

    public MultiProjectileBowItem(Item.Properties property) {
        super(property);
        if (property instanceof QuietusItemProperties prop) {
            this.projectilesPerShot = prop.projectilesPerShot;
            System.out.println(prop.projectilesPerShot);
            this.xRotCalc = prop.rotOffsetCalc[0];
            this.yRotCalc = prop.rotOffsetCalc[1];
        } else {
            this.projectilesPerShot = 1;
            this.xRotCalc = (rotX, index, random) -> rotX;
            this.yRotCalc = (rotY, index, random) -> rotY;
        }
        
        
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        } else {
            int useTime = this.getUseDuration(stack, entity) - timeLeft;
            useTime = net.neoforged.neoforge.event.EventHooks.onArrowLoose(stack, level, player, useTime, !player.getProjectile(stack).isEmpty());
            if (useTime < 0) return false;
            float f = getPowerForTime(useTime);
            if (f < 0.1) {
                return false;
            } else {
                int projectiles = this.projectilesPerShot;
                List<ItemStack> list = new ArrayList<>(this.projectilesPerShot);
                for (int i = 0; i < projectiles; i++) {
                    ItemStack supportedAmmoStack = player.getProjectile(stack);
                    if (supportedAmmoStack.isEmpty()) {
                        return false;
                    }
                    list.addAll(draw(stack, supportedAmmoStack, player));
                }
                if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                    this.shoot(serverlevel, player, player.getUsedItemHand(), stack, list, f * 3.0F, 1.0F, f == 1.0F, null);
                }

                level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ARROW_SHOOT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F
                );
                player.awardStat(Stats.ITEM_USED.get(this));
                return true;
            }
        }
    }

    @Override
    protected void shoot(
        ServerLevel level,
        LivingEntity shooter,
        InteractionHand hand,
        ItemStack weapon,
        List<ItemStack> projectileItems,
        float velocity,
        float inaccuracy,
        boolean isCrit,
        @Nullable LivingEntity target
    ) {
        float f = EnchantmentHelper.processProjectileSpread(level, weapon, shooter, 0.0F);
        float f1 = projectileItems.size() == 1 ? 0.0F : 2.0F * f / (projectileItems.size() - 1);
        float f2 = (projectileItems.size() - 1) % 2 * f1 / 2.0F;
        float f3 = 1.0F;
        for (int i = 0; i < projectileItems.size(); i++) {
            ItemStack projectileItem = projectileItems.get(i);
            if (!projectileItem.isEmpty()) {
                float f4 = f2 + f3 * ((i + 1) / 2) * f1;
                f3 = -f3;
                int index = i;
                Projectile.spawnProjectile(
                    this.createProjectile(level, shooter, weapon, projectileItem, isCrit),
                    level,
                    projectileItem,
                    projectile -> this.shootProjectile(shooter, projectile, index, velocity, inaccuracy, f4, target)
                );
                weapon.hurtAndBreak(this.getDurabilityUse(projectileItem), shooter, LivingEntity.getSlotForHand(hand));
                if (weapon.isEmpty()) {
                    break;
                }
            }
        }
    }

    @Override
    protected void shootProjectile(
        LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target
    ) {
        projectile.shootFromRotation(shooter, this.xRotCalc.apply(shooter.getXRot(),index,shooter.getRandom()), this.yRotCalc.apply(shooter.getYRot() + angle,index,shooter.getRandom()), 0.0F, velocity, inaccuracy);
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getPowerForTime(int charge) {
        float f = charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack p_40678_) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        int[] projectileItemCount = {0};
        Consumer<ItemStack> projectileItemAddUp = (stack) -> {
            if (getAllSupportedProjectiles().test(stack)) projectileItemCount[0] += stack.getCount();
        };
        player.getInventory().forEach(projectileItemAddUp);
        boolean flag = projectileItemCount[0] >= this.projectilesPerShot;

        InteractionResult ret = net.neoforged.neoforge.event.EventHooks.onArrowNock(itemstack, level, player, hand, flag);
        if (ret != null) return ret;

        if (!player.hasInfiniteMaterials() && !flag) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    public int getDefaultProjectileRange() {
        return 15;
    }

    public int getProjectilesPerShot() {
        return this.projectilesPerShot;
    }
}
