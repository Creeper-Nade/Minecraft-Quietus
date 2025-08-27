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

                    // Apply grayscale tint
                    int grayscale = 0xFF808080; // Medium gray
                    VertexConsumer tintedBuffer = bufferSource.getBuffer(
                            RenderType.entityTranslucent(getTextureResource((R) renderState))
                    );

                    // Render player head with tint
                    Minecraft mc = Minecraft.getInstance();
                    ItemRenderer itemRenderer = mc.getItemRenderer();

                    itemRenderer.renderStatic(headStack, ItemDisplayContext.HEAD, packedLight,
                            packedOverlay, poseStack, bufferSource, ghost.level(), 0);

                    poseStack.popPose();
                });
            }
        }
    }
}
