package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class PlayerGhostModel extends GeoModel<PlayerGhost> {
    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "mob/player_ghost");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/mob/player_ghost.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerGhost animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "player_ghost");
    }

    @Override
    public RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return GhostHeadLayer.createGrayscaleRenderType(texture,true);
    }



}
