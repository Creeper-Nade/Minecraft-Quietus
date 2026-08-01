package com.quietus.mixin;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerSweepMixin {
    @Shadow
    public ServerPlayer player;

    @Unique
    private long quietus$lastMainHandBlockInteractionTick = Long.MIN_VALUE;

    @Inject(method = "handleUseItemOn", at = @At("TAIL"))
    private void rememberBlockInteraction(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (packet.getHand() == InteractionHand.MAIN_HAND) {
            this.quietus$lastMainHandBlockInteractionTick = this.player.level().getGameTime();
        }
    }

    @Inject(
            method = "handleAnimate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"
            )
    )
    private void sweepOnMiss(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (packet.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        // A successful right-click block interaction sends a swing packet too.
        // It is not evidence of a missed left-click attack.
        if (this.quietus$lastMainHandBlockInteractionTick == this.player.level().getGameTime()) {
            return;
        }

        float attackStrengthScale = player.getAttackStrengthScale(0.5F);
        boolean fullStrengthAttack = attackStrengthScale > 0.9F;
        boolean knockbackAttack = player.isSprinting() && fullStrengthAttack;
        PlayerSweepInvoker invoker = (PlayerSweepInvoker) player;
        if (!invoker.quietus$isSweepAttack(fullStrengthAttack, false, knockbackAttack)) {
            return;
        }

        ItemStack attackingItemStack = player.getWeaponItem();
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * invoker.quietus$baseDamageScaleFactor();
        DamageSource damageSource = invoker.quietus$createAttackSource(attackingItemStack);
        double reach = player.entityInteractionRange();
        double facingX = -Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
        double facingZ = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
        AABB sweepHitBox = player.getBoundingBox()
                .expandTowards(facingX * reach, 0.0, facingZ * reach)
                .inflate(1.0, 0.25, 1.0);

        // The player stands in for vanilla's directly-hit entity, so the normal
        // sweep routine excludes the attacker without requiring a primary target.
        invoker.quietus$doSweepAttack(player, baseDamage, damageSource, attackStrengthScale, sweepHitBox);
        player.onAttack();
    }
}
