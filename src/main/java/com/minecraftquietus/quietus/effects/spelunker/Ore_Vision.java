package com.minecraftquietus.quietus.effects.spelunker;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.effects.QuietusMobEffects;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.joml.Matrix4fStack;

import static com.minecraftquietus.quietus.tags.QuietusTags.Blocks.SPELUNKABLE_ORES;

import java.util.*;

public class Ore_Vision {
    private static final Map<BlockPos, Block> VISIBLE_ORES = new HashMap<>();
    private static GpuBuffer vertexBuffer = null;
    private static int indexCount = 0;
    public static boolean needsRefresh = false;
    private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
    private static Vec3 lastPlayerPos = null;
    private static final double MOVEMENT_THRESHOLD_SQ = 0.1 * 0.1;

    public static RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
            .withLocation("pipeline/xray_lines")
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader(ResourceLocation.fromNamespaceAndPath("quietus", "core/orevision_line"))
            .withUniform("LineWidth", UniformType.FLOAT)
            .withUniform("ScreenSize", UniformType.VEC2)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build();


    public static void updateVisibleOres(LocalPlayer player) {
        VISIBLE_ORES.clear();
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        int range = Quietus.ConfigHandler.getRange(player);

        /*VISIBLE_ORES.keySet().removeIf(pos ->
                !player.level().getBlockState(pos).is(Tags.Blocks.ORES)
        );*/

        BlockPos.betweenClosedStream(
                player.blockPosition().offset(-range, -range, -range),
                player.blockPosition().offset(range, range, range)
        ).forEach(pos -> {
            BlockState state = player.level().getBlockState(pos);
            if (state.is(SPELUNKABLE_ORES)) {
                // Store BlockPos and Block type
                VISIBLE_ORES.put(pos.immutable(), state.getBlock());
            }
        });
    }

    public static void IfPlayerMoved(LocalPlayer player)
    {
        Vec3 currentPos = player.position();

        // First run or teleportation
        if (lastPlayerPos == null) {
            lastPlayerPos = currentPos;
            return;
        }

        // Check if player moved beyond threshold
        if (currentPos.distanceToSqr(lastPlayerPos) > MOVEMENT_THRESHOLD_SQ) {
            needsRefresh = true;
            lastPlayerPos = currentPos;
        }
    }

    public static void RemoveSingleBlock(BlockEvent.BreakEvent event)
    {
        VISIBLE_ORES.remove(event.getPos());
        needsRefresh = true;
    }
    public static void AddSingleBlock(BlockEvent.EntityPlaceEvent event)
    {
        BlockState placedState = event.getPlacedBlock();
        VISIBLE_ORES.put(event.getPos().immutable(),placedState.getBlock());
        needsRefresh = true;
    }

@SubscribeEvent
    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        PoseStack poseStack = event.getPoseStack();

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        if (player.hasEffect(QuietusMobEffects.SPELUNKING_EFFECT) && player != null) {
            // this is a world pos of the player
            Ore_Vision.updateVisibleOres(player);
            Ore_Vision.renderOreOutlines(poseStack);
        }
        else
        {
            Ore_Vision.clearAllOutlines();
            return;
        }
    }

    public static void renderOreOutlines(PoseStack poseStack) {
        // Disable depth test to see through walls
        //RenderPipeline pipeline= LINES_NO_DEPTH;
        //System.out.println("Whatever Text");
    /*if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
        return;
    }*/

        if(VISIBLE_ORES.isEmpty())
            {
                return;
            }
        // Set line thickness
        //RenderSystem.lineWidth(2.0f);
        RenderPipeline pipeline= LINES_NO_DEPTH;
        if (vertexBuffer == null || needsRefresh) {
            needsRefresh = false;

            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                    pipeline.getVertexFormatMode(), pipeline.getVertexFormat()
            );

            VISIBLE_ORES.forEach((pos, block) -> {
                int colorHex = OreColorRegistry.getColor(block);
                float r = ((colorHex >> 16) & 0xFF) / 255f;
                float g = ((colorHex >> 8) & 0xFF) / 255f;
                float b = (colorHex & 0xFF) / 255f;

                AABB box = new AABB(pos).inflate(-0.002);
                ShapeRenderer.renderLineBox(poseStack, bufferBuilder, box, r, g, b, 1.0f);
            });
            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                vertexBuffer = RenderSystem.getDevice()
                        .createBuffer(() -> "Xray vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());


                indexCount = meshData.drawState().indexCount();
            }
        }

        if (indexCount != 0) {
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().reverse();

            RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
            if (renderTarget.getColorTexture() == null) {
                return;
            }


            GpuBuffer gpuBuffer = indices.getBuffer(indexCount);
            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(renderTarget.getColorTexture(), OptionalInt.empty(), renderTarget.getDepthTexture(), OptionalDouble.empty())) {

                Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
                matrix4fStack.pushMatrix();
                matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());

                renderPass.setPipeline(pipeline);
                renderPass.setIndexBuffer(gpuBuffer, indices.type());
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.drawIndexed(0, indexCount);

                matrix4fStack.popMatrix();
            }
        }
    }

    public static void clearAllOutlines() {
        VISIBLE_ORES.clear();
        needsRefresh = true;

        // Release GPU resources
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        indexCount = 0;
    }

}
