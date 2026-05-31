package com.quietus.mixin;

import com.quietus.client.handler.ClientPayloadHandler;
import com.quietus.client.model.projectile.misc.ChainHookRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart leftArm;
    private static final float SMOOTHING = 0.1F; // adjust for responsiveness
    private static final float ATTACK_SMOOTHING = 0.2F; // how fast blend responds to attackTime changes

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void onSetupAnim(T state, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;


        // Get the arm that holds the grappling hook
        HumanoidArm holdingArm=ChainHookRenderer.getCachedArm();
        var targetArm = holdingArm==HumanoidArm.RIGHT?rightArm:leftArm;

        // Retrieve custom angles stored in player persistent data
        var data = player.getPersistentData();
        float targetPitch = data.getFloatOr("QuietusGrappleArmPitch",0);
        float targetYaw = data.getFloatOr("QuietusGrappleArmYaw",0);
        // Read current smoothed angles (default to target if not present)
        float currentPitch = data.getFloatOr("QuietusGrappleCurrentPitch", targetPitch);
        float currentYaw   = data.getFloatOr("QuietusGrappleCurrentYaw", targetYaw);
        // Smooth toward target
        currentPitch = currentPitch * (1 - SMOOTHING) + targetPitch * SMOOTHING;
        currentYaw   = currentYaw   * (1 - SMOOTHING) + targetYaw   * SMOOTHING;
        data.putFloat("QuietusGrappleCurrentPitch", currentPitch);
        data.putFloat("QuietusGrappleCurrentYaw", currentYaw);
        // Mirror yaw for left arm
        float finalYaw = currentYaw;
        if (targetArm==leftArm) {
            finalYaw = -finalYaw;
        }
        // Blend weight: 1 if hook active, otherwise decay to 0
        float blend = data.getFloatOr("QuietusGrappleBlend", 0);

        if (ClientPayloadHandler.getInstance().GetHookActivity()){
            blend = Math.min(1.0f, blend + 0.1f); // increase weight quickly
        } else {
            blend = Math.max(0, blend * 0.95F);
        }
        // Store updated current angles
        data.putFloat("QuietusGrappleBlend", blend);
        // Determine if this arm is the one currently attacking
        boolean isAttackingArm = (state.attackArm == holdingArm);
        float attackBlend;
        if (isAttackingArm) {
            float attackTime = state.attackTime;
            if (attackTime <= 0.5F) {
                attackBlend = 1.0F - (attackTime * 2.0F);
            } else {
                attackBlend = (attackTime - 0.5F) * 2.0F;
            }
            attackBlend = Mth.clamp(attackBlend, 0, 1);
        } else {
            // Non-attacking arm: full custom pose (no attack influence)
            attackBlend = 1.0F;
        }

        float smoothedAttackBlend = data.getFloatOr("QuietusGrappleAttackBlend", attackBlend);
        smoothedAttackBlend = smoothedAttackBlend * (1 - ATTACK_SMOOTHING) + attackBlend * ATTACK_SMOOTHING;
        data.putFloat("QuietusGrappleAttackBlend", smoothedAttackBlend);

        float finalBlend = blend * smoothedAttackBlend;
        if (finalBlend <= 0.01F) return;

        System.out.println(finalBlend);
        targetArm.xRot = targetArm.xRot * (1 - finalBlend) + currentPitch * finalBlend;
        targetArm.yRot = targetArm.yRot * (1 - finalBlend) + finalYaw * finalBlend;
    }
}
