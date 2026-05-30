package com.minecraftquietus.quietus.core.DeathRevamp;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minecraftquietus.quietus.client.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.util.PlayerClientPacketDistributor;
import com.minecraftquietus.quietus.util.sound.EntitySoundSource;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Mth;
import net.minecraft.util.StrictJsonParser;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static com.minecraftquietus.quietus.Quietus.MODID;
@EventBusSubscriber(modid=MODID)
public class GhostDeath{
    private static Component deathMessage;
    private static int reviveCooldown;
    private static int playerScore;
    private static float pulseScale = 1.0f;
    private static int pulseColor = 0x00FF00;
    private static int lastSecond=0;
    private static int animation_cd_tick = 0;
    private static float fade=0;
    private static boolean animation_flag=false;
    private static boolean animation_completed=false;
    private static float IntensityAddition =0.02f;
    private static boolean hardcore;

    private static GhostAmbientSound AmbientSound;
    private static GhostAmbientSound MoodSound;


    public static void showScreen(Component causeOfDeath) {
        Minecraft.getInstance().setScreen(null);
        deathMessage = causeOfDeath;
        reviveCooldown = ClientPayloadHandler.getInstance().getMaxReviveCD();
        playerScore = Minecraft.getInstance().player.getScore();
        animation_cd_tick = 0;
        animation_flag=true;
        animation_completed=false;
        hardcore= ClientPayloadHandler.getInstance().getHardcore();
        vignetteIntensity=0.8f;
        fade=0;
        IntensityAddition =0.02f;
        pulseScale = 1.0f;
        pulseColor = 0x00FF00; // Reset to green
        AmbientSound = new GhostAmbientSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP.value());
        MoodSound = new GhostAmbientSound(SoundEvents.AMBIENT_WARPED_FOREST_MOOD.value());
        Minecraft.getInstance().getSoundManager().play(AmbientSound);
        Minecraft.getInstance().getSoundManager().play(MoodSound);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag nbt= player.getPersistentData();

