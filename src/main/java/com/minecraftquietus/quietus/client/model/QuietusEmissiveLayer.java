package com.minecraftquietus.quietus.client.model;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuietusEmissiveLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends AutoGlowingGeoLayer<T,O,R> {
    //ignore this whole class for now
    public QuietusEmissiveLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    protected static final TriFunction<ResourceLocation, Boolean, Boolean, RenderType> FIXED_RENDER_TYPE = memoizeRenderType(QuietusEmissiveLayer::createRenderType);

    public static RenderPipeline FIXED_EMISSIVE = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_FOG_SNIPPET)
            .withLocation("pipeline/fixed_emissive")
            .withVertexShader("core/entity")
            .withFragmentShader("core/entity")
            .withShaderDefine("EMISSIVE")
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withSampler("Sampler0")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
            .build();

/*
    @Override
    protected RenderType getRenderType(R renderState) {
        ResourceLocation texture = getTextureResource(renderState);
        boolean respectLighting = shouldRespectWorldLighting();

        if (!(renderState instanceof EntityRenderState entityRenderState))
            return FIXED_RENDER_TYPE.apply(texture, false, respectLighting);

        boolean invisible = entityRenderState.isInvisible;

        if (invisible && !renderState.getOrDefaultGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, false))
            return RenderType.itemEntityTranslucentCull(texture);

        if (renderState.getOrDefaultGeckolibData(DataTickets.IS_GLOWING, false)) {
            if (invisible)
                return RenderType.outline(texture);

            return FIXED_RENDER_TYPE.apply(texture, true, respectLighting);
        }

        return invisible ? null : FIXED_RENDER_TYPE.apply(texture, false, respectLighting);
    }*/
    @Override
    protected boolean shouldRespectWorldLighting() {
        return true;
    }

    private static RenderType createRenderType(ResourceLocation texture, boolean outline, boolean respectLighting) {
        return respectLighting ? RenderType.entityTranslucentEmissive(texture, outline) :
                RenderType.create("geckolib_emissive",
                        RenderType.TRANSIENT_BUFFER_SIZE,
                        false,
                        true,
                        FIXED_EMISSIVE,
                        RenderType.CompositeState.builder()
                                .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                                .createCompositeState(outline));
    }

    private static <T, O, L, R> TriFunction<T, O, L, R> memoizeRenderType(final TriFunction<T, O, L, R> function) {
        return new TriFunction<>() {
            private final Map<Triple<T, O, L>, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T texture, O outline, L respectLighting) {
                return this.cache.computeIfAbsent(Triple.of(texture, outline, respectLighting), triple -> function.apply(triple.getLeft(), triple.getMiddle(), triple.getRight()));
            }

            @Override
            public String toString() {
                return "memoize/3[function=" + function + ", size=" + this.cache.size() + "]";
            }
        };
    }

    @Override
    protected RenderType getRenderType(R renderState) {
        ResourceLocation texture = getTextureResource(renderState);

        return RenderType.eyes(texture);
    }

    @Override
    public void render(R renderState, PoseStack poseStack, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                       int packedLight, int packedOverlay, int renderColor) {
        super.render(renderState, poseStack, bakedModel, renderType, bufferSource, buffer, LightTexture.FULL_SKY, packedOverlay, renderColor);
    }

}
