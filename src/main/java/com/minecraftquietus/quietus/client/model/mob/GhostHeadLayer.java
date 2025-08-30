package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.client.model.QuietusDataTickets;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;

public class GhostHeadLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T,O,R> {
    public GhostHeadLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void render(GeoRenderState renderState, PoseStack poseStack, BakedGeoModel bakedModel,
                       @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                       int packedLight, int packedOverlay, int renderColor) {
        // Get the entity from the render state using our custom data ticket
        if (renderState.hasGeckolibData(QuietusDataTickets.PLAYER_GHOST_ENTITY)) {
            PlayerGhost ghost = renderState.getGeckolibData(QuietusDataTickets.PLAYER_GHOST_ENTITY);
            ItemStack headStack = ghost.getPlayerHead();

            if (headStack.getItem() instanceof PlayerHeadItem) {
                // Get head bone and apply transformations
                Optional<GeoBone> headBone = getGeoModel().getBone("head");
                headBone.ifPresent(bone -> {
                    poseStack.pushPose();

                    // Transform to bone position
                    RenderUtil.translateToPivotPoint(poseStack, bone);
                    RenderUtil.rotateMatrixAroundBone(poseStack, bone);

                    // Adjust these values to position the head correctly
                    // Adjust position to match ghost's head
                    poseStack.translate(0, 0.6, 0); // Adjust Y position
                    poseStack.scale(1.1f, 1.1f, 1.1f);

                    // Rotate to match ghost's head direction
                    poseStack.mulPose(Axis.YP.rotationDegrees(180)); // Fix opposite direction

                    // Create grayscale tint (0 saturation - black, gray, white)
                    int grayscale = 0xFF808080; // Medium gray

                    // Create a custom render type with grayscale tint
                    RenderType tintedRenderType = RenderType.entityTranslucent(getTextureResource((R) renderState));
                    VertexConsumer tintedBuffer = bufferSource.getBuffer(tintedRenderType).setColor(grayscale);
                    //MultiBufferSource aa = renderType1 -> ;

                    // Render player head with tint
                    Minecraft mc = Minecraft.getInstance();
                    ItemRenderer itemRenderer = mc.getItemRenderer();

                    //itemRenderer.renderStatic(headStack, ItemDisplayContext.HEAD, packedLight,
                      //      packedOverlay, poseStack, aa, ghost.level(), 0);

                    // Apply the grayscale tint by modifying the render color
                    int tintedColor = applyGrayscaleTint(renderColor, grayscale);

                    // You'll need to create a custom method to render with tint
                    // Create a custom buffer source that applies grayscale
                    MultiBufferSource grayscaleBufferSource = new GrayscaleMultiBufferSource(bufferSource);

                    // Render the item with our custom grayscale buffer
                    itemRenderer.renderStatic(headStack, ItemDisplayContext.HEAD, packedLight,
                            OverlayTexture.NO_OVERLAY, poseStack,
                            grayscaleBufferSource, null, 0);

                    poseStack.popPose();
                });
            }
        }
    }
    private int applyGrayscaleTint(int originalColor, int grayscale) {
        // Extract RGB components from grayscale
        int r = (grayscale >> 16) & 0xFF;
        int g = (grayscale >> 8) & 0xFF;
        int b = grayscale & 0xFF;

        // Apply grayscale tint to the original color
        return (originalColor & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /**
     * Custom MultiBufferSource that applies a grayscale effect to all rendered items
     */
    private static class GrayscaleMultiBufferSource implements MultiBufferSource {
        private final MultiBufferSource original;

        public GrayscaleMultiBufferSource(MultiBufferSource original) {
            this.original = original;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            VertexConsumer originalBuffer = original.getBuffer(renderType);
            return new GrayscaleVertexConsumer(originalBuffer);
        }
    }

    /**
     * Custom VertexConsumer that converts colors to grayscale
     */
    private static class GrayscaleVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        public GrayscaleVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return delegate.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            // Convert to grayscale using luminance formula
            float gray = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
            int grayInt = (int)(gray * 255);
            return delegate.setColor(grayInt, grayInt, grayInt, a);
        }

        @Override
        public VertexConsumer setUv(float v, float v1) {
            return delegate.setUv(v, v1);
        }

        @Override
        public VertexConsumer setColor(float r, float g, float b, float a) {
            // Convert to grayscale using luminance formula
            float gray = r * 0.299f + g * 0.587f + b * 0.114f;
            return delegate.setColor(gray, gray, gray, a);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return delegate.setNormal(x, y, z);
        }

        @Override
        public VertexConsumer setLight(int packedLight) {
            return delegate.setLight(packedLight);
        }


        @Override
        public VertexConsumer setOverlay(int packedOverlay) {
            return delegate.setOverlay(packedOverlay);
        }


    }

}
