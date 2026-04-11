package com.minecraftquietus.quietus.client.model.mob;

import com.minecraftquietus.quietus.entity.monster.PlayerFragment;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class PlayerFragmentModel extends GeoModel<PlayerFragment> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "mob/player_fragment");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/mob/player_fragment.png");
    }

    @Override
    public Identifier getAnimationResource(PlayerFragment animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "player_fragment");
    }


}
