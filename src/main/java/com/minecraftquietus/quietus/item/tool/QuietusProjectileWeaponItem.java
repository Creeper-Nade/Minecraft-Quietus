package com.minecraftquietus.quietus.item.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.item.QuietusItemProperties;
import com.minecraftquietus.quietus.item.property.SoundAsset;
import com.minecraftquietus.quietus.item.property.QuietusProjectileProperty;
import com.minecraftquietus.quietus.util.TriFunction;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
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
import net.minecraft.world.phys.Vec3;

/**
 * Class for weapon items not consuming items. Has to be configured to shoot a projectile of QuietusProjectile
 */
public class QuietusProjectileWeaponItem extends ProjectileWeaponItem {

    protected final int projectilesPerShot;
    protected final int attackRange;
    protected Map<Integer,QuietusProjectileProperty> projectilePropertyMap;
    protected final float shootVelocity;
    protected final float shootInaccuracy;
    protected final int useDuration;
    protected final int powerDuration;
    protected final TriFunction<Float,Integer,RandomSource,Float> xRotCalc;
    protected final TriFunction<Float,Integer,RandomSource,Float> yRotCalc;
    protected final Map<String,SoundAsset> soundMap;

    private final Map<Integer,QuietusProjectileProperty> MAP_DEFAULT_PROJECTILE_PROPERTY = 
        Map.of(0, new QuietusProjectileProperty.Builder().damage(5.0f).critChance(0.05d).knockback(0.4f).gravity(0.0f).persistanceTicks(200).projectileType(QuietusProjectiles.AMETHYST_PROJECTILE.get()).build());;

    public static final String MAPKEY_SOUND_PLAYER_SHOOT = "player_shoot";

