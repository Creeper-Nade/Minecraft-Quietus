package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.entity.monster.PlayerFragment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class PlayerFragmentModel extends GeoModel<PlayerFragment> {
    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "mob/player_fragment");
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/mob/player_fragment.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerFragment animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "player_fragment");
    }

    @Override
    public RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return FragmentHeadLayer.createGrayscaleRenderType(texture,true);
    }



}
