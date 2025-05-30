package com.minecraftquietus.quietus.client.model.projectile.magic;

import com.minecraftquietus.quietus.entity.projectiles.magic.MagicalProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class amethyst_projectile_renderer extends EntityRenderer<MagicalProjectile, magic_projectile_renderState> {

    private amethyst_projectile_model model;

    public amethyst_projectile_renderer(EntityRendererProvider.Context context) {
        super(context);
        this.model= new amethyst_projectile_model(context.bakeLayer(amethyst_projectile_model.LAYER_LOCATION));
    }

    // Tell the render engine how to create a new entity render state.
    @Override
    public magic_projectile_renderState createRenderState() {
        return new magic_projectile_renderState();
    }

    // Update the render state by copying the needed values from the passed entity to the passed state.
    // Both Entity and EntityRenderState may be replaced with more concrete types,
    // based on the generic types that have been passed to the supertype.
    @Override
    public void extractRenderState(MagicalProjectile entity, magic_projectile_renderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
        // Extract and store any additional values in the state here.
    }


    // Actually render the entity. The first parameter matches the render state's generic type.
    // Calling super will handle leash and name tag rendering for you, if applicable.
    @Override
    public void render(magic_projectile_renderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        //poseStack.translate(0.0F, -0.1F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(
                buffer, this.model.renderType(this.getTextureLocation()),false, true);


        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(state, poseStack, buffer, packedLight);
        // do your own rendering here
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/projectile/amethyst_projectile.png");
    }
}
