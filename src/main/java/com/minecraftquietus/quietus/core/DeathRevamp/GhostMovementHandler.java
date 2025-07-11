package com.minecraftquietus.quietus.core.DeathRevamp;

import com.minecraftquietus.quietus.util.handler.ClientPayloadHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.List;

import static com.minecraftquietus.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class GhostMovementHandler {
    private static final double SPEED = 0.8;
    private static final double ACCELERATION_FACTOR = 0.1;
    private static final double DAMPING_FACTOR = 0.85;
    private static final double MAX_SPEED = 1.0;

    private static Vec3 ghostVelocity = Vec3.ZERO;

    public static void Init()
    {
        ghostVelocity=Vec3.ZERO;
    }
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClientPayloadHandler.getInstance().getGhostState()|| ClientPayloadHandler.getInstance().getHardcore() ||
                Minecraft.getInstance().getCameraEntity() != player) {
            return;
        }


        ClientInput input = event.getInput();
        Vec2 moveVector = input.getMoveVector();
        boolean jumping = input.keyPresses.jump();
        boolean shifting = input.keyPresses.shift();

        // Convert input to acceleration vector
        Vec3 acceleration = convertInputToMovement(player, moveVector, jumping, shifting)
                .scale(ACCELERATION_FACTOR);

        // Apply damping and acceleration to velocity
        ghostVelocity = ghostVelocity.scale(DAMPING_FACTOR).add(acceleration);

        // Cap maximum speed
        if (ghostVelocity.length() > MAX_SPEED) {
            ghostVelocity = ghostVelocity.normalize().scale(MAX_SPEED);
        }

        // Apply collision constraints
        Vec3 constrainedMovement = constrainMovement(player, ghostVelocity);
        player.setDeltaMovement(constrainedMovement);

        // Update velocity to constrained value for next tick
        ghostVelocity = constrainedMovement;

        resetInput(input, event);
    }

    private static Vec3 convertInputToMovement(LocalPlayer player, Vec2 moveVector,
                                               boolean jumping, boolean shifting) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0, look.z).normalize();
        Vec3 right = new Vec3(forward.z, 0, -forward.x);

        // Horizontal movement
        Vec3 horizontal = forward.scale(moveVector.y)
                .add(right.scale(moveVector.x))
                .scale(SPEED);

        // Vertical movement
        double vertical = 0;
        if (jumping) vertical += SPEED;
        if (shifting) vertical -= SPEED;

        return new Vec3(horizontal.x, vertical, horizontal.z);
    }

    private static Vec3 constrainMovement(Entity entity, Vec3 movement) {
        if (movement.equals(Vec3.ZERO)) return movement;

        AABB aabb = entity.getBoundingBox();
        Level level = entity.level();
        List<VoxelShape> collisions = level.getEntityCollisions(entity, aabb.expandTowards(movement));

        // Allow sliding along surfaces (remove the extra stop condition)
        return Entity.collideBoundingBox(entity, movement, aabb, level, collisions);
    }


    private static void resetInput(ClientInput input,MovementInputUpdateEvent event) {
        // Create new empty key presses
        Input emptyKeys = new Input(
                false,  // jumping
                false,  // shift
                false,  // sprint
                false,  // forward
                false,  // backward
                false,  // left
                false  // right
        );
        input.keyPresses = emptyKeys;

        // Reset move vector using reflection
        try {
            Field moveVectorField = ClientInput.class.getDeclaredField("moveVector");
            moveVectorField.setAccessible(true);
            moveVectorField.set(input, Vec2.ZERO);
        } catch (Exception e) {
            Logger LOGGER = LogUtils.getLogger();
            LOGGER.error("Failed to reset moveVector: {}", e.getMessage());
        }
        //event.setInput(new ClientInput());
    }
}
