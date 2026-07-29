package com.quietus.client.model.projectile.misc;

import com.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.quietus.item.QuietusComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import static com.quietus.Quietus.MODID;

public class ChainHookRenderer extends EntityRenderer<GrapplingHookProjectile, GrapplingHookRenderState> {

    private ChainHookModel<GrapplingHookRenderState> model;
    private static final Identifier CHAIN_TEXTURE = Identifier.fromNamespaceAndPath(
            MODID, "textures/entity/projectile/chain_hook_chain.png"
    );
    private static final RenderType CHAIN_RENDER_TYPE = RenderTypes.entityCutout(CHAIN_TEXTURE);
    private static final float CHAIN_HALF_WIDTH = 1.5F / 16.0F;
    private static final float CHAIN_FIRST_STRIP_MIN_U = 0.0F;
    private static final float CHAIN_FIRST_STRIP_MAX_U = 3.0F / 16.0F;
    private static final float CHAIN_SECOND_STRIP_MIN_U = 3.0F / 16.0F;
    private static final float CHAIN_SECOND_STRIP_MAX_U = 6.0F / 16.0F;
    private static final double VIEW_BOBBING_SCALE = 960.0; // from FishingHookRenderer
    private static final Vec3 MODEL_TRANSLATION = new Vec3(-0.5 / 16.0, 5.0 / 16.0, 0.0);
    /*
     * Top-center of the anchor texture after the model's rotations. The two
     * crossed planes place it on opposite sides, so their midpoint is the
     * shared rope attachment.
     */
    private static final Vec3 ROPE_ATTACHMENT_IN_MODEL = new Vec3(
            0.0,
            -0.18935 / 16.0,
            (-3.1464 / Math.sqrt(2.0) - 3.0) / 16.0
    );
    private static HumanoidArm cachedArm;

