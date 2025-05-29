package com.minecraftquietus.quietus.item.weapons;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class TripleBowItem extends BowItem {

    public static final int DEFAULT_RANGE = 15;
    public static final int MAX_DRAW_DURATION = 30;

    public TripleBowItem(Item.Properties property) {
        super(property);
    }

    @Override
    public boolean releaseUsing(ItemStack weapon, Level level, LivingEntity shooter, int useTime) {
        if (!(shooter instanceof Player player)) {
            return false;
        } else {
            ItemStack itemstack = player.getProjectile(weapon);
            if (itemstack.isEmpty()) {
                return false;
            } else {
                int i = this.getUseDuration(weapon, shooter) - useTime;
                i = net.neoforged.neoforge.event.EventHooks.onArrowLoose(weapon, level, player, i, !itemstack.isEmpty());
                if (i < 0) return false;
                float expectedPower = getPowerForTime(i);
                if (expectedPower < 0.1) {
                    return false;
                } else {
                    for (int j = 0; j < 3; j++) {
                        List<ItemStack> list = draw(weapon, itemstack, player);
                        if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                            //this.shoot(serverlevel, player, player.getUsedItemHand(), weapon, list, expectedPower * 3.0F, 1.0F, expectedPower == 1.0F, null);
                            this.shoot(serverlevel, player, player.getUsedItemHand(), weapon, list, expectedPower * 3.0F, 1.5F, expectedPower == 1.0F, null);
                        }
                        level.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ARROW_SHOOT,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + expectedPower * 0.5F
                        );
                    }
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return true;
                }
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
            ItemStack itemstack = projectileItems.get(i);
            if (!itemstack.isEmpty()) {
                float f4 = f2 + f3 * ((i + 1) / 2) * f1;
                f3 = -f3;
                int j = i;
                Projectile.spawnProjectile(
                    this.createProjectile(level, shooter, weapon, itemstack, isCrit),
                    level,
                    itemstack,
                    p_360045_ -> this.shootProjectile(shooter, p_360045_, j, velocity, inaccuracy, f4, target)
                );
                weapon.hurtAndBreak(this.getDurabilityUse(itemstack), shooter, LivingEntity.getSlotForHand(hand));
                if (weapon.isEmpty()) {
                    break;
                }
            }
        }
    }

    @Override
    protected void shootProjectile(
        LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float p_330857_, @Nullable LivingEntity target
    ) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + p_330857_, 0.0F, velocity, inaccuracy);
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getPowerForTime(int charge) {
        float f = charge / 0.1f;
        //float f = charge / MAX_DRAW_DURATION;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public int getUseDuration(ItemStack weapon, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack weapon) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = !player.getProjectile(itemstack).isEmpty();

        InteractionResult ret = net.neoforged.neoforge.event.EventHooks.onArrowNock(itemstack, level, player, hand, flag);
        if (ret != null) return ret;

        if (!player.hasInfiniteMaterials() && !flag) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return DEFAULT_RANGE;
    }
}
