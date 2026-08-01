package com.quietus.client.hud;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

/** A position-only GUI quad compatible with Minecraft's vanilla End Portal pipeline. */
public record EndPortalGuiRenderState(
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x0,
        int y0,
        int x1,
        int y1,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public EndPortalGuiRenderState(TextureSetup textureSetup, Matrix3x2f pose,
                                   int x0, int y0, int x1, int y1) {
        this(textureSetup, pose, x0, y0, x1, y1, null,
                new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.END_PORTAL;
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        vertices.addVertexWith2DPose(this.pose, this.x0, this.y0);
        vertices.addVertexWith2DPose(this.pose, this.x0, this.y1);
        vertices.addVertexWith2DPose(this.pose, this.x1, this.y1);
        vertices.addVertexWith2DPose(this.pose, this.x1, this.y0);
    }
}
