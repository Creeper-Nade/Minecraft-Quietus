package com.minecraftquietus.quietus.mixin;

import com.minecraftquietus.quietus.core.DeathRevamp.GhostDeathScreen;
import com.minecraftquietus.quietus.sounds.QuietusSounds;
import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }
    @Inject(method = "die", at = @At("HEAD"))
    private void handleGhostDeathHead(DamageSource cause, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        Component deathMessage = cause.getLocalizedDeathMessage(player);
        player.level().playSound(null,player.getX(),player.getY(),player.getZ(),QuietusSounds.Steve_UOH.value(), EntitySoundSource.of(player), 0.8F, 1.0F);
        player.level().playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.BELL_BLOCK, EntitySoundSource.of(player), 1.0F, 1.0F);

        // Set ghost state
        CompoundTag data = player.getPersistentData();
        int cooldown=100;
        data.putBoolean("isGhost", true);
        data.putInt("reviveCooldown", cooldown);

        PlayerData.GhostPackToPlayer(player,true,deathMessage,cooldown);
        PlayerData.ReviveCDToPlayer(player,cooldown);




    }
    @Inject(method = "die", at = @At("TAIL"))
    private void handleGhostDeathTail(DamageSource cause, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        // Switch to spectator mode
        player.setGameMode(GameType.SPECTATOR);

        // Reset health but keep player "alive"
        player.setHealth(1.0F);
        player.removeAllEffects();



    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void handleGhostTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        CompoundTag data = player.getPersistentData();


        // Check ghost state
        if (!data.contains("isGhost") || !data.getBooleanOr("isGhost",false)) return;

        // Handle cooldown
        int cooldown = data.getIntOr("reviveCooldown",0);
        if (cooldown > 0) {
            data.putInt("reviveCooldown", cooldown - 1);
            validateGhostPosition(player);
        } else {
            revivePlayer(player);
        }
        if(cooldown%20==0)
        {
            PlayerData.ReviveCDToPlayer(player,cooldown);
        }
    }

    // Prevent spectator no-clip through blocks
    private static void validateGhostPosition(Player player) {
        Level level = player.level();
        CompoundTag data = player.getPersistentData();

        // Get last safe position
        Vec3 safePos = new Vec3(
                data.getDoubleOr("ghostSafeX",0),
                data.getDoubleOr("ghostSafeY",0),
                data.getDoubleOr("ghostSafeZ",0)
        );

        // Check if player is in a valid position
        if (level.noCollision(player, player.getBoundingBox())) {
            // Update safe position
            data.putDouble("ghostSafeX", player.getX());
            data.putDouble("ghostSafeY", player.getY());
            data.putDouble("ghostSafeZ", player.getZ());
        } else {
            // Move player to last safe position
            player.teleportTo(safePos.x, safePos.y, safePos.z);
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
        PlayerData.GhostPackToPlayer(player,false, CommonComponents.EMPTY,0);

        LocalPlayer Localplayer = Minecraft.getInstance().player;
        Localplayer.getPersistentData().remove("isGhost");
    }
}
