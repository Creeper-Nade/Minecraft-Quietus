package com.minecraftquietus.quietus.item.weapons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.item.QuietusItemProperties;
import com.minecraftquietus.quietus.item.property.SoundAsset;
import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Class for weapons consuming ammo. The projectile is hence spawned directly from the ammo class.
 */
public class AmmoProjectileWeaponItem extends QuietusProjectileWeaponItem {

    protected final double projectileCritChance;
    protected final Predicate<ItemStack> supportedProjectile;

    public static final String MAPKEY_SOUND_PLAYER_SHOOT = "player_shoot";
    public static final int MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE = 0;

    public AmmoProjectileWeaponItem(Item.Properties property) {
        super(property);
        if (property instanceof QuietusItemProperties prop) {
            this.projectileCritChance = prop.projectileProperties.get(MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE).critChance();
            this.supportedProjectile = prop.weaponProperty.supportedProjectiles();
        } else {
            this.projectileCritChance = 0.05d;
            this.supportedProjectile = ARROW_ONLY;
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        // check for ammo
        List<ItemStack> ammoList = this.checkForAmmo(player, itemstack);
        int ammoCount = 0;
        for (ItemStack ammo : ammoList) {
            ammoCount += ammo.getCount();
        }
        boolean flag = (ammoCount >= this.projectilesPerShot);

        InteractionResult ret = net.neoforged.neoforge.event.EventHooks.onArrowNock(itemstack, level, player, hand, flag);
        if (ret != null) return ret;

        if (!player.hasInfiniteMaterials() && !flag) {
            return InteractionResult.FAIL;
        } else {
            if (this.getUseDuration(itemstack, player) > 0) {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME; 
            } else {
                if (player instanceof ServerPlayer) {
                    List<ItemStack> list = drawAmmo(this.projectilesPerShot, player, itemstack);
                    if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                        this.shoot(
                            serverlevel, player, player.getUsedItemHand(), itemstack, 
                            list, 
                            shootVelocity, 
                            this.shootInaccuracy, 
                            player.getRandom().nextDouble() < projectileCritChance, 
                            null
                        );
                    }

                    level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        this.soundMap.get(MAPKEY_SOUND_PLAYER_SHOOT).soundEvent(),
                        this.soundMap.get(MAPKEY_SOUND_PLAYER_SHOOT).soundSource(),
                        1.0F,
                        1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + shootVelocity * 0.5F
                    );
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResult.SUCCESS_SERVER; 
                } else {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (this.getUseDuration(stack, entity) == 0 || this.powerDuration < 0) {
            return super.releaseUsing(stack, level, entity, timeLeft);
        } else {
            if (!(entity instanceof Player player)) {
                return false;
            } else { 
                // checking power
                int useTime = this.getUseDuration(stack, entity) - timeLeft;
                useTime = net.neoforged.neoforge.event.EventHooks.onArrowLoose(stack, level, player, useTime, !player.getProjectile(stack).isEmpty());
                if (useTime < 0) return false; 
                float power = 1.0f;
                power = this.getPowerForTime(useTime);
                if (power < 0.1) return false;
                // checking ammo
                List<ItemStack> ammoList = this.checkForAmmo(player, stack);
                int ammoCount = 0;
                for (ItemStack ammo : ammoList) {
                    ammoCount += ammo.getCount();
                }
                boolean flag = (ammoCount >= this.projectilesPerShot);

                if (!player.hasInfiniteMaterials() && !flag) {
                    return false;
                // server side
                } else {
                    if (player instanceof ServerPlayer) {
                        List<ItemStack> list = drawAmmo(this.projectilesPerShot, player, stack);
                        if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                            this.shoot(
                                serverlevel, player, player.getUsedItemHand(), stack, 
                                list, 
                                shootVelocity*power, 
                                this.shootInaccuracy, 
                                this.getPowerDuration(stack, entity) >= 0 ? power >= 1.0f : false,  // don't care if has no power charge duration. This is not use in NonAmmoProjectileWeaponItem#shoot
                                null
                            );
                        }

                        level.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            this.soundMap.get(MAPKEY_SOUND_PLAYER_SHOOT).soundEvent(),
                            this.soundMap.get(MAPKEY_SOUND_PLAYER_SHOOT).soundSource(),
                            1.0F,
                            1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + shootVelocity * 0.5F
                        );
                        player.awardStat(Stats.ITEM_USED.get(this));
                        return true;
                    } else {
                        return true;
                    }
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (this.powerDuration < 0 && this.getUseDuration(stack, livingEntity) > 0) {
            if (livingEntity instanceof Player player) {
                List<ItemStack> ammoList = this.checkForAmmo(player, stack);
                    int ammoCount = 0;
                    for (ItemStack ammo : ammoList) {
                        ammoCount += ammo.getCount();
                    }
                    boolean flag = (ammoCount >= this.projectilesPerShot);

                    if (!player.hasInfiniteMaterials() && !flag) {
                        return stack;
                    } else {
                        if (player instanceof ServerPlayer) {
                            List<ItemStack> list = drawAmmo(this.projectilesPerShot, player, stack);
                            if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                                this.shoot(
                                    serverlevel, player, player.getUsedItemHand(), stack, 
                                    list, 
                                    shootVelocity, 
                                    this.shootInaccuracy, 
                                    (player.getRandom().nextDouble() < projectileCritChance), 
                                    null
                                );
                            }

                            level.playSound(
                                null,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                this.soundMap.get(MAPKEY_SOUND_PLAYER_SHOOT).soundEvent(),
                                this.soundMap.get(MAPKEY_SOUND_PLAYER_SHOOT).soundSource(),
                                1.0F,
                                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + shootVelocity * 0.5F
                            );
                            player.awardStat(Stats.ITEM_USED.get(this));
                            return stack;
                        } else {
                            return stack;
                        }
                    }
                }
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }
    
    @Override
    public void shoot(
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
        int totalDurabilityUse = 0;
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
                totalDurabilityUse += this.getDurabilityUse(projectileItem);
                
            }
        }
        // get average durability use of all ammo launched
        weapon.hurtAndBreak((int)(totalDurabilityUse / (float)projectileItems.size()), shooter, LivingEntity.getSlotForHand(hand));
    }

    /**
     * Checks for available ammo supported by weapon from player
     * @param player player being checked
     * @param weapon the weapon item
     * @return {@link List} of all found applicable items, in form of {@link ItemStack}
     */
    protected List<ItemStack> checkForAmmo(Player player, ItemStack weapon) {
        List<ItemStack> list = new ArrayList<>();
        Consumer<ItemStack> projectileItemAddUp = (stack) -> {
            if (this.getAllSupportedProjectiles().test(stack)) list.add(stack);
        };
        player.getInventory().forEach(projectileItemAddUp);
        return list;
    }

    /**
     * Static method.
     * Draws amount of ammo supported by weapon from player. 
     * @param amount amount of ammo needed
     * @param player player drawing from
     * @param weapon the weapon item
     * @return {@link List} of drawn ammo, in form of {@link ItemStack}, 
     * each ItemStack has count relative to the weapon's enchantments, such as minecraft:multishot.
     */
    protected static List<ItemStack> drawAmmo(int amount, Player player, ItemStack weapon) {
        List<ItemStack> list = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            ItemStack supportedAmmoStack = player.getProjectile(weapon);
            if (supportedAmmoStack.isEmpty()) {
                return list;
            }
            list.addAll(draw(weapon, supportedAmmoStack, player));
        }
        return list;
    }


    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        ArrowItem arrowitem = ammo.getItem() instanceof ArrowItem arrowitem1 ? arrowitem1 : (ArrowItem)Items.ARROW;
        AbstractArrow abstractarrow = arrowitem.createArrow(level, ammo, shooter, weapon);
        if (isCrit) {
            abstractarrow.setCritArrow(true);
        }
        return customArrow(abstractarrow, ammo, weapon);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        return arrow;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return this.supportedProjectile;
    }

}
