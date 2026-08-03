package com.quietus.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.quietus.item.tool.MagicChantingWeaponItem;
import com.quietus.magic.MagicChantingPattern;
import com.quietus.server.packet.MagicCastInputPacket;
import com.quietus.enchantment.QuietusEnchantmentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class MagicClickerHudOverlay {
    private static final Identifier BACKGROUND = texture("magic_clicker_bg.png");
    private static final Identifier CHECK = texture("magic_clicker_thick.png");
    private static final Identifier POINTER = texture("magic_clicker_pointer.png");
    private static final int TEXTURE_SIZE = 32;
    private static final float CIRCLE_CENTER_X = 15.0F;
    private static final float CIRCLE_CENTER_Y = 15.0F;
    private static final float POINTER_RADIUS = 10.0F;
    private static final int BACKGROUND_COLOR = 0xFFFFFFFF;
    private static final int LEFT_COLOR = 0xFF35DDEB;
    private static final int RIGHT_COLOR = 0xFFE68A32;
    private static final int SUCCESS_COLOR = 0xFFFFD83D;
    private static final int FAILURE_COLOR = 0xFF5A0C0C;
    private static final int POINTER_COLOR = 0xFFFFFFFF;
    // Animation durations are measured in game ticks (20 ticks = 1 second).
    private static final float HUD_FADE_TICKS = 2.0F;
    private static final float CHECKPOINT_SUCCESS_FAILURE_FADE_TICKS = 4.0F;
    private static final float CHECKPOINT_GROWTH = 0.75F;
    private static final float CHANT_SOUND_VOLUME = 0.5F;
    private static final float CHANT_SUCCESS_BASE_PITCH = 1.0F;
    private static final float CHANT_SUCCESS_MAX_PITCH = 1.2F;
    private static final float CHANT_PERFECT_PITCH = 1.0F;

    private static long startGameTime;
    private static int processedCheckpoints;
    private static int successfulCheckpoints;
    private static boolean missedSinceLastInput;
    private static boolean active;
    private static InteractionHand castingHand;
    private static MagicChantingPattern chantingPattern;
    private static MagicChantingPattern.Generated generatedPattern;
    private static ClientLevel castingLevel;
    private static float checkpointRadius;
    private static float hudStartTime;
    private static float fadeOutStartTime = -1.0F;
    private static float fadeOutBaseOpacity = 1.0F;
    private static final List<CheckpointAnimation> checkpointAnimations = new ArrayList<>();

    private MagicClickerHudOverlay() {
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/gui/sprites/magic_ui/" + name);
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || event.getAction() != InputConstants.PRESS) {
            return;
        }

        boolean leftClick = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
        boolean rightClick = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        if (active) {
            if (rightClick) {
                event.setCanceled(true);
                resolveInput(MagicCastInputPacket.RIGHT);
            } else if (leftClick) {
                event.setCanceled(true);
                resolveInput(MagicCastInputPacket.LEFT);
            }
            return;
        }

    }

    public static void handleStartResult(boolean accepted, long seed, int hand) {
        Minecraft minecraft = Minecraft.getInstance();
        castingHand = hand == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!accepted || minecraft.player == null
                || !(minecraft.player.getItemInHand(castingHand).getItem() instanceof MagicChantingWeaponItem magicWeapon)) {
            return;
        }
        chantingPattern = magicWeapon.getChantingPattern();
        checkpointRadius = QuietusEnchantmentHelper.getAttunedCheckpointRadius(
                minecraft.level, minecraft.player.getItemInHand(castingHand), chantingPattern.windowRadius());
        generatedPattern = chantingPattern.generate(seed);
        castingLevel = minecraft.level;
        active = true;
        processedCheckpoints = 0;
        successfulCheckpoints = 0;
        missedSinceLastInput = false;
        startGameTime = minecraft.level.getGameTime();
        hudStartTime = animationTime(0.0F);
        fadeOutStartTime = -1.0F;
        fadeOutBaseOpacity = 1.0F;
        checkpointAnimations.clear();
        minecraft.gameRenderer.itemInHandRenderer.itemUsed(castingHand);
    }

    private static void resolveInput(int action) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float progress = progress(partialTick);
        advanceMissedCheckpoints(progress);
        boolean missedCheckpoint = missedSinceLastInput;
        missedSinceLastInput = false;
        int index = processedCheckpoints;
        MagicChantingPattern.Checkpoint checkpoint = index < generatedPattern.size()
                ? generatedPattern.checkpoints().get(index) : null;
        boolean correctButton = checkpoint != null
                && ((action == MagicCastInputPacket.LEFT
                    && checkpoint.input() == MagicChantingPattern.Input.LEFT)
                || (action == MagicCastInputPacket.RIGHT
                    && checkpoint.input() == MagicChantingPattern.Input.RIGHT));
        boolean inWindow = checkpoint != null
                && Math.abs(progress - checkpoint.center()) <= checkpointRadius;

        ClientPacketDistributor.sendToServer(new MagicCastInputPacket(action, progress));
        if (inWindow && correctButton) {
            animateCheckpoint(index, true);
            boolean finalCheckpoint = index == generatedPattern.size() - 1;
            boolean perfectSequence = finalCheckpoint && successfulCheckpoints == index;
            if (perfectSequence) {
                playChantSound(SoundEvents.ENDER_EYE_DEATH, CHANT_PERFECT_PITCH);
                playChantSound(SoundEvents.EXPERIENCE_ORB_PICKUP, CHANT_PERFECT_PITCH);
            } else {
                playAccumulatedSuccessSound();
            }
            successfulCheckpoints++;
            processedCheckpoints++;
            Minecraft.getInstance().gameRenderer.itemInHandRenderer.itemUsed(castingHand);
            if (processedCheckpoints == generatedPattern.size()) {
                completeClientCast();
            }
        } else if (inWindow) {
            failClientCast(true);
        } else if (missedCheckpoint) {
            if (processedCheckpoints == generatedPattern.size()) {
                failClientCast(false);
            }
        } else {
            failClientCast(true);
        }
    }

    private static void advanceMissedCheckpoints(float progress) {
        while (generatedPattern != null && processedCheckpoints < generatedPattern.size()) {
            MagicChantingPattern.Checkpoint checkpoint = generatedPattern.checkpoints().get(processedCheckpoints);
            if (progress <= checkpoint.center() + checkpointRadius) {
                break;
            }
            animateCheckpoint(processedCheckpoints, false);
            processedCheckpoints++;
            missedSinceLastInput = true;
        }
    }

    private static float progress(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0.0F;
        }
        float elapsedTicks = minecraft.level.getGameTime() - startGameTime + partialTick;
        return elapsedTicks / chantingPattern.durationTicks();
    }

    private static void animateCheckpoint(int index, boolean successful) {
        if (generatedPattern == null || index < 0 || index >= generatedPattern.size()) {
            return;
        }
        checkpointAnimations.add(new CheckpointAnimation(
                generatedPattern.checkpoints().get(index).center(), successful, animationTime(0.0F)));
    }

    private static void completeClientCast() {
        endClientCast(false);
    }

    private static void failClientCast(boolean failUnreached) {
        playChantSound(SoundEvents.SAND_BREAK, 1.0F);
        endClientCast(failUnreached);
    }

    private static void endClientCast(boolean failUnreached) {
        if (failUnreached && generatedPattern != null) {
            while (processedCheckpoints < generatedPattern.size()) {
                animateCheckpoint(processedCheckpoints, false);
                processedCheckpoints++;
            }
        }
        active = false;
        if (fadeOutStartTime < 0.0F) {
            fadeOutStartTime = animationTime(0.0F);
            fadeOutBaseOpacity = clamp01((fadeOutStartTime - hudStartTime) / HUD_FADE_TICKS);
        }
    }

    private static void playAccumulatedSuccessSound() {
        if (generatedPattern == null) {
            return;
        }
        int pitchSteps = Math.max(1, generatedPattern.size() - 1);
        float accumulatedProgress = Math.min(successfulCheckpoints, pitchSteps) / (float) pitchSteps;
        float pitch = CHANT_SUCCESS_BASE_PITCH
                + (CHANT_SUCCESS_MAX_PITCH - CHANT_SUCCESS_BASE_PITCH) * accumulatedProgress;
        playChantSound(SoundEvents.END_PORTAL_FRAME_FILL, pitch);
    }

    private static void playChantSound(net.minecraft.sounds.SoundEvent sound, float pitch) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            minecraft.level.playLocalSound(minecraft.player, sound,
                    SoundSource.PLAYERS, CHANT_SOUND_VOLUME, pitch);
        }
    }

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        resetClientState();
    }

    @SubscribeEvent
    public static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        resetClientState();
    }

    private static void resetClientState() {
        active = false;
        processedCheckpoints = 0;
        successfulCheckpoints = 0;
        missedSinceLastInput = false;
        castingHand = null;
        chantingPattern = null;
        generatedPattern = null;
        castingLevel = null;
        checkpointRadius = 0.0F;
        startGameTime = 0L;
        hudStartTime = 0.0F;
        fadeOutStartTime = -1.0F;
        fadeOutBaseOpacity = 1.0F;
        checkpointAnimations.clear();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        if ((active || fadeOutStartTime >= 0.0F || !checkpointAnimations.isEmpty())
                && castingLevel != minecraft.level) {
            resetClientState();
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float animationTime = animationTime(partialTick);
        checkpointAnimations.removeIf(animation ->
                animationTime - animation.startTime() >= CHECKPOINT_SUCCESS_FAILURE_FADE_TICKS);
        if (!active && (fadeOutStartTime < 0.0F
                || (animationTime - fadeOutStartTime >= HUD_FADE_TICKS
                    && checkpointAnimations.isEmpty()))) {
            resetClientState();
            return;
        }

        if (active && (castingHand == null
                || !(minecraft.player.getItemInHand(castingHand).getItem() instanceof MagicChantingWeaponItem))) {
            ClientPacketDistributor.sendToServer(new MagicCastInputPacket(
                    MagicCastInputPacket.CANCEL, progress(0.0F)));
            failClientCast(false);
        }

        float progress = progress(partialTick);
        if (active) {
            advanceMissedCheckpoints(progress);
            if (progress >= 1.0F) {
                failClientCast(false);
            }
        }
        progress = Math.min(progress, 1.0F);

        float hudOpacity = active
                ? clamp01((animationTime - hudStartTime) / HUD_FADE_TICKS)
                : fadeOutBaseOpacity
                    * (1.0F - clamp01((animationTime - fadeOutStartTime) / HUD_FADE_TICKS));

        GuiGraphicsExtractor gui = event.getGuiGraphics();
        // Vanilla places its 15x15 crosshair at (dimension - 15) / 2. Its
        // center pixel is therefore one GUI pixel left/up of dimension / 2
        // when the scaled window dimension is even.
        int centerX = (minecraft.getWindow().getGuiScaledWidth() - 15) / 2 + 7;
        int centerY = (minecraft.getWindow().getGuiScaledHeight() - 15) / 2 + 7;

        // The supplied alignment reference defines (15, 15) in the 32x32
        // artwork as the spell-circle pivot. Align that design pivot with the
        // vanilla crosshair's actual center pixel.
        gui.pose().pushMatrix();
        gui.pose().translate(centerX - CIRCLE_CENTER_X, centerY - CIRCLE_CENTER_Y);

        // CROSSHAIR supplies the inverse-color blend used by the artwork. Its
        // RGB strength, rather than its alpha channel, controls how strongly
        // it inverts the scene, so fade the tint from black to white.
        gui.blit(RenderPipelines.CROSSHAIR, BACKGROUND, 0, 0,
                0, 0, TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE, withInverseStrength(hudOpacity));

        for (int i = processedCheckpoints; i < generatedPattern.size(); i++) {
            MagicChantingPattern.Checkpoint checkpoint = generatedPattern.checkpoints().get(i);
            int color = checkpoint.input() == MagicChantingPattern.Input.LEFT
                    ? LEFT_COLOR : RIGHT_COLOR;
            renderArcSection(gui, checkpoint.center(), withOpacity(color, hudOpacity));
        }

        for (CheckpointAnimation animation : checkpointAnimations) {
            float animationProgress = clamp01(
                    (animationTime - animation.startTime()) / CHECKPOINT_SUCCESS_FAILURE_FADE_TICKS);
            float scale = 1.0F + CHECKPOINT_GROWTH * animationProgress;
            float checkpointX = CIRCLE_CENTER_X
                    - POINTER_RADIUS * (float) Math.cos(animation.center() * Math.PI);
            float checkpointY = CIRCLE_CENTER_Y
                    - 7.5F * (float) Math.sin(animation.center() * Math.PI);
            int color = animation.successful() ? SUCCESS_COLOR : FAILURE_COLOR;

            gui.pose().pushMatrix();
            gui.pose().translate(checkpointX, checkpointY);
            gui.pose().scale(scale, scale);
            gui.pose().translate(-checkpointX, -checkpointY);
            renderArcSection(gui, animation.center(),
                    withOpacity(color, (active ? hudOpacity : fadeOutBaseOpacity)
                            * (1.0F - animationProgress)));
            gui.pose().popMatrix();
        }

        // Draw the pointer once at the left endpoint, then rotate the entire
        // pointer around the circle center. This rotates both its position and
        // orientation, while the matrix preserves smooth sub-pixel motion.
        gui.pose().pushMatrix();
        gui.pose().translate(CIRCLE_CENTER_X, CIRCLE_CENTER_Y);
        gui.pose().rotate(progress * (float) Math.PI);
        gui.pose().translate(-CIRCLE_CENTER_X, -CIRCLE_CENTER_Y);
        int pointerX = Math.round(CIRCLE_CENTER_X - POINTER_RADIUS - 8.0F);
        int pointerY = Math.round(CIRCLE_CENTER_Y - 7.5F);
        // The sprite's sharp end points down. Rotate its initial orientation
        // 90 degrees counter-clockwise so that end points inward from the left.
        gui.pose().translate(pointerX + 8.0F, pointerY + 7.5F);
        gui.pose().rotate(-(float) Math.PI / 2.0F);
        gui.pose().translate(-(pointerX + 8.0F), -(pointerY + 7.5F));
        gui.blit(RenderPipelines.GUI_TEXTURED, POINTER, pointerX, pointerY,
                0, 0, 16, 16, 16, 16, withOpacity(POINTER_COLOR, hudOpacity));
        gui.pose().popMatrix();
        gui.pose().popMatrix();
    }

    private static void renderArcSection(GuiGraphicsExtractor gui, float center, int color) {
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            int runStart = -1;
            for (int x = 0; x <= TEXTURE_SIZE; x++) {
                boolean inSection = false;
                if (x < TEXTURE_SIZE && y <= CIRCLE_CENTER_Y) {
                    double nx = (x - CIRCLE_CENTER_X) / 10.0;
                    double ny = (CIRCLE_CENTER_Y - y) / 7.5;
                    double p = 1.0 - Math.atan2(ny, nx) / Math.PI;
                    inSection = Math.abs(p - center) <= checkpointRadius;
                }
                if (inSection && runStart < 0) {
                    runStart = x;
                } else if (!inSection && runStart >= 0) {
                    int width = x - runStart;
                    gui.blit(RenderPipelines.GUI_TEXTURED, CHECK,
                            runStart, y,
                            runStart, y, width, 1,
                            width, 1, TEXTURE_SIZE, TEXTURE_SIZE, color);
                    runStart = -1;
                }
            }
        }
    }

    private static float animationTime(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0.0F : minecraft.level.getGameTime() + partialTick;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static int withOpacity(int color, float opacity) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * clamp01(opacity));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static int withInverseStrength(float strength) {
        int channel = Math.round(0xFF * clamp01(strength));
        return 0xFF000000 | (channel << 16) | (channel << 8) | channel;
    }

    private record CheckpointAnimation(float center, boolean successful, float startTime) {
    }
}