        HolderLookup.Provider registries = player.level().registryAccess();
        String json = nbt.getStringOr("deathMessage","death.attack.generic");
        JsonElement element = LenientJsonParser.parse(json);
        //change old Component.Serializer.FromJson
        Component deathMessage = ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), element).getOrThrow(JsonParseException::new);

        Boolean isGhost = nbt.getBooleanOr("isGhost",false);
        boolean hardcore = player.level().getLevelData().isHardcore();
        int ReviveCD = nbt.getIntOr("reviveCooldown",0);

        PlayerClientPacketDistributor.sendGhostPackToPlayer(player,isGhost,deathMessage,ReviveCD,hardcore);
        PlayerClientPacketDistributor.sendRevivalCDToPlayer(player,ReviveCD);

    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)||!animation_flag) return;

        if(animation_cd_tick<=20) {
            animation_cd_tick++;

        }
        else
        {
            //reset
            animation_cd_tick=0;
            animation_flag=false;
            animation_completed=true;
        }

    }
    //For countdown ui
    @SubscribeEvent
    public static void render(RenderGuiEvent.Pre event) {
        if (!ClientPayloadHandler.getInstance().getGhostState() && fade <= 0) return;
        GuiGraphicsExtractor GuiGraphicsExtractor = event.getGuiGraphics();

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // cd
        int cd = ClientPayloadHandler.getInstance().GetReviveCD();
        int seconds= cd/20;
        //bug fix: putting sound reset at the earlier part of code
        if(seconds<=1)
        {
            AmbientSound.fadeOut();
            MoodSound.fadeOut();
        }

        // Calculate fade factor (fade in during first second, out during last second)
        fade = 1.0f;
        if (cd==reviveCooldown && !animation_completed) {
            animation_flag=true;
            fade = Math.max(0.0f, animation_cd_tick / 20f);
        } else if(cd==0) {
            animation_flag=true;
            fade = Math.min(1.0f, (10-animation_cd_tick) / 10f);
            if(fade<=0)
            {
                animation_cd_tick=0;
                animation_flag=false;
                cleanup();
            }
        }

        if(fade<=0)
        {
            return;
        }
        int alpha = (int)(fade * 255);

        // Render "You Died" title
        GuiGraphicsExtractor.pose().pushMatrix();
        GuiGraphicsExtractor.pose().scale(2.0F, 2.0F);
        Component title = Component.translatable("deathScreen.title").withStyle(ChatFormatting.BOLD);
        int titleWidth = mc.font.width(title);
        int titleX = (width / 2 - titleWidth) / 2;
        GuiGraphicsExtractor.text(mc.font, title, titleX, 30, 0xFF5555 | (alpha << 24), false);
        GuiGraphicsExtractor.pose().popMatrix();

        // Render cause of death
        if (deathMessage != null) {
            int deathY = 85;
            int deathX = width / 2 - mc.font.width(deathMessage) / 2;
            GuiGraphicsExtractor.text(mc.font, deathMessage, deathX, deathY, 0xFF5555 | (alpha << 24), false);
        }

        // Render score
        if(hardcore)
        {
            return;
        }
        Component scoreText = Component.translatable("deathScreen.score.value",
                Component.literal(Integer.toString(playerScore)).withStyle(ChatFormatting.YELLOW));
        int scoreX = width / 2 - mc.font.width(scoreText) / 2;
        GuiGraphicsExtractor.text(mc.font, scoreText, scoreX, 100, 0xFF5555 | (alpha << 24), false);

        // Render countdown

        // Render countdown - only if there are seconds left
        if (seconds > 0) {
            // Handle pulsing effect for last 3 seconds
            updatePulseEffect(seconds);

            Component countdown = Component.literal(String.valueOf(seconds))
                    .withStyle(ChatFormatting.BOLD);

            GuiGraphicsExtractor.pose().pushMatrix();
            // Apply pulsing scale
            float scale = 2.0f * pulseScale; // Base size is 3x
            GuiGraphicsExtractor.pose().scale(scale, scale);

            int scaledWidth = (int) (width / scale);
            int scaledHeight = (int) (height / scale)+40;

            int countdownX = (scaledWidth - mc.font.width(countdown)) / 2;
            int countdownY = (int) ((scaledHeight / 2 + Math.max(40,(4-seconds)*40)) / scale);

            GuiGraphicsExtractor.text(
                    mc.font,
                    countdown,
                    countdownX,
                    countdownY,
                    pulseColor | (alpha << 24),
                    false
            );

            GuiGraphicsExtractor.pose().popMatrix();
        }
        if(seconds<=3)
        {

            if(seconds<=1)
            {
                UpdateVignetteLastSecond();
            }

            else
                UpdateVignetteIntensity(0.8f + (4 - seconds) * 0.2f,seconds);
        }
        //play warden heartbeat sound
        if(seconds!=lastSecond)
        {
            LocalPlayer player = Minecraft.getInstance().player;
            if(seconds<=3)
            player.level().playLocalSound(player, SoundEvents.WARDEN_HEARTBEAT, EntitySoundSource.of(player), 1.0F, 1.0F);

            switch(seconds) {
                case 4:
                    player.level().playLocalSound(player, SoundEvents.PORTAL_TRIGGER, EntitySoundSource.of(player), 0.8F, 1.0F);
                    break;
                case 0:
                    player.level().playLocalSound(player, SoundEvents.PLAYER_BREATH, EntitySoundSource.of(player), 0.6F, 1.0F);
                    break;
                default:
                    break;
            }
            lastSecond=seconds;
        }
    }
    private static void updatePulseEffect(int seconds) {
        if (seconds <= 3) {

            // Pulse animation - scales between 0.9 and 1.1
            float pulseSpeed = 2.0f; // Speed of pulse
            pulseScale = (0.9f + 0.2f * (0.5f * (1 + Mth.sin((System.currentTimeMillis() % 2000) / 2000f * (float)Math.PI * 2 * pulseSpeed))))*(4-seconds);

            // Color transition from green to red
            float ratio = (4 - seconds) / 3.0f;
            int r = (int)(Mth.lerp(ratio, 0, 255));
            int g = (int)(Mth.lerp(ratio, 255, 0));
            pulseColor = (r << 16) | (g << 8);
            //vignetteIntensity = 0.8f + (3 - seconds) * 0.2f;
        } else {
            pulseScale = 1.0f;
            pulseColor = 0x00FF00; // Green
            vignetteIntensity=0.8f;
        }
    }


    private static void UpdateVignetteIntensity(float targetIntensity,int seconds)
    {
        if (vignetteIntensity>=targetIntensity)
        {
            if(seconds-1<=1)
                //For the screen transition at the last second
            IntensityAddition =10f;
            else
                IntensityAddition =0.02f;
            return;
        }
        vignetteIntensity+= IntensityAddition;

        if(IntensityAddition>=0)
        IntensityAddition -=0.001f;
        else
        IntensityAddition=0;
    }
    private static void UpdateVignetteLastSecond()
    {

        vignetteIntensity+= IntensityAddition;

        IntensityAddition -=0.4f;
    }

    //For vignette shader

    public static RenderPipeline ghostPipeline = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(MODID, "ghost_effect"))
            .withVertexShader(Identifier.fromNamespaceAndPath(MODID, "core/ghost_effect"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(MODID, "core/ghost_effect"))
            .withSampler("DiffuseSampler")  // Correct sampler declaration
            .withUniform("ScreenSize", UniformType.UNIFORM_BUFFER)
            .withDepthStencilState(new DepthStencilState(CompareOp.NEVER_PASS, false))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .build();
    private static float vignetteIntensity = 0.8f;
    private static GpuBuffer vertexBuffer;
    private static int vertexCount = 0;
    private static GpuSampler linearSampler;


    private static void createVertexBuffer() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }

        // Create normalized device coordinates (NDC) quad
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX
        );

        // Triangle 1 (bottom-left)
        bufferBuilder.addVertex(-1, -1, 0).setUv(0, 0);
        bufferBuilder.addVertex(-1, 1, 0).setUv(0, 1);
        bufferBuilder.addVertex(1, 1, 0).setUv(1, 1);

        // Triangle 2 (top-right)
        bufferBuilder.addVertex(-1, -1, 0).setUv(0, 0);
        bufferBuilder.addVertex(1, 1, 0).setUv(1, 1);
        bufferBuilder.addVertex(1, -1, 0).setUv(1, 0);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            vertexCount = meshData.drawState().indexCount();

            // Use GpuBuffer.Usage.VERTEX_BUFFER
            // meshData.vertexBuffer() provides the ByteBuffer containing the quad data
            vertexBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "GhostEffectVertices",
                    GpuBuffer.USAGE_VERTEX,
                    meshData.vertexBuffer()
            );

            meshData.close();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent.AfterWeather event) {
        if (!ClientPayloadHandler.getInstance().getGhostState()&& fade <= 0) return;

        Minecraft mc = Minecraft.getInstance();

        // Create vertex buffer once
        if (vertexBuffer == null) {
            createVertexBuffer();
        }
        if (linearSampler == null) {
            linearSampler = RenderSystem.getDevice().createSampler(
                    AddressMode.CLAMP_TO_EDGE,    // addressModeU
                    AddressMode.CLAMP_TO_EDGE,    // addressModeV
                    FilterMode.LINEAR,    // minFilter
                    FilterMode.LINEAR,    // magFilter
                    1,                    // maxAnisotropy (1 = off)
                    OptionalDouble.empty() // maxLod
            );
        }

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        RenderTarget renderTarget = mc.getMainRenderTarget();
        if (renderTarget.getColorTexture() == null) return;

        // Bind the framebuffer texture to texture unit 0
        //RenderSystem.setShaderTexture(0, renderTarget.getColorTexture());

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(()->"GhostDeathScreen",
                        renderTarget.getColorTextureView(),
                        OptionalInt.empty(),
                        renderTarget.getDepthTextureView(),
                        OptionalDouble.empty()
                )) {

            // Calculate time in seconds
            float time = (System.currentTimeMillis() % 100000) / 1000.0f;
            renderPass.setPipeline(ghostPipeline);

            GhostEffectData data = new GhostEffectData(new org.joml.Vector2f(width, height), vignetteIntensity, time, fade);

            // 2. Prepare the ByteBuffer (std140 alignment)
            // vec2 (8 bytes) + 3 floats (12 bytes) = 20 bytes total.
            // We allocate 32 bytes to ensure block alignment.
            ByteBuffer byteBuffer = org.lwjgl.system.MemoryUtil.memAlloc(32);
            byteBuffer.putFloat(data.screenSize().x());
            byteBuffer.putFloat(data.screenSize().y());
            byteBuffer.putFloat(data.vignetteIntensity());
            byteBuffer.putFloat(data.time());
            byteBuffer.putFloat(data.fadeFactor());
            byteBuffer.flip();

            // 3. Create the GPU Buffer using the signature from GpuDevice.txt
            try (GpuBuffer uboBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "GhostUBO",
                    GpuBuffer.USAGE_UNIFORM,
                    byteBuffer)) {

                renderPass.setUniform("GhostEffectUbo", uboBuffer.slice());

                // 4. Bind Sampler and Draw
                renderPass.bindTexture("DiffuseSampler", renderTarget.getColorTextureView(),linearSampler);
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.draw(0, vertexCount);
            } finally {
                // 5. Free the native memory allocated for the ByteBuffer
                org.lwjgl.system.MemoryUtil.memFree(byteBuffer);
            }

        }
    }

    public static void cleanup() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        vertexCount = 0;
    }
    public record GhostEffectData(Vector2fc screenSize, float vignetteIntensity, float time, float fadeFactor) {

        public static final Codec<GhostEffectData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ExtraCodecs.VECTOR2F.fieldOf("ScreenSize").forGetter(GhostEffectData::screenSize),
                        Codec.FLOAT.fieldOf("VignetteIntensity").forGetter(GhostEffectData::vignetteIntensity),
                        Codec.FLOAT.fieldOf("Time").forGetter(GhostEffectData::time),
                        Codec.FLOAT.fieldOf("FadeFactor").forGetter(GhostEffectData::fadeFactor)
                ).apply(instance, GhostEffectData::new)
        );
    }

}
