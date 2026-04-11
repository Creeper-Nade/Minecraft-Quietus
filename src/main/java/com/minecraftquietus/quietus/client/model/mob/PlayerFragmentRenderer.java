package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.client.model.QuietusDataTickets;
import com.minecraftquietus.quietus.entity.monster.PlayerFragment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

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


}
