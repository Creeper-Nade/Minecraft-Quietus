package com.quietus.client.model.mob;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.quietus.client.model.QuietusDataTickets;
import com.quietus.entity.monster.PlayerFragment;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import org.jspecify.annotations.Nullable;

import static com.quietus.client.model.mob.FragmentHeadLayer.createGrayscaleRenderType;

public class PlayerFragmentRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<PlayerFragment,R> {
    public PlayerFragmentRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerFragmentModel());
        withRenderLayer(new FragmentHeadLayer<>(this));
    }

    @Override
    public void captureDefaultRenderState(PlayerFragment animatable, Void relatedObject, R renderState, float partialTick) {
        super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        // Add the entity to the render state using our custom data ticket
        renderState.addGeckolibData(QuietusDataTickets.PLAYER_FRAGMENT_ENTITY, animatable);
    }

    @Override
    public int getPackedOverlay(PlayerFragment animatable, Void relatedObject, float u, float partialTick) {
        if (!(animatable instanceof LivingEntity entity))
            return OverlayTexture.NO_OVERLAY;

        if (animatable.isHurt()) {
            float whiteOverlayProgress = animatable.getWhiteOverlayProgress();

            // Use overlay system to create white flash
            return OverlayTexture.pack(OverlayTexture.u(whiteOverlayProgress), OverlayTexture.v(false));
        }
        return OverlayTexture.pack(OverlayTexture.u(u),
                OverlayTexture.v(entity.deathTime > 0));
    }
    /**
     * Override geometry submission to render the whole body twice when glowing:
     * once with grayscale, once with outline.
     */
    @Override
    public void submitRenderTasks(RenderPassInfo<R> renderPassInfo, OrderedSubmitNodeCollector renderTasks, @Nullable RenderType renderType) {
        // Obtain the texture and normal (grayscale) render type
        Identifier texture = getTextureLocation(renderPassInfo.renderState());
        RenderType grayscaleType = getRenderType(renderPassInfo.renderState(), texture);
        if (grayscaleType == null) return;

        // Submit grayscale geometry (normal body)
        submitGeometry(renderPassInfo, renderTasks, grayscaleType);

        // If glowing, also submit outline geometry
        if (renderPassInfo.renderState().appearsGlowing()) {
            RenderType outlineType = RenderTypes.outline(texture);
            submitGeometry(renderPassInfo, renderTasks, outlineType);
        }
    }

    /**
     * Helper that actually submits the model geometry with the given RenderType.
     * This replicates the default GeckoLib submission logic.
     */
    private void submitGeometry(RenderPassInfo<R> renderPassInfo, OrderedSubmitNodeCollector renderTasks, RenderType renderType) {
        final int packedLight = renderPassInfo.packedLight();
        final int packedOverlay = renderPassInfo.packedOverlay();
        final int renderColor = renderPassInfo.renderColor();
        final var model = renderPassInfo.model();

        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            final PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            renderPassInfo.renderPosed(() -> model.render(renderPassInfo, vertexConsumer, packedLight, packedOverlay, renderColor));
            poseStack.popPose();
        });
    }
    @Override
    public @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        if (renderState.isInvisible && !renderState.getOrDefaultGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, false))
            return RenderTypes.entityTranslucentCullItemTarget(texture);

        // Glowing entities: return grayscale (outline will be added as second pass)
        if (renderState.appearsGlowing()) {
            if (renderState.isInvisible)
                return RenderTypes.outline(texture);
            return createGrayscaleRenderType(texture);
        }

        return renderState.isInvisible ? null : createGrayscaleRenderType(texture);
    }



}
