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
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
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
            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            if (attachment.hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
            }
        }
    }

    private static void applyGrapplingPhysics(Player player, GrapplingHookProjectile hook) {
        player.resetFallDistance();

        Vec3 hookPos = hook.position();
        Vec3 playerPos = player.getEyePosition();
        Vec3 pullDirection = hookPos.subtract(playerPos);
        float ropeLength = hook.getLength();
        double currentDistance = pullDirection.length();

        if (currentDistance > ropeLength) {
            // Calculate pull strength based on how much we're over the rope length
            double overDistance = currentDistance - ropeLength;
            double pullStrength = hook.getPullStrength() * (overDistance / ropeLength);

            // Apply pull towards the hook
            Vec3 pullVector = pullDirection.normalize().scale(pullStrength);
            player.addDeltaMovement(pullVector);
        }
    }

    @SubscribeEvent
    public static void onPlayerTravel(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);

        if (attachment.hasActiveHook()) {
            GrapplingHookProjectile hook = getHookFromAttachment(player.level(), player, attachment);
            if (hook != null && hook.isInBlock() && !player.onGround()) {
                // Apply air resistance/friction
                float friction = hook.getFrictionMultiplier();
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(
                        motion.x * friction,
                        motion.y,
                        motion.z * friction
                );

                // Apply slight upward force to prevent falling too fast
                if (motion.y < 0) {
                    player.setDeltaMovement(motion.x, motion.y * 0.95, motion.z);
                }
            }
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