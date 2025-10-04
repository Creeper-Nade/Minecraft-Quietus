package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.client.model.QuietusDataTickets;
import com.minecraftquietus.quietus.client.model.QuietusEmissiveLayer;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class PlayerGhostRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<PlayerGhost,R> {
    public PlayerGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerGhostModel());
        addRenderLayer(new GhostHeadLayer<>(this));
    }

    @Override
    public R captureDefaultRenderState(PlayerGhost animatable, Void relatedObject, R renderState, float partialTick) {
        renderState = super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);
        // Add the entity to the render state using our custom data ticket
        renderState.addGeckolibData(QuietusDataTickets.PLAYER_GHOST_ENTITY, animatable);
        return renderState;
    }

    @Override
    public int getPackedOverlay(PlayerGhost animatable, Void relatedObject, float u, float partialTick) {
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


}