    public ChainHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model= new ChainHookModel<>(context.bakeLayer(ChainHookModel.LAYER_LOCATION));
    }

    // Tell the render engine how to create a new entity render state.
    @Override
    public GrapplingHookRenderState createRenderState() {
        return new GrapplingHookRenderState();
    }

    @Override
    public boolean shouldRender(GrapplingHookProjectile entity, Frustum culler, double camX, double camY, double camZ) {
        return super.shouldRender(entity, culler, camX, camY, camZ) && entity.getPlayerOwner() != null;
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
            Vec3 attachmentOffset = ROPE_ATTACHMENT_IN_MODEL
                    // Vec3.xRot has the opposite sign convention from the
                    // quaternion used by Axis.XP/PoseStack.
                    .xRot((float) Math.toRadians(state.xRot))
                    .yRot((float) Math.toRadians(state.yRot))
                    .add(MODEL_TRANSLATION);
            Vec3 attachmentPos = entity.getPosition(partialTick).add(attachmentOffset);
            state.lineStartOffset = attachmentOffset;
            state.lineOriginOffset = handPos.subtract(attachmentPos);
        } else {
            state.lineStartOffset = Vec3.ZERO;
            state.lineOriginOffset = Vec3.ZERO;
        }
    }
    // Actually render the entity. The first parameter matches the render state's generic type.
    // Calling super will handle leash and name tag rendering for you, if applicable.
    @Override
    public void submit(GrapplingHookRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose(); // first push – preserves original transform for line
        poseStack.pushPose(); // second push – for hook model transformations

        // Transform the hook model
        poseStack.translate(MODEL_TRANSLATION.x, MODEL_TRANSLATION.y, MODEL_TRANSLATION.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        submitNodeCollector.submitModel(this.model, state, poseStack, this.getTextureLocation(),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);

        poseStack.popPose(); // remove hook model transformations, keep original translation

        // Render a textured, infinitely repeating chain between the hook and the player's hand.
        if (state.lineOriginOffset != null && !state.lineOriginOffset.equals(Vec3.ZERO)) {
            renderChain(poseStack, submitNodeCollector, state.lineStartOffset, state.lineOriginOffset, state.lightCoords);
        }
        poseStack.popPose(); // final pop – balances the first push
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    // ========== Helper methods copied/adapted from FishingHookRenderer ==========

    private Vec3 getPlayerHandPos(Player player, float handAngle, float partialTick) {
        HumanoidArm arm = getHoldingArm(player);
        boolean isRightHand = (arm == HumanoidArm.RIGHT);
        int armFactor=isRightHand?1:-1;
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            float fov=this.entityRenderDispatcher.options.fov().get().intValue();
            double fovScale = VIEW_BOBBING_SCALE / fov;
            Vec3 cameraOffset = this.entityRenderDispatcher.camera.getNearPlane(fov)
                    .getPointOnPlane((float) armFactor * 0.525F, -0.4F)
                    .scale(fovScale)
                    .yRot(handAngle * 0.5F)
                    .xRot(-handAngle * 0.7F);
            return player.getEyePosition(partialTick).add(cameraOffset);
        } else {

            float bodyYaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
            double cosYaw = Math.cos(bodyYaw);
            double sinYaw = Math.sin(bodyYaw);
            float scale = player.getScale();

            // Shoulder offset: right/left, at eye level, slightly back
            double side = (arm == HumanoidArm.RIGHT ? -0.35 : 0.35) * scale;
            double down = -0.2 * scale;  // slightly below eye
            double back = -0.05 * scale; // slightly behind
            Vec3 localShoulder = new Vec3(side, down, back);

            // Rotate shoulder
            double worldShoulderX = localShoulder.x * cosYaw - localShoulder.z * sinYaw;
            double worldShoulderZ = localShoulder.x * sinYaw + localShoulder.z * cosYaw;
            Vec3 shoulder = player.getEyePosition(partialTick).add(worldShoulderX, localShoulder.y, worldShoulderZ);

            // Arm vector: down and forward to reach item tip
            double armLength = 0.80 * scale;      // shoulder to hand
            double forwardOffset = 0.15 * scale;  // hand to item tip (adjust!)
            Vec3 localArm = new Vec3(0, -armLength, forwardOffset);

            // Apply custom rotations
            var data = player.getPersistentData();
            float pitch = data.getFloatOr("QuietusGrappleCurrentPitch", 0);
            float yaw   = data.getFloatOr("QuietusGrappleCurrentYaw", 0);
            float blend = data.getFloatOr("QuietusGrappleBlend", 0);
            if (blend > 0.01f) {
                float finalYaw = (arm == HumanoidArm.LEFT) ? -yaw : yaw;
                localArm = localArm.xRot(-pitch).yRot(-finalYaw);
            }

            // Rotate arm to world
            double worldArmX = localArm.x * cosYaw - localArm.z * sinYaw;
            double worldArmZ = localArm.x * sinYaw + localArm.z * cosYaw;
            Vec3 worldArm = new Vec3(worldArmX, localArm.y, worldArmZ);

            return shoulder.add(worldArm);
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

    private static void renderChain(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                    Vec3 startOffset, Vec3 chainVector, int packedLight) {
        float length = (float) chainVector.length();
        if (length < 1.0E-4F) {
            return;
        }

        Vec3 direction = chainVector.scale(1.0 / length);
        float xRot = (float) Math.acos(Mth.clamp(direction.y, -1.0, 1.0));
        float yRot = (float) (Math.PI / 2.0) - (float) Math.atan2(direction.z, direction.x);
        float maxV = length;

        poseStack.pushPose();
        poseStack.translate(startOffset.x, startOffset.y, startOffset.z);
        poseStack.mulPose(Axis.YP.rotation(yRot));
        poseStack.mulPose(Axis.XP.rotation(xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));

        submitNodeCollector.submitCustomGeometry(poseStack, CHAIN_RENDER_TYPE, (pose, buffer) -> {
            // These dimensions and U ranges match vanilla's block/chain model exactly.
            chainVertex(buffer, pose, -CHAIN_HALF_WIDTH, length, 0.0F, CHAIN_FIRST_STRIP_MIN_U, maxV, packedLight, 0.0F, 0.0F, 1.0F);
            chainVertex(buffer, pose, -CHAIN_HALF_WIDTH, 0.0F, 0.0F, CHAIN_FIRST_STRIP_MIN_U, 0.0F, packedLight, 0.0F, 0.0F, 1.0F);
            chainVertex(buffer, pose, CHAIN_HALF_WIDTH, 0.0F, 0.0F, CHAIN_FIRST_STRIP_MAX_U, 0.0F, packedLight, 0.0F, 0.0F, 1.0F);
            chainVertex(buffer, pose, CHAIN_HALF_WIDTH, length, 0.0F, CHAIN_FIRST_STRIP_MAX_U, maxV, packedLight, 0.0F, 0.0F, 1.0F);

            chainVertex(buffer, pose, 0.0F, length, -CHAIN_HALF_WIDTH, CHAIN_SECOND_STRIP_MIN_U, maxV, packedLight, 1.0F, 0.0F, 0.0F);
            chainVertex(buffer, pose, 0.0F, 0.0F, -CHAIN_HALF_WIDTH, CHAIN_SECOND_STRIP_MIN_U, 0.0F, packedLight, 1.0F, 0.0F, 0.0F);
            chainVertex(buffer, pose, 0.0F, 0.0F, CHAIN_HALF_WIDTH, CHAIN_SECOND_STRIP_MAX_U, 0.0F, packedLight, 1.0F, 0.0F, 0.0F);
            chainVertex(buffer, pose, 0.0F, length, CHAIN_HALF_WIDTH, CHAIN_SECOND_STRIP_MAX_U, maxV, packedLight, 1.0F, 0.0F, 0.0F);
        });
        poseStack.popPose();
    }

    private static void chainVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v, int packedLight,
                                    float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, normalX, normalY, normalZ);
    }
    @Override
    protected boolean affectedByCulling(GrapplingHookProjectile display) {
        return false;
    }

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/projectile/chain_hook_projectile.png");
    }
}
