package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.client.model.QuietusDataTickets;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class GhostHeadLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T,O,R> {
    private final SkullModel skullModel;

    public static RenderPipeline GRAY_SCALE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath(MODID, "grayscale"))
            .withVertexShader(ResourceLocation.fromNamespaceAndPath("quietus", "core/grayscale"))
            .withFragmentShader(ResourceLocation.fromNamespaceAndPath("quietus", "core/grayscale"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("EMISSIVE")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withSampler("Sampler1")
            .withBlend(BlendFunction.TRANSLUCENT)

            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
            .build();

    public GhostHeadLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
        // Create the skull model once
        this.skullModel = new SkullModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_HEAD));
    }

    @Override
    public void render(GeoRenderState renderState, PoseStack poseStack, BakedGeoModel bakedModel,
                       @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                       int packedLight, int packedOverlay, int renderColor) {
        // Get the entity from the render state using our custom data ticket
        if (renderState.hasGeckolibData(QuietusDataTickets.PLAYER_GHOST_ENTITY)) {
            PlayerGhost ghost = renderState.getGeckolibData(QuietusDataTickets.PLAYER_GHOST_ENTITY);
                // Get head bone and apply transformations
                Optional<GeoBone> headBone = getGeoModel().getBone("head");
                headBone.ifPresent(bone -> {
                    poseStack.pushPose();

                    // Transform to bone position
                    RenderUtil.translateToPivotPoint(poseStack,bone);
                    RenderUtil.translateMatrixToBone(poseStack, bone);
                    RenderUtil.rotateMatrixAroundBone(poseStack, bone);

                    // Adjust these values to position the head correctly
                    // Adjust position to match ghost's head
                    //poseStack.translate(0, 0, 1.6); // Adjust Y position
                    //poseStack.translate(0, 0, 1.6); // Adjust Y position
                    //poseStack.scale(1.1f, 1.1f, 1.1f);

                    // Rotate to match ghost's head direction
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180)); // Fix opposite direction
                    // Get the player head texture
                    ResourceLocation texture = ghost.getPlayerHeadTexture();

                    // Use our custom grayscale render type
                    RenderType renderType1= getRenderType((R) renderState,texture);
                    if(renderType1!=null)
                    {
                        VertexConsumer vertexConsumer = bufferSource.getBuffer(
                                renderType1
                        );
                        skullModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);
                    }


                    poseStack.popPose();
                });
                //super.render((R) renderState,poseStack,bakedModel,getRenderType((R) renderState,null),bufferSource,buffer,packedLight,packedOverlay,renderColor);
        }
    }

    protected RenderType getRenderType(R renderState, ResourceLocation texture) {

        if (!(renderState instanceof EntityRenderState entityRenderState))
            return createGrayscaleRenderType(texture,false);

        boolean invisible = entityRenderState.isInvisible;

        if (invisible && Boolean.FALSE.equals(renderState.getOrDefaultGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, false)))
            return RenderType.itemEntityTranslucentCull(texture);

        if (Boolean.TRUE.equals(renderState.getOrDefaultGeckolibData(DataTickets.IS_GLOWING, false))) {
            if (invisible)
                return RenderType.outline(texture);

            return createGrayscaleRenderType(texture,true);
        }

        return invisible ? null : createGrayscaleRenderType(texture,false);
    }

    public static RenderType createGrayscaleRenderType(ResourceLocation texture, boolean outline) {
        // Create a custom render type with grayscale effect
        return RenderType.create(
                "ghost_head_grayscale",
                256,
                false,
                true,
                GRAY_SCALE,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .createCompositeState(outline)
        );
    }


}
