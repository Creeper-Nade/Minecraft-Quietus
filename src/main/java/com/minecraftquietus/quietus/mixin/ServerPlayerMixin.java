package com.minecraftquietus.quietus.mixin;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.client.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.sounds.QuietusSounds;
import com.minecraftquietus.quietus.tags.QuietusTags;
import com.minecraftquietus.quietus.util.PlayerData;
import com.minecraftquietus.quietus.util.QuietusGameRules;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }
    @Inject(method = "die", at = @At("HEAD"))
    private void handleGhostDeathHead(DamageSource cause, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        Component deathMessage = cause.getLocalizedDeathMessage(player);
        boolean hardcore= player.level().getLevelData().isHardcore();

        player.level().playSound(null,player.getX(),player.getY(),player.getZ(),QuietusSounds.Steve_UOH.value(), EntitySoundSource.of(player), 0.8F, 1.0F);
        player.level().playSound(null,player.getX(),player.getY(),player.getZ(),SoundEvents.BELL_BLOCK, EntitySoundSource.of(player), 1.0F, 1.0F);

        GameRules gameRules= player.serverLevel().getGameRules();
        if(gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN) || !gameRules.getBoolean(QuietusGameRules.GHOST_MODE_ENABLED))
            return;
        // Set ghost state
        CompoundTag data = player.getPersistentData();
        boolean boss_exists = !player.level().getEntities(
                (Entity) null, // No specific entity type filter
                player.getBoundingBox().inflate(1000), // 100 block radius
                entity -> entity.getType().is(QuietusTags.Entity.BOSS_MONSTER)// Check tag
        ).isEmpty();
        int cooldown=100;
        if(boss_exists) cooldown=300;

        data.putBoolean("isGhost", true);
        data.putInt("reviveCooldown", cooldown);
        data.putString("originalGameMode", player.gameMode.getGameModeForPlayer().getName());
        // Serialize death message with registry access
        HolderLookup.Provider registries = player.level().registryAccess();
        String json = Component.Serializer.toJson(deathMessage, registries);
        data.putString("deathMessage", json);

        PlayerData.sendGhostPackToPlayer(player,true,deathMessage,cooldown,hardcore);
        PlayerData.sendRevivalCDToPlayer(player,cooldown);
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void handleGhostDeathTail(DamageSource cause, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        GameRules gameRules= player.serverLevel().getGameRules();
        if(gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN) || !gameRules.getBoolean(QuietusGameRules.GHOST_MODE_ENABLED))
            return;
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
        if (!ClientPayloadHandler.getInstance().getGhostState()) return;
        boolean is_hardcore = ClientPayloadHandler.getInstance().getHardcore();

        int cooldown = data.getIntOr("reviveCooldown",0);
        if (is_hardcore)
        {
            if (cooldown > 60) {
                data.putInt("reviveCooldown", cooldown - 1);
            }
        }
        // Handle cooldown
        if (cooldown%20==0)
        {
            PlayerData.sendRevivalCDToPlayer(player,cooldown);
        }
       if (is_hardcore) return;

        if (cooldown > 0) {
            data.putInt("reviveCooldown", cooldown - 1);
            validateGhostPosition(player);
        } else {
            revivePlayer(player);
        }

    }

    // Prevent spectator no-clip through blocks
    @Unique
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
    @Unique
    private static void revivePlayer(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        boolean hardcore= player.level().getLevelData().isHardcore();

        String gameModeName = data.getStringOr("originalGameMode","survival");
        GameType originalGameMode = GameType.byName(gameModeName);
        if (originalGameMode == null) originalGameMode = GameType.SURVIVAL;

        // Clear ghost data
        data.remove("isGhost");
        data.remove("reviveCooldown");
        data.remove("originalGameMode");
        data.remove("deathMessage");
        data.remove("ghostSafeX");
        data.remove("ghostSafeY");
        data.remove("ghostSafeZ");
        PlayerData.sendGhostPackToPlayer(player,false, CommonComponents.EMPTY,0,hardcore);
        // Restore survival mode
        player.setGameMode(originalGameMode);
        player.onUpdateAbilities();




        player.connection.player = player.server.getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);

        //if we are going to halve player's health upon revival, like in Terraria
        //player.connection.player.setHealth(player.getMaxHealth() / 2);

        player.connection.resetPosition();
    }
}
