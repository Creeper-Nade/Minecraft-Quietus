package com.minecraftquietus.quietus.event_listener;

import com.minecraftquietus.quietus.client.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.client.model.projectile.misc.ChainHookRenderer;
import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.minecraftquietus.quietus.item.tool.GrapplingHookItem;
import com.minecraftquietus.quietus.server.PlayerData;
import com.minecraftquietus.quietus.server.packet.GrapplingJumpReleasePacket;
import com.minecraftquietus.quietus.client.packet.GrapplingHookPhysicsPacket;
import com.minecraftquietus.quietus.util.PlayerClientPacketDistributor;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.minecraftquietus.quietus.util.ServerPacketDistributor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid=MODID)
public class GrapplingEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean jumpPressed = false;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;  // Only run on server

        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);

        if (attachment.hasActiveHook()) {
            GrapplingHookProjectile hook = getHookFromAttachment(player.level(), player, attachment);
            if (hook != null && hook.isInBlock()) {
                float ropeLength = hook.getLength();
                float pullStrength = hook.getPullStrength();
                //LOGGER.debug("Hook: length={}, pullStrength={}", ropeLength, pullStrength);

                double distance = player.getEyePosition().distanceTo(hook.position());
                //LOGGER.debug("Distance={}, ratio={}", distance, distance / ropeLength);

                Vec3 newVelocity = calculateGrapplingPhysics(player, hook);
                //LOGGER.debug("Old vel={}, new vel={}", player.getDeltaMovement(), newVelocity);

                // Apply velocity on server
                player.setDeltaMovement(newVelocity);
                player.resetFallDistance();

                // Also send packet to client for immediate response
                if (player instanceof ServerPlayer serverPlayer) {
                    PlayerClientPacketDistributor.sendGrapplePhysicsPackToEntity(serverPlayer,newVelocity);
                }
            }
            else {
                // Clear the grappling flag when not grappling
                player.getPersistentData().remove("QuietusGrappling");
            }
        }
    }

    // Jump-to-release mechanic (like Terraria)
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide()) return;

            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            if (attachment.hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
            }
        }
    }
    private static Vec3 calculateGrapplingPhysics(Player player, GrapplingHookProjectile hook) {
        Vec3 hookPos = hook.position();
        Vec3 playerPos = player.getEyePosition();
        Vec3 toHook = hookPos.subtract(playerPos);
        double distance = toHook.length();
        float ropeLength = hook.getLength();

        // If no valid rope length or rope is slack, apply only friction/gravity
        if (ropeLength <= 0 || distance <= ropeLength) {
            return player.getDeltaMovement();
        }

        // Rope is taut: compute spring force
        double stretch = distance - ropeLength;
        float pullStrength = hook.getPullStrength();
        float maxSpeed = hook.getMaxPullSpeed();
        Vec3 direction = toHook.normalize();

        // Spring force: pullStrength * stretch (Hooke's law)
        double springForce = pullStrength * stretch;
        // Damping: reduce velocity along the rope to avoid oscillation
        double alongSpeed = player.getDeltaMovement().dot(direction);
        double damping = hook.getFrictionMultiplier() * alongSpeed; // frictionMultiplier acts as damping factor

        // Net acceleration along rope
        double acceleration = springForce - damping;
        // Clamp to reasonable range
        acceleration = Math.max(-maxSpeed * 0.5, Math.min(acceleration, maxSpeed * 0.5));

        // Apply acceleration
        Vec3 deltaV = direction.scale(acceleration * 0.2); // scale factor to tune
        Vec3 newVelocity = player.getDeltaMovement().add(deltaV);

        // Perpendicular damping (air resistance while swinging)
        Vec3 parallel = direction.scale(newVelocity.dot(direction));
        Vec3 perpendicular = newVelocity.subtract(parallel);
        newVelocity = parallel.add(perpendicular.scale(0.99)); // slight damping

        // Absolute speed limit
        double speed = newVelocity.length();
        if (speed > maxSpeed) {
            newVelocity = newVelocity.scale(maxSpeed / speed);
        }

        return newVelocity;
    }
    private static Vec3 calculateSimplePull(Player player, GrapplingHookProjectile hook) {
        Vec3 toHook = hook.position().subtract(player.getEyePosition());
        double distance = toHook.length();
        float ropeLength = hook.getLength();

        if (ropeLength <= 0 || distance <= ropeLength) {
            // Slack: just slow down and apply gravity
            return player.getDeltaMovement().scale(0.98).add(0, -0.02, 0);
        }

        double over = distance - ropeLength;
        double force = hook.getPullStrength() * 0.8; // constant pull

        // Optionally increase force when overstretched
        force += over * 0.1;

        Vec3 pull = toHook.normalize().scale(force);

        // Combine with existing motion, then add a tiny bit of gravity
        return player.getDeltaMovement().add(pull).add(0, -0.02, 0);
    }
    @SubscribeEvent
    public static void onPlayerTravel(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        // On client side, check if we should override input
        if (player.level().isClientSide() && player.getPersistentData().getBooleanOr("QuietusGrappling",false)) {
            // Don't apply normal input while grappling
            // The velocity will come from the server packets
            player.setDeltaMovement(player.getDeltaMovement().scale(0.98)); // Dampen any local input
        }
    }
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
        if (attachment.hasActiveHook()) {
            // The hook is in the old dimension; we need to remove it.
            removeHookFromAnyDimension((ServerPlayer) player, attachment);
        }
    }
    private static void removeHookFromAnyDimension(ServerPlayer player, GrapplingHookAttachment attachment) {
        if (!attachment.hasActiveHook()) return;
        int hookId = attachment.getHookEntityId();
        for (ServerLevel world : player.level().getServer().getAllLevels()) {
            Entity e = world.getEntity(hookId);
            if (e instanceof GrapplingHookProjectile hook) {
                hook.discard();
                attachment.clear();
                PlayerClientPacketDistributor.sendGrappleActivityPackToEntity(player,attachment.hasActiveHook(), attachment.getHookEntityId());
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            if (attachment.hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            GrapplingHookAttachment attachment = player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
            if (attachment.hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean jumpDown = mc.options.keyJump.isDown();
        if (jumpDown && !jumpPressed) {
            // Jump key just pressed
            jumpPressed = true;
            ServerPacketDistributor.sendGrappleJumpPackToServer();
        } else if (!jumpDown && jumpPressed) {
            jumpPressed = false;
        }

        // --- Arm rotation logic ---
        if (!ClientPayloadHandler.getInstance().GetHookActivity()) {
            resetArmAngles(player);
            return;
        }

        Entity hookEntity = player.level().getEntity(ClientPayloadHandler.getInstance().getActiveHookID());
        if (!(hookEntity instanceof GrapplingHookProjectile hook) || !hook.isInBlock()) {
            resetArmAngles(player);
            return;
        }

        boolean isRightHand = ChainHookRenderer.getCachedArm() == HumanoidArm.RIGHT;
        Vec3 shoulderPos = getShoulderPosition(player, isRightHand);
        Vec3 hookPos = hook.position();
        Vec3 dir = hookPos.subtract(shoulderPos).normalize();

// Transform direction to player-local coordinates
        float bodyYawRad = (float) Math.toRadians(player.yBodyRot);
        Vec3 forward = new Vec3(-Math.sin(bodyYawRad), 0, Math.cos(bodyYawRad));
        Vec3 right = new Vec3(Math.cos(bodyYawRad), 0, Math.sin(bodyYawRad));
        Vec3 up = new Vec3(0, 1, 0);

        double forwardComp = dir.dot(forward);
        double rightComp = dir.dot(right);
        double upComp = dir.dot(up);

// Pitch: angle from horizontal plane (positive = upward)
        float pitchRaw = (float) Math.asin(Mth.clamp(upComp, -1.0, 1.0));
// Yaw: angle from forward direction (positive = to the right)
        float yawRaw = (float) Math.atan2(rightComp, forwardComp);

// Map to arm model axes
// For right arm: xRot negative = raise arm, yRot positive = outward swing
        float armPitch = -(pitchRaw + (float)Math.PI / 2F);         // invert: pointing up -> negative pitch
        float armYaw = -yawRaw;               // positive yaw = arm moves to the right
        armYaw = Mth.clamp(armYaw, -1.2F, 1.2F);

// Mirror yaw for left arm
        if (!isRightHand) {
            armYaw = -armYaw;
        }


        player.getPersistentData().putFloat("QuietusGrappleArmPitch", armPitch);
        player.getPersistentData().putFloat("QuietusGrappleArmYaw", armYaw);

    }
    private static void resetArmAngles(Player player) {
        player.getPersistentData().remove("QuietusGrappleArmPitch");
        player.getPersistentData().remove("QuietusGrappleArmYaw");
    }

    private static Vec3 getShoulderPosition(Player player, boolean isRightHand) {
        Vec3 eyePos = player.getEyePosition();
        float sideOffset = isRightHand ? 0.3F : -0.3F;
        // shoulder is slightly lower and behind eye
        return eyePos.add(sideOffset, -0.35, -0.1);
    }
}