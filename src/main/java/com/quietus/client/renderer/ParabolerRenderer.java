package com.quietus.client.renderer;

import com.quietus.entity.monster.Paraboler;

import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.model.geom.ModelLayers;

public class ParabolerRenderer extends AbstractSkeletonRenderer<Paraboler, SkeletonRenderState> {
    private static final Identifier SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public ParabolerRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_ARMOR);
    }

    public Identifier getTextureLocation(SkeletonRenderState renderState) {
        return SKELETON_LOCATION;
    }

    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}