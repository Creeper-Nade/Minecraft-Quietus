package com.minecraftquietus.quietus.item.tool;

import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectiles;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.minecraftquietus.quietus.entity.projectiles.misc.grapples.ChainGrapplingHookProjectile;
import com.minecraftquietus.quietus.item.QuietusItemProperties;
import com.minecraftquietus.quietus.item.property.GrapplingHookProperty;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class GrapplingHookItem extends Item {
    private final GrapplingHookProperty grapplingHookProperty;

    public GrapplingHookItem(Item.Properties properties) {
        super(properties);

        if (properties instanceof QuietusItemProperties quietusProps) {
            this.grapplingHookProperty = quietusProps.getGrapplingHookProperty();
        } else {
            // Default properties if not specified
            this.grapplingHookProperty = GrapplingHookProperty.builder()
                    .maxRange(100.0F)
                    .speed(5.0D)
                    .pullStrength(0.1F)
                    .frictionMultiplier(0.99F)
                    .build();
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // If player has active hook, retrieve it first
        retrieveHookForPlayer(player);

        // Shoot new hook
        if (!level.isClientSide()) {
            itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }

        shootHook(level, player);

        return InteractionResult.SUCCESS_SERVER;
    }

    private void shootHook(Level level, Player player) {
        if (!level.isClientSide() && grapplingHookProperty != null) {
            // Create projectile using factory
            GrapplingHookProjectile hook = (GrapplingHookProjectile) grapplingHookProperty.projectileFactory().apply(level, player);

            // Configure the projectile
            hook.setGrapplingHookProperty(grapplingHookProperty);
            hook.setOwner(player);

            // Calculate position in front of player
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            Vec3 spawnPos = eyePos.add(lookVec.scale(0.5));

            hook.setPos(spawnPos.x, spawnPos.y - 0.1, spawnPos.z);

            // Set velocity based on player's look direction
            hook.setDeltaMovement(lookVec.scale(grapplingHookProperty.speed()));

            // Set initial rotation
            hook.setYRot(player.getYRot());
            hook.setXRot(player.getXRot());

            level.addFreshEntity(hook);

            // Store in attachment
            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            attachment.setHookEntityId(hook.getId());
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.gameEvent(GameEvent.ITEM_INTERACT_START);
    }

    public static void retrieveHookForPlayer(Player player) {
        Level level = player.level();
        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);

        if (attachment.hasActiveHook()) {
            var entity = level.getEntity(attachment.getHookEntityId());
            if (entity instanceof GrapplingHookProjectile hook && !level.isClientSide()) {
                hook.discard();
                attachment.clear();
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                    1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
    }

    public GrapplingHookProperty getGrapplingHookProperty() {
        return grapplingHookProperty;
    }
}