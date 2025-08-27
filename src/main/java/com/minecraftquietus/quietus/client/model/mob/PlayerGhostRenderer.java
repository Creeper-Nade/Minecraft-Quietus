package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.client.model.QuietusDataTickets;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

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
}
