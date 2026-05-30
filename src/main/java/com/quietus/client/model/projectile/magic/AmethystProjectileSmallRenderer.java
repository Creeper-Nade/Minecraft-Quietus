package com.quietus.client.model.projectile.magic;

import com.quietus.entity.projectiles.QuietusProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import static com.quietus.Quietus.MODID;

public class AmethystProjectileSmallRenderer extends EntityRenderer<QuietusProjectile, ProjectileRenderState> {

    private AmethystProjectileSmallModel<ProjectileRenderState> model;

    public AmethystProjectileSmallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model= new AmethystProjectileSmallModel<>(context.bakeLayer(AmethystProjectileModel.LAYER_LOCATION));
    }

    // Tell the render engine how to create a new entity render state.
    @Override
    public ProjectileRenderState createRenderState() {
        return new ProjectileRenderState();
    }

    // Update the render state by copying the needed values from the passed entity to the passed state.
    // Both Entity and EntityRenderState may be replaced with more concrete types,
    // based on the generic types that have been passed to the supertype.
    @Override
    public void extractRenderState(QuietusProjectile entity, ProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
        // Extract and store any additional values in the state here.
    }


    // Actually render the entity. The first parameter matches the render state's generic type.
    // Calling super will handle leash and name tag rendering for you, if applicable.
    @Override
    public void submit(ProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        //poseStack.translate(0.0F, -0.1F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        submitNodeCollector.order(0).submitModel(this.model, state, poseStack, this.getTextureLocation(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
        submitNodeCollector.order(1).submitModel(this.model, state, poseStack, ItemFeatureRenderer.getFoilRenderType(this.model.renderType(this.getTextureLocation()), false), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
        // do your own rendering here
    }

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/projectile/amethyst_projectile_small.png");
    }
}
