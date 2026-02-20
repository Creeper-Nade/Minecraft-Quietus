package com.minecraftquietus.quietus.client.model.projectile.misc;

import com.minecraftquietus.quietus.client.model.projectile.magic.AmethystProjectileModel;
import com.minecraftquietus.quietus.client.model.projectile.magic.ProjectileRenderState;
import com.minecraftquietus.quietus.entity.projectiles.QuietusProjectile;
import com.minecraftquietus.quietus.entity.projectiles.misc.GrapplingHookProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ChainHookRenderer extends EntityRenderer<GrapplingHookProjectile, ProjectileRenderState> {

    private ChainHookModel model;

    public ChainHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model= new ChainHookModel(context.bakeLayer(ChainHookModel.LAYER_LOCATION));
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
    public void extractRenderState(GrapplingHookProjectile entity, ProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
        // Extract and store any additional values in the state here.
    }


    // Actually render the entity. The first parameter matches the render state's generic type.
    // Calling super will handle leash and name tag rendering for you, if applicable.
    @Override
    public void render(ProjectileRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        //poseStack.translate(0.0F, 0.0F, 0.0F);
        poseStack.translate(-0.5F / 16.0F, 5.0F / 16.0F, 0.0F); // Convert pixels to blocks (1/16th of a block)
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(
                buffer, this.model.renderType(this.getTextureLocation()),false, false);


        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(state, poseStack, buffer, packedLight);
        // do your own rendering here
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/projectile/chain_hook_projectile.png");
    }
}
