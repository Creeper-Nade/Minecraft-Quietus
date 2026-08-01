package com.quietus.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quietus.entity.monster.SkeletonAppearance;
import com.quietus.entity.monster.ThemedSkeleton;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public abstract class ThemedSkeletonRenderer<T extends ThemedSkeleton>
        extends AbstractSkeletonRenderer<T, ThemedSkeletonRenderState> {
    private static final Identifier SKELETON_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    private static final Identifier STRAY_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/skeleton/stray.png");
    private static final Identifier STRAY_OVERLAY =
            Identifier.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png");
    private static final Identifier BOGGED_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/skeleton/bogged.png");
    private static final Identifier BOGGED_OVERLAY =
            Identifier.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");
    private static final Identifier PARCHED_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/skeleton/parched.png");

    private final AbstractSkeletonRenderer<T, SkeletonRenderState> skeletonRenderer;
    private final AbstractSkeletonRenderer<T, SkeletonRenderState> strayRenderer;
    private final AbstractSkeletonRenderer<T, BoggedRenderState> boggedRenderer;
    private final AbstractSkeletonRenderer<T, SkeletonRenderState> parchedRenderer;

    protected ThemedSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_ARMOR);
        this.skeletonRenderer = simpleRenderer(
                context, ModelLayers.SKELETON, ModelLayers.SKELETON_ARMOR, SKELETON_TEXTURE);
        this.strayRenderer = simpleRenderer(
                context, ModelLayers.STRAY, ModelLayers.STRAY_ARMOR, STRAY_TEXTURE);
        this.strayRenderer.addLayer(new SkeletonClothingLayer<>(
                this.strayRenderer, context.getModelSet(), ModelLayers.STRAY_OUTER_LAYER, STRAY_OVERLAY));

        this.boggedRenderer = new AbstractSkeletonRenderer<T, BoggedRenderState>(
                context, ModelLayers.BOGGED_ARMOR, new BoggedModel(context.bakeLayer(ModelLayers.BOGGED))) {
            @Override
            public Identifier getTextureLocation(BoggedRenderState state) {
                return BOGGED_TEXTURE;
            }

            @Override
            public BoggedRenderState createRenderState() {
                return new BoggedRenderState();
            }
        };
        this.boggedRenderer.addLayer(new SkeletonClothingLayer<>(
                this.boggedRenderer, context.getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, BOGGED_OVERLAY));

        this.parchedRenderer = simpleRenderer(
                context, ModelLayers.PARCHED, ModelLayers.PARCHED_ARMOR, PARCHED_TEXTURE);
    }

    private AbstractSkeletonRenderer<T, SkeletonRenderState> simpleRenderer(
            EntityRendererProvider.Context context,
            net.minecraft.client.model.geom.ModelLayerLocation body,
            ArmorModelSet<net.minecraft.client.model.geom.ModelLayerLocation> armor,
            Identifier texture) {
        return new AbstractSkeletonRenderer<T, SkeletonRenderState>(context, body, armor) {
            @Override
            public Identifier getTextureLocation(SkeletonRenderState state) {
                return texture;
            }

            @Override
            public SkeletonRenderState createRenderState() {
                return new SkeletonRenderState();
            }
        };
    }

    @Override
    public Identifier getTextureLocation(ThemedSkeletonRenderState state) {
        return switch (state.appearance) {
            case STRAY -> STRAY_TEXTURE;
            case BOGGED -> BOGGED_TEXTURE;
            case PARCHED -> PARCHED_TEXTURE;
            default -> SKELETON_TEXTURE;
        };
    }

    @Override
    public ThemedSkeletonRenderState createRenderState() {
        return new ThemedSkeletonRenderState();
    }

    @Override
    public void extractRenderState(T entity, ThemedSkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.appearance = entity.getSkeletonAppearance();
        state.isSheared = entity.isSheared();
    }

    @Override
    public void submit(
            ThemedSkeletonRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        switch (state.appearance) {
            case STRAY -> this.strayRenderer.submit(state, poseStack, submitNodeCollector, camera);
            case BOGGED -> this.boggedRenderer.submit(state, poseStack, submitNodeCollector, camera);
            case PARCHED -> this.parchedRenderer.submit(state, poseStack, submitNodeCollector, camera);
            default -> this.skeletonRenderer.submit(state, poseStack, submitNodeCollector, camera);
        }
    }
}
