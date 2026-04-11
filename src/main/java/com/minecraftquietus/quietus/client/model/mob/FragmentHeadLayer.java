package com.minecraftquietus.quietus.client.model.mob;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.RenderPassInfo;
import com.minecraftquietus.quietus.client.model.QuietusDataTickets;
import com.minecraftquietus.quietus.entity.monster.PlayerFragment;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoAnimatable;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.util.RenderUtil;

import java.util.Optional;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class FragmentHeadLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T,O,R> {
    private final SkullModel skullModel;

    public static RenderPipeline GRAY_SCALE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MODID, "grayscale"))
            .withVertexShader(Identifier.fromNamespaceAndPath(MODID, "core/grayscale"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(MODID, "core/grayscale"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("EMISSIVE")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .build();

    public FragmentHeadLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
        this.skullModel = new SkullModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_HEAD));
    }

    // NEW OVERRIDE: submitRenderTask replaces the old render method
    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        R renderState = renderPassInfo.renderState();

        // Get the entity from the render state using our custom data ticket
        if (!renderState.hasGeckolibData(QuietusDataTickets.PLAYER_FRAGMENT_ENTITY)) {
            return;
        }

        PlayerFragment ghost = renderState.getGeckolibData(QuietusDataTickets.PLAYER_FRAGMENT_ENTITY);

        getDefaultBakedModel(renderState).getBone("head").ifPresent(headBone -> {
            Identifier texture = ghost.getPlayerHeadTexture();
            RenderType renderType = getRenderType(renderState, texture);

            if (renderType != null) {
                // Submit custom geometry to the node collector
                renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
                    final PoseStack poseStack = renderPassInfo.poseStack();

                    poseStack.pushPose();
                    // Sync the pose stack with the geometry collector's current state
                    poseStack.last().set(pose);

                    // Execute model posing safely within Geckolib's isolated animation state
                    renderPassInfo.renderPosed(() -> {
                        // 1. Snaps the PoseStack EXACTLY to the coordinate space of the head bone.
                        // (This entirely replaces all your old parent grabbing and scaling math)
                        RenderUtil.transformToBone(poseStack, headBone);

                        // 2. Apply your custom offsets
                        // Note: Because the parent math is now accurate natively, you might want
                        // to test if `-0.8f` is still exactly what you need here.
                        poseStack.translate(0, -0.8f, 0);

                        // 3. Rotate to match ghost's head direction
                        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

                        // 4. Render the skull
                        skullModel.renderToBuffer(poseStack, vertexConsumer, renderPassInfo.packedLight(), renderPassInfo.packedOverlay());
                    });

                    poseStack.popPose();
                });
            }
        });
    }

    protected RenderType getRenderType(R renderState, Identifier texture) {
        if (!(renderState instanceof EntityRenderState entityRenderState))
            return createGrayscaleRenderType(texture, false);

        boolean invisible = entityRenderState.isInvisible;

        // Note: Make sure your DataTickets getters are up to date!
        if (invisible && Boolean.FALSE.equals(renderState.getGeckolibData(DataTickets.INVISIBLE_TO_PLAYER)))
            return RenderTypes.entityTranslucentCullItemTarget(texture);

        if (entityRenderState.appearsGlowing()) {
            if (invisible)
                return RenderTypes.outline(texture);

            return createGrayscaleRenderType(texture, true);
        }

        return invisible ? null : createGrayscaleRenderType(texture, false);
    }

    public static RenderType createGrayscaleRenderType(Identifier texture, boolean outline) {
        RenderSetup state = RenderSetup.builder(GRAY_SCALE).withTexture("Sampler0", texture).useLightmap().useOverlay().setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).sortOnUpload().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();

        return RenderType.create(
                "fragment_head_grayscale",
                state
        );
    }


}
