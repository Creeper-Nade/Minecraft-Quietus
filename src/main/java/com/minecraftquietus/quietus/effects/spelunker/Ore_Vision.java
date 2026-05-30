package com.minecraftquietus.quietus.effects.spelunker;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.effects.QuietusMobEffects;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import static com.minecraftquietus.quietus.Quietus.MODID;
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

    public static RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation("pipeline/xray_lines")
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader(Identifier.fromNamespaceAndPath(MODID, "core/orevision_line"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
            //view https://github.com/neoforged/.github/blob/89b8119f521b3c60e5d736828e606918d1594d1a/primers/26.1/index.md#pipeline-depth-and-color for depth and blend change
            .withDepthStencilState(new DepthStencilState(CompareOp.NEVER_PASS, false))
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
    public static void onWorldRenderLast(RenderLevelStageEvent.AfterWeather event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        PoseStack poseStack = event.getPoseStack();

        if (player.hasEffect(QuietusMobEffects.SPELUNKING_EFFECT) && player != null) {
            // this is a world pos of the player
            Ore_Vision.updateVisibleOres(player);
            Ore_Vision.renderOreOutlines(poseStack);
        }
        else
        {
            Ore_Vision.clearAllOutlines();
        }
    }

    public static void renderOreOutlines(PoseStack poseStack) {
        if (VISIBLE_ORES.isEmpty()) return;

        if (vertexBuffer == null || needsRefresh) {
            needsRefresh = false;
            if (vertexBuffer != null) vertexBuffer.close();

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                    LINES_NO_DEPTH.getVertexFormatMode(),
                    LINES_NO_DEPTH.getVertexFormat()
            );

            for (Map.Entry<BlockPos, Block> entry : VISIBLE_ORES.entrySet()) {
                BlockPos pos = entry.getKey();
                Block block = entry.getValue();
                int colorHex = OreColorRegistry.getColor(block);
                float r = ((colorHex >> 16) & 0xFF) / 255f;
                float g = ((colorHex >> 8) & 0xFF) / 255f;
                float b = (colorHex & 0xFF) / 255f;

                AABB box = new AABB(pos).inflate(-0.002); // slight shrink to avoid Z-fighting
                addBoxEdges(bufferBuilder, box, r, g, b);
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Spelunker vertex buffer",
                        GpuBuffer.USAGE_VERTEX,
                        meshData.vertexBuffer()
                );
                indexCount = meshData.drawState().indexCount();
            }
        }

        if (indexCount == 0) return;

        // Transform: translate by negative camera (world to view)
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.translate((float) -cameraPos.x(), (float) -cameraPos.y(), (float) -cameraPos.z());
        GpuBufferSlice[] dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransforms(
                new DynamicUniforms.Transform(
                        new Matrix4f(matrixStack),
                        new Vector4f(1, 1, 1, 1),
                        new Vector3f(),
                        new Matrix4f()
                )
        );
        // Do NOT pop matrix here – keep it for the render pass

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "spelunker",
                        mainTarget.getColorTextureView(),
                        OptionalInt.empty(),
                        mainTarget.getDepthTextureView(),
                        OptionalDouble.empty())) {

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setPipeline(LINES_NO_DEPTH);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
            renderPass.setUniform("DynamicTransforms", dynamicTransforms[0]);
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        matrixStack.popMatrix();
    }

    // Helper methods
    private static void addBoxEdges(BufferBuilder builder, AABB box, float r, float g, float b) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Bottom face
        addLine(builder, minX, minY, minZ, maxX, minY, minZ, r, g, b);
        addLine(builder, maxX, minY, minZ, maxX, minY, maxZ, r, g, b);
        addLine(builder, maxX, minY, maxZ, minX, minY, maxZ, r, g, b);
        addLine(builder, minX, minY, maxZ, minX, minY, minZ, r, g, b);
        // Top face
        addLine(builder, minX, maxY, minZ, maxX, maxY, minZ, r, g, b);
        addLine(builder, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b);
        addLine(builder, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b);
        addLine(builder, minX, maxY, maxZ, minX, maxY, minZ, r, g, b);
        // Vertical edges
        addLine(builder, minX, minY, minZ, minX, maxY, minZ, r, g, b);
        addLine(builder, maxX, minY, minZ, maxX, maxY, minZ, r, g, b);
        addLine(builder, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b);
        addLine(builder, minX, minY, maxZ, minX, maxY, maxZ, r, g, b);
    }

    private static void addLine(BufferBuilder builder, float x1, float y1, float z1,
                                float x2, float y2, float z2, float r, float g, float b) {
        builder.addVertex(x1, y1, z1)
                .setColor(r, g, b, 1.0f)
                .setNormal(0, 1, 0)
                .setLineWidth(2.0f);   // <-- add this
        builder.addVertex(x2, y2, z2)
                .setColor(r, g, b, 1.0f)
                .setNormal(0, 1, 0)
                .setLineWidth(2.0f);   // <-- add this
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
