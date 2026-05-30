package com.quietus.client.model;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import com.geckolib.animatable.GeoAnimatable;

import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;

public class QuietusEmissiveLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends AutoGlowingGeoLayer<T,O,R> {
    public QuietusEmissiveLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }
    @Override
    protected boolean shouldRespectWorldLighting(R renderState) {
        return true;
    }

}
