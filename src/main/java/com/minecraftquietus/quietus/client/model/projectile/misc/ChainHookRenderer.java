package com.minecraftquietus.quietus.client.model.projectile.misc;

import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileModel;
import com.minecraftquietus.quietus.client.model.projectile.magic.ProjectileRenderState;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.tool.GrapplingHookItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ChainHookRenderer extends EntityRenderer<GrapplingHookProjectile, GrapplingHookRenderState> {

    private ChainHookModel model;
    private static final double VIEW_BOBBING_SCALE = 960.0; // from FishingHookRenderer
    private static HumanoidArm cachedArm;

    public ChainHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model= new ChainHookModel(context.bakeLayer(ChainHookModel.LAYER_LOCATION));
    }

    // Tell the render engine how to create a new entity render state.
    @Override
    public GrapplingHookRenderState createRenderState() {
        return new GrapplingHookRenderState();
    }

    

    // Update the render state by copying the needed values from the passed entity to the passed state.
    // Both Entity and EntityRenderState may be replaced with more concrete types,
    // based on the generic types that have been passed to the supertype.
    @Override
    public void extractRenderState(GrapplingHookProjectile entity, GrapplingHookRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
        // Extract and store any additional values in the state here.
        // Compute line offset from hook to player's hand
        Player player = entity.getPlayerOwner();
        if (player != null) {
            float attackAnim = player.getAttackAnim(partialTick);
            float handAngle = Mth.sin(Mth.sqrt(attackAnim) * (float) Math.PI);
            Vec3 handPos = getPlayerHandPos(player, handAngle, partialTick);
            // Slight vertical offset to attach line to the "tip" of the hook (like fishing bobber)
            Vec3 hookPos = entity.getPosition(partialTick).add(0.0, 0.25, 0.0);
            state.lineOriginOffset = handPos.subtract(hookPos);
        } else {
            state.lineOriginOffset = Vec3.ZERO;
        }
    }


    // Actually render the entity. The first parameter matches the render state's generic type.
    // Calling super will handle leash and name tag rendering for you, if applicable.
    @Override
    public void render(GrapplingHookRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        //poseStack.translate(0.0F, 0.0F, 0.0F);
        poseStack.translate(-0.5F / 16.0F, 5.0F / 16.0F, 0.0F); // Convert pixels to blocks (1/16th of a block)
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(
                buffer, this.model.renderType(this.getTextureLocation()),false, false);


        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        // Render the line (rope) if we have a valid offset
        if (state.lineOriginOffset != null && !state.lineOriginOffset.equals(Vec3.ZERO)) {
            float fx = (float) state.lineOriginOffset.x;
            float fy = (float) state.lineOriginOffset.y;
            float fz = (float) state.lineOriginOffset.z;

            VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lineStrip());
            PoseStack.Pose pose = poseStack.last();

            for (int j = 0; j <= 16; ++j) {
                stringVertex(fx, fy, fz, lineConsumer, pose, fraction(j, 16), fraction(j + 1, 16));
            }
        }
        super.render(state, poseStack, buffer, packedLight);
        // do your own rendering here
    }

    // ========== Helper methods copied/adapted from FishingHookRenderer ==========

    private Vec3 getPlayerHandPos(Player player, float handAngle, float partialTick) {
        int armFactor = getHoldingArm(player) == HumanoidArm.RIGHT ? 1 : -1;
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            double fovScale = VIEW_BOBBING_SCALE / (double) (Integer) this.entityRenderDispatcher.options.fov().get();
            Vec3 cameraOffset = this.entityRenderDispatcher.camera.getNearPlane()
                    .getPointOnPlane((float) armFactor * 0.525F, -0.1F)
                    .scale(fovScale)
                    .yRot(handAngle * 0.5F)
                    .xRot(-handAngle * 0.7F);
            return player.getEyePosition(partialTick).add(cameraOffset);
        } else {
            float bodyYaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
            double sinYaw = Mth.sin(bodyYaw);
            double cosYaw = Mth.cos(bodyYaw);
            float scale = player.getScale();
            double armOffset = (double) armFactor * 0.35 * (double) scale;
            double forwardOffset = 0.8 * (double) scale;
            float crouchOffset = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(partialTick)
                    .add(-cosYaw * armOffset - sinYaw * forwardOffset,
                            (double) crouchOffset - 0.45 * (double) scale,
                            -sinYaw * armOffset + cosYaw * forwardOffset);
        }
    }

    private static HumanoidArm getHoldingArm(Player player) {
        // In vanilla fishing, this checks if the main hand item can perform the "fishing_rod_cast" ability.
        // For grappling hook, we can simply return the main arm if the item is a grappling hook, else opposite.
        // Simplified: assume the grappling hook is in the main hand if present.
        // More robust: check if the main hand item is a GrapplingHookItem.
        // For now, just use main arm (you can enhance later).
        ItemStack mainHandItem=player.getMainHandItem();
        if(mainHandItem.get(QuietusComponents.GRAPPLING_HOOK_CAST.get())==null && player.getOffhandItem().get(QuietusComponents.GRAPPLING_HOOK_CAST.get())==null) return cachedArm;
        cachedArm= mainHandItem.get(QuietusComponents.GRAPPLING_HOOK_CAST.get())!=null?player.getMainArm() : player.getMainArm().getOpposite();
        return cachedArm;
    }
    public static HumanoidArm getCachedArm()
    {
        return cachedArm;
    }

    private static float fraction(int numerator, int denominator) {
        return (float) numerator / (float) denominator;
    }

    private static void stringVertex(float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose,
                                     float stringFraction, float nextStringFraction) {
        float fx = x * stringFraction;
        float fy = y * (stringFraction * stringFraction + stringFraction) * 0.5F + 0.25F;
        float fz = z * stringFraction;

        float nx = x * nextStringFraction - fx;
        float ny = y * (nextStringFraction * nextStringFraction + nextStringFraction) * 0.5F + 0.25F - fy;
        float nz = z * nextStringFraction - fz;

        float norm = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= norm;
        ny /= norm;
        nz /= norm;

        consumer.addVertex(pose, fx, fy, fz)
                .setColor(-16777216)   // black (opaque)
                .setNormal(pose, nx, ny, nz);
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/projectile/chain_hook_projectile.png");
    }
}
