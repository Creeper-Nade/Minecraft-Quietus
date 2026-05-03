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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
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
    public void submit(GrapplingHookRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        //poseStack.translate(0.0F, 0.0F, 0.0F);
        poseStack.translate(-0.5F / 16.0F, 5.0F / 16.0F, 0.0F); // Convert pixels to blocks (1/16th of a block)
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, this.getTextureLocation(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
        poseStack.popPose();

        // Render the line (rope) if we have a valid offset
        if (state.lineOriginOffset != null && !state.lineOriginOffset.equals(Vec3.ZERO)) {
            float xa = (float) state.lineOriginOffset.x;
            float ya = (float) state.lineOriginOffset.y;
            float za = (float) state.lineOriginOffset.z;
            float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;

            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                int steps = 16;

                for(int i = 0; i < 16; ++i) {
                    float a0 = fraction(i, 16);
                    float a1 = fraction(i + 1, 16);
                    stringVertex(xa, ya, za, buffer, pose, a0, a1, width);
                    stringVertex(xa, ya, za, buffer, pose, a1, a0, width);
                }

            });

        }
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
        // do your own rendering here
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

    private static float fraction(int numerator, int denominator) {
        return (float) numerator / (float) denominator;
    }

    private static void stringVertex(float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose,
                                     float stringFraction, float nextStringFraction,float width) {
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

        consumer.addVertex(pose, x, y, z).setColor(-16777216).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
    @Override
    protected boolean affectedByCulling(GrapplingHookProjectile display) {
        return false;
    }

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/projectile/chain_hook_projectile.png");
    }
}
