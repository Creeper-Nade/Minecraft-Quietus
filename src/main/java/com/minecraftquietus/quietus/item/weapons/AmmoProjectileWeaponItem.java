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
public class AmmoProjectileWeaponItem extends ProjectileWeaponItem {

    protected final int projectilesPerShot;
    protected final int attackRange;
    protected final double projectileCritChance;
    protected final float shootVelocity;
    protected final Predicate<ItemStack> supportedProjectile;
    protected final TriFunction<Float,Integer,RandomSource,Float> xRotCalc;
    protected final TriFunction<Float,Integer,RandomSource,Float> yRotCalc;
    protected final Map<String, SoundAsset> soundMap;

    public static final String MAPKEY_SOUND_PLAYER_SHOOT = "player_shoot";
    public static final int MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE = 0;

    public AmmoProjectileWeaponItem(Item.Properties property) {
        super(property);
        if (property instanceof QuietusItemProperties prop) {
            this.projectilesPerShot = prop.weaponProperty.projectilesPerShot();
            this.projectileCritChance = prop.projectileProperties.get(MAPKEY_PROJECTILE_DEFAULT_CRITCHANCE).critChance();
            this.shootVelocity = prop.weaponProperty.shootVelocity();
            this.supportedProjectile = prop.weaponProperty.supportedProjectiles();
            this.xRotCalc = prop.weaponProperty.xRotOffsetCalc();
            this.yRotCalc = prop.weaponProperty.yRotOffsetCalc();
            this.attackRange = Objects.requireNonNullElse(prop.weaponProperty.attackRange(), 8);
            this.soundMap = prop.sounds.isEmpty() ? 
                Map.of(MAPKEY_SOUND_PLAYER_SHOOT, new SoundAsset.Builder().event(SoundEvents.ARROW_SHOOT).source(SoundSource.PLAYERS).build())
                 : Map.copyOf(prop.sounds);
        } else {
            this.projectilesPerShot = 1;
            this.shootVelocity = 3.0f;
            this.projectileCritChance = 0.05d;
            this.supportedProjectile = ARROW_ONLY;
            this.xRotCalc = (rotX, index, random) -> rotX;
            this.yRotCalc = (rotY, index, random) -> rotY;
            this.attackRange = 15;
            this.soundMap = Map.of(
                MAPKEY_SOUND_PLAYER_SHOOT, new SoundAsset.Builder().event(SoundEvents.ARROW_SHOOT).source(SoundSource.PLAYERS).build()
            );
        }
    }

    /* @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        } else { */
            /* int useTime = this.getUseDuration(stack, entity) - timeLeft;
            useTime = net.neoforged.neoforge.event.EventHooks.onArrowLoose(stack, level, player, useTime, !player.getProjectile(stack).isEmpty());
            if (useTime < 0) return false; */
            /* float f = getPowerForTime(useTime);
            if (f < 0.1) {
                return false;
            } else { */
                /* List<ItemStack> list = new ArrayList<>(this.projectilesPerShot);
                for (int i = 0; i < this.projectilesPerShot; i++) {
                    ItemStack supportedAmmoStack = player.getProjectile(stack);
                    if (supportedAmmoStack.isEmpty()) {
                        return false;
                    }
                    list.addAll(draw(stack, supportedAmmoStack, player));
                }
                if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                    this.shoot(
                        serverlevel, player, player.getUsedItemHand(), stack, 
                        list, 
                        shootVelocity, 
                        1.0F, 
                        player.getRandom().nextDouble() < projectileCritChance, 
                        null
                    );
                }

                level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ARROW_SHOOT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + shootVelocity * 0.5F
                );
                player.awardStat(Stats.ITEM_USED.get(this));
                return true;
            //}
        }
    } */

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
    protected void shootProjectile(
        LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target
    ) {
        projectile.shootFromRotation(shooter, this.xRotCalc.apply(shooter.getXRot(),index,shooter.getRandom()), this.yRotCalc.apply(shooter.getYRot() + angle,index,shooter.getRandom()), 0.0F, velocity, inaccuracy);
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

    
    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    /* public static float getPowerForTime(int charge) {
        float f = charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    } */

    /* @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    } */

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack p_40678_) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
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
            if (player instanceof ServerPlayer) {
                List<ItemStack> list = drawAmmo(this.projectilesPerShot, player, itemstack);
                if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                    this.shoot(
                        serverlevel, player, player.getUsedItemHand(), itemstack, 
                        list, 
                        shootVelocity, 
                        1.0F, 
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
            /* player.startUsingItem(hand);
            return InteractionResult.CONSUME; */
        }
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return this.supportedProjectile;
    }

    // Used in net.minecraft.world.entity.ai.behavior.BehaviourUtils by entity AIs to check whether or not in range for attack
    public int getDefaultProjectileRange() {
        return this.attackRange;
    }

    public int getProjectilesPerShot() {
        return this.projectilesPerShot;
    }
}
