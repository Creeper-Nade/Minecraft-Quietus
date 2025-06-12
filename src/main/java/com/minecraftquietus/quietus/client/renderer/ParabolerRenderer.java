package com.minecraftquietus.quietus.client.renderer;

import com.minecraftquietus.quietus.entity.monster.Paraboler;

import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.ModelLayers;

public class ParabolerRenderer extends AbstractSkeletonRenderer<Paraboler, SkeletonRenderState> {
    private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public ParabolerRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public ResourceLocation getTextureLocation(SkeletonRenderState renderState) {
        return SKELETON_LOCATION;
    }

    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}