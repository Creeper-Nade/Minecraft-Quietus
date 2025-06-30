package com.minecraftquietus.quietus.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.minecraftquietus.quietus.Quietus.MODID;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@EventBusSubscriber(modid=MODID)
public class PlayerDeathHandler {

    private static Vec3 lastPos;
    // Register the death event
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Prevent actual death but simulate death effects
        event.setCanceled(true);

        // Simulate death effects

        // Simulate death effects (updated to avoid protected access)
        boolean keepInventory = player.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        if (!keepInventory) {
            player.getInventory().dropAll();
            int reward = player.totalExperience;
            if (reward > 0) {
                ExperienceOrb.award(player.serverLevel(), player.position(), reward);
            }
            player.totalExperience = 0;
            player.experienceLevel = 0;
            player.experienceProgress = 0;
        }
        //player.dropAllDeathLoot(event.getSource());
        //player.dropExperience();

        // Set ghost state
        CompoundTag data = player.getPersistentData();
        data.putBoolean("isGhost", true);
        data.putInt("reviveCooldown", 100); // 5 seconds

        // Switch to spectator mode
        player.setGameMode(GameType.SPECTATOR);

        // Reset health but keep player "alive"
        player.setHealth(1.0F);
        player.removeAllEffects();
    }

    // Handle movement and revival
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if(!(player instanceof  ServerPlayer serverPlayer)) return;
        CompoundTag data = player.getPersistentData();

        // Check ghost state
        if (!data.contains("isGhost") || !data.getBooleanOr("isGhost",false)) return;

        // Handle cooldown
        int cooldown = data.getIntOr("reviveCooldown",0);
        if (cooldown > 0) {
            data.putInt("reviveCooldown", cooldown - 1);
            preventSpectatorNoClip(serverPlayer);  // Block passthrough
        } else {
            revivePlayer(serverPlayer);
        }
       // lastPos= player.getPosition(player.);
    }

    // Prevent spectator no-clip through blocks
    private static void preventSpectatorNoClip(ServerPlayer player) {

        // Calculate movement direction
        /*
        Vec3 movement = player.getDeltaMovement();
        System.out.println(movement);

        if (movement.equals(Vec3.ZERO)) return;

        // Create collision box
        AABB collisionBox = player.getBoundingBox().inflate(0.1);
        AABB futureBox = collisionBox.move(movement);
        //System.out.println("asd");

        // Check collisions
        if (!player.level().noCollision(player, futureBox)) {
            // Cancel movement if collision detected

            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;  // Force movement sync
        }*/
        CompoundTag data = player.getPersistentData();

        // Get last safe position with safe defaults
        double safeX = data.getDoubleOr("ghostSafeX",0);
        double safeY = data.getDoubleOr("ghostSafeY",0);
        double safeZ = data.getDoubleOr("ghostSafeZ",0);

        // Check if player has moved into a solid block
        AABB playerBB = player.getBoundingBox();
        if (!player.level().noCollision(player, playerBB)) {
            // Move player back to safe position
            player.teleportTo(safeX, safeY, safeZ);

            // Reset movement
            player.setDeltaMovement(Vec3.ZERO);
        } else {
            // Update safe position
            data.putDouble("ghostSafeX", player.getX());
            data.putDouble("ghostSafeY", player.getY());
            data.putDouble("ghostSafeZ", player.getZ());
        }

        // Prevent movement through blocks
        Vec3 movement = player.getDeltaMovement();
        if (!movement.equals(Vec3.ZERO)) {
            AABB futureBB = playerBB.move(movement);
            if (!player.level().noCollision(player, futureBB)) {
                player.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    // Revive the player properly
    private static void revivePlayer(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        // Get respawn configuration
        ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
        ServerLevel respawnLevel = player.server.overworld(); // Default to overworld
        BlockPos respawnPos = respawnLevel.getSharedSpawnPos();
        float respawnAngle = respawnLevel.getSharedSpawnAngle();

        if (respawnConfig != null) {
            // Get the respawn dimension
            respawnLevel = player.server.getLevel(respawnConfig.dimension());
            if (respawnLevel == null) {
                respawnLevel = player.server.overworld();
            }
            respawnPos = respawnConfig.pos();
            respawnAngle = respawnConfig.angle();
        }

        // Create empty relative movement set
        Set<Relative> relativeSet = Collections.emptySet();

        // Teleport to respawn location
        player.teleportTo(
                respawnLevel,
                respawnPos.getX() + 0.5,
                respawnPos.getY(),
                respawnPos.getZ() + 0.5,
                relativeSet,
                respawnAngle,
                0,   // Pitch
                false // setCamera
        );

        // Restore survival mode
        player.setGameMode(GameType.SURVIVAL);

        // Reset health and other player states
        player.setHealth(player.getMaxHealth() / 2);
        player.getFoodData().setFoodLevel(20);
        player.removeAllEffects();

        // Sync player state to client
        player.onUpdateAbilities();
        player.connection.send(new ClientboundSetHealthPacket(
                player.getHealth(),
                player.getFoodData().getFoodLevel(),
                player.getFoodData().getSaturationLevel()
        ));

        // Clear ghost data
        data.remove("isGhost");
        data.remove("reviveCooldown");
    }

    // Helper to get respawn dimension
    // Block interactions during ghost state
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getPersistentData().getBooleanOr("isGhost",false)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().getPersistentData().getBooleanOr("isGhost",false)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().getPersistentData().getBooleanOr("isGhost",false)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().getPersistentData().getBooleanOr("isGhost",false)) {
            event.setCanceled(true);
        }
    }

    // Prevent damage during ghost state
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player &&
                player.getPersistentData().getBooleanOr("isGhost",false)) {
            // Set damage to zero for players in ghost state
            event.setNewDamage(0.0f);
        }
    }
}
