package com.minecraftquietus.quietus.client.model.projectile.magic;

import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class AmethystProjectileRenderer extends EntityRenderer<QuietusProjectile, ProjectileRenderState> {

    // Specify the generic type to match your render state
    private AmethystProjectileModel<ProjectileRenderState> model;

    public AmethystProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new AmethystProjectileModel<>(context.bakeLayer(AmethystProjectileModel.LAYER_LOCATION));
    }

    @Override
    public ProjectileRenderState createRenderState() {
        return new ProjectileRenderState();
    }

    @Override
    public void extractRenderState(QuietusProjectile entity, ProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
    }

    @Override
    public void submit(ProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        // Pass the actual render state (state) instead of Unit.INSTANCE
        submitNodeCollector.order(0).submitModel(this.model, state, poseStack, this.getTextureLocation(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);
        submitNodeCollector.order(1).submitModel(this.model, state, poseStack, ItemFeatureRenderer.getFoilRenderType(this.model.renderType(this.getTextureLocation()), false), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/projectile/amethyst_projectile.png");
    }
}