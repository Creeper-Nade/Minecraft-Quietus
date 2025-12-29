package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.minecraftquietus.quietus.item.tool.GrapplingHookItem;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID)
public class GrapplingEvent {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;  // Only run on server

        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);

        if (attachment.hasActiveHook()) {
            GrapplingHookProjectile hook = getHookFromAttachment(player.level(), player, attachment);
            if (hook != null && hook.isInBlock()) {
                applyGrapplingPhysics(player, hook);
            }
        }
    }

    // Jump-to-release mechanic (like Terraria)
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide) return;

            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            if (attachment.hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
            }
        }
    }

    private static void applyGrapplingPhysics(Player player, GrapplingHookProjectile hook) {
        // Get the rope length from the hook (set when it hit the block)
        float ropeLength = hook.getLength();
        if (ropeLength <= 0) return;  // No valid rope length

        Vec3 hookPos = hook.position();
        Vec3 playerPos = player.getEyePosition();
        Vec3 toHook = hookPos.subtract(playerPos);
        double currentDistance = toHook.length();

        // If we're beyond the rope length, pull towards the hook
        if (currentDistance > ropeLength) {
            double overDistance = currentDistance - ropeLength;
            double pullStrength = hook.getPullStrength();

            // Calculate how much to pull (stronger when farther away)
            double pullFactor = Math.min(overDistance / ropeLength, 1.0);
            double pullForce = pullStrength * (0.5 + pullFactor * 1.5);

            // Normalize direction and apply pull force
            Vec3 pullDirection = toHook.normalize();
            Vec3 pullVector = pullDirection.scale(pullForce);

            // Apply to player velocity
            Vec3 currentMotion = player.getDeltaMovement();
            player.setDeltaMovement(
                    currentMotion.x + pullVector.x,
                    currentMotion.y + pullVector.y * 0.8,  // Less vertical pull
                    currentMotion.z + pullVector.z
            );
            player.setPos(playerPos.add(player.getDeltaMovement()));
            System.out.println(currentMotion+"target"+player.getDeltaMovement());

            // Reset fall distance when grappling
            player.resetFallDistance();
        }

        // Apply swinging physics (like a pendulum)
        if (currentDistance > 0) {
            Vec3 toHookNormalized = toHook.normalize();
            Vec3 currentVelocity = player.getDeltaMovement();

            // Calculate velocity component perpendicular to rope
            Vec3 velocityAlongRope = toHookNormalized.scale(currentVelocity.dot(toHookNormalized));
            Vec3 perpendicularVelocity = currentVelocity.subtract(velocityAlongRope);

            // Apply friction to perpendicular motion for swinging
            float friction = hook.getFrictionMultiplier();
            Vec3 newPerpendicularVelocity = perpendicularVelocity.scale(friction);

            // Combine velocities
            Vec3 newVelocity = velocityAlongRope.add(newPerpendicularVelocity);

            // Limit maximum speed
            double speed = newVelocity.length();
            if (speed > 2.0) {
                newVelocity = newVelocity.scale(2.0 / speed);
            }

            player.setDeltaMovement(newVelocity);
        }
    }

    private static GrapplingHookProjectile getHookFromAttachment(Level level, Player player, GrapplingHookAttachment attachment) {
        if (attachment.hasActiveHook()) {
            var entity = level.getEntity(attachment.getHookEntityId());
            if (entity instanceof GrapplingHookProjectile hook && hook.getOwner() == player) {
                return hook;
            }
        }
        return null;
    }
}