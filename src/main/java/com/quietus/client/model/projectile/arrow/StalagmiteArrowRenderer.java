package com.quietus.client.model.projectile.arrow;

import com.quietus.Quietus;
import com.quietus.entity.projectiles.StalagmiteArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;


public class StalagmiteArrowRenderer extends ArrowRenderer<StalagmiteArrow, ArrowRenderState> {
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(Quietus.MODID, "textures/entity/projectile/stalagmite_arrow_projectile.png");

    public StalagmiteArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE_LOCATION;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