    public QuietusProjectileWeaponItem(Item.Properties property) {
        super(property);
        if (property instanceof QuietusItemProperties prop) {
            this.projectilesPerShot = prop.projectileWeaponProperty.projectilesPerShot();
            this.projectilePropertyMap = Map.copyOf(prop.projectileProperties);
            this.shootVelocity = prop.projectileWeaponProperty.shootVelocity();
            this.shootInaccuracy = prop.projectileWeaponProperty.shootInaccuracy();
            this.useDuration = prop.projectileWeaponProperty.useDuration();
            this.powerDuration = prop.projectileWeaponProperty.powerDuration();
            this.xRotCalc = prop.projectileWeaponProperty.xRotOffsetCalc();
            this.yRotCalc = prop.projectileWeaponProperty.yRotOffsetCalc();
            this.attackRange = Objects.requireNonNullElse(prop.projectileWeaponProperty.attackRange(), 8);
            this.soundMap = prop.sounds.isEmpty() ? 
                Map.of(MAPKEY_SOUND_PLAYER_SHOOT, new SoundAsset.Builder().event(SoundEvents.ARROW_SHOOT).source(SoundSource.PLAYERS).build())
                 : Map.copyOf(prop.sounds);
        } else {
            this.projectilesPerShot = 1;
            this.shootVelocity = 1.5f;
            this.shootInaccuracy = 0.0f;
            this.useDuration = 0;
            this.powerDuration = -1;
            this.projectilePropertyMap = MAP_DEFAULT_PROJECTILE_PROPERTY;
            this.xRotCalc = (rotX, index, random) -> rotX;
            this.yRotCalc = (rotY, index, random) -> rotY;
            this.attackRange = 15;
            this.soundMap = Map.of(
                MAPKEY_SOUND_PLAYER_SHOOT, new SoundAsset.Builder().event(SoundEvents.ARROW_SHOOT).source(SoundSource.PLAYERS).build()
            );
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        InteractionResult ret = net.neoforged.neoforge.event.EventHooks.onArrowNock(itemstack, level, player, hand, true);
        if (ret != null) return ret;

        if (this.getUseDuration(itemstack, player) > 0) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME; 
        } else {
            if (player instanceof ServerPlayer) {
                List<ItemStack> list = new ArrayList<>();
                if (level instanceof ServerLevel serverlevel) {
                    this.shoot(
                        serverlevel, player, player.getUsedItemHand(), itemstack, 
                        list, 
                        this.shootVelocity, 
                        this.shootInaccuracy, 
                        false, // not taken in account
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
        /* player.startUsingItem(hand);
        return InteractionResult.CONSUME; */
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
                // server side
                if (player instanceof ServerPlayer) {
                    List<ItemStack> list = new ArrayList<>();
                    if (level instanceof ServerLevel serverlevel) {
                        this.shoot(
                            serverlevel, player, player.getUsedItemHand(), stack, 
                            list, // don't care, NonAmmoProjectileWeaponItem#shoot checks on its own
                            shootVelocity*power, 
                            1.0F, 
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

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (this.powerDuration < 0 && this.getUseDuration(stack, livingEntity) > 0) {
            if (livingEntity instanceof Player player) {
                if (player instanceof ServerPlayer) {
                    List<ItemStack> list = new ArrayList<>(); 
                    if (level instanceof ServerLevel serverlevel) {
                        this.shoot(
                            serverlevel, player, player.getUsedItemHand(), stack, 
                            list, // don't care, NonAmmoProjectileWeaponItem#shoot checks on its own
                            shootVelocity, 
                            1.0F, 
                            false,  // don't care. This is not use in NonAmmoProjectileWeaponItem#shoot
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
        float f1 = this.projectilesPerShot == 1 ? 0.0F : 2.0F * f / (projectileItems.size() - 1);
        float f2 = (this.projectilesPerShot - 1) % 2 * f1 / 2.0F;
        float f3 = 1.0F;
        QuietusProjectileProperty projectileProperty = MAP_DEFAULT_PROJECTILE_PROPERTY.get(0); // default
        for (int i = 0; i < this.projectilesPerShot; i++) {
            projectileProperty = Objects.requireNonNullElse(this.projectilePropertyMap.get(i), projectileProperty); // if this key not specified take previous property
            if (projectileProperty.isCustom()) { // custom projectile supports below arguments for projectiles configuring:
                QuietusProjectile projectile = this.createProjectileWithKey(i, level, shooter, weapon, ItemStack.EMPTY, shooter.getRandom().nextDouble() < projectileProperty.critChance());
                projectile.setOwner(shooter);
                // CreeperNade: Offset the y position for -0.1f, this is the y pos for arrow in vanilla minecraft, and doesn't block view
                projectile.setPos(shooter.getEyePosition().x,shooter.getEyePosition().y-0.1f,shooter.getEyePosition().z);
                /* if (projectileProperty.projectileType().create(level, EntitySpawnReason.LOAD) != null) { // projectile of this index is not null */
                    float f4 = f2 + f3 * ((i + 1) / 2) * f1;
                    f3 = -f3;
                    this.shootProjectile(shooter, projectile, i, velocity, inaccuracy, f4, target);
                /* } */
                level.addFreshEntity(projectile);
            }
        }
        weapon.hurtAndBreak(1, shooter, LivingEntity.getSlotForHand(hand));
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public float getPowerForTime(int charge) {
        if (this.powerDuration == 0) { // power = 0: do not divide, return full charge
            return 1.0f;
        }
        float f = charge / (float)this.powerDuration;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    } 

    @Override
    protected void shootProjectile(
        LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target
    ) {
        projectile.shootFromRotation(
            shooter, 
            this.xRotCalc.apply(shooter.getXRot(), index, shooter.getRandom()), 
            this.yRotCalc.apply(shooter.getYRot() + angle, index, shooter.getRandom()), 
            0.0F, 
            velocity, inaccuracy
        );
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        /* ArrowItem arrowitem = ammo.getItem() instanceof ArrowItem arrowitem1 ? arrowitem1 : (ArrowItem)Items.ARROW;
        AbstractArrow abstractarrow = arrowitem.createArrow(level, ammo, shooter, weapon);
        if (isCrit) {
            abstractarrow.setCritArrow(true);
        } */
        return this.createProjectileWithKey(0, level, shooter, weapon, ammo, isCrit);
    }
    protected QuietusProjectile createProjectileWithKey(int key, Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        QuietusProjectile projectile;
        // Use projectileProperty of weapon provided, or else use this own projectileProperty
        if (weapon.getItem() instanceof QuietusProjectileWeaponItem weapon1) {
            projectile = weapon1.getProjectileProperty(key).projectileType().create(level, EntitySpawnReason.LOAD);
            projectile.configure(weapon1.getProjectileProperty(key),weapon);
        } else {
            projectile = this.projectilePropertyMap.get(key).projectileType().create(level, EntitySpawnReason.LOAD);
            projectile.configure(this.projectilePropertyMap.get(key),weapon);
        }
        return projectile;
    }


    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return this.useDuration == -1 ? 72000 : this.useDuration;
    } 
    public int getPowerDuration(ItemStack stack, LivingEntity entity) {
        return this.powerDuration;
    } 

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (this.useDuration == 0) {
            return super.getUseAnimation(stack);
        } else {
            return ItemUseAnimation.BOW;
        }
    } 

    // Used in net.minecraft.world.entity.ai.behavior.BehaviourUtils by entity AIs to check whether or not in range for attack
    public int getDefaultProjectileRange() {
        return this.attackRange;
    }

    public int getProjectilesPerShot() {
        return this.projectilesPerShot;
    }

    public QuietusProjectileProperty getProjectileProperty(int key) {
        return this.projectilePropertyMap.get(key);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (projectile)->true; // everything, since this does not consume projectile
    }
}





/*
 * package com.minecraftquietus.quietus.item.weapons;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
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

public class MagicalWeapon<T extends QuietusProjectile> extends Item {
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
        projectile.configure(gravity,knockback,base_damage,life_span,base_crit_chance);
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

 */