package com.quietus.client.hud;

import com.quietus.client.handler.ClientPayloadHandler;
import com.quietus.item.QuietusItems;
import com.quietus.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import static com.quietus.Quietus.MODID;

/** Draws the inventory-only Void Orrery weather report. */
public final class VoidOrreryInventoryDisplay {
    private static final Identifier CONTAINER =
            Identifier.fromNamespaceAndPath(MODID, "void_orrery/container");
    private static final Identifier WAVE_CONTAINER =
            Identifier.fromNamespaceAndPath(MODID, "void_orrery/wave_container");
    private static final Identifier END_PORTAL_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/end_portal/end_portal.png");
    private static final Identifier END_SKY_TEXTURE =
            Identifier.withDefaultNamespace("textures/environment/end_sky.png");

    /*
     * The supplied artwork is 195x38 because it was traced over a scaled
     * inventory reference. Vanilla's inventory is 176 GUI pixels wide, so all
     * source measurements are converted by the same 176/195 scale here.
     */
    public static final int WIDTH = 176;
    public static final int HEIGHT = 34;
    private static final int WAVE_X = 5;
    private static final int WAVE_Y = 5;
    private static final int WAVE_WIDTH = 166;
    private static final int WAVE_HEIGHT = 16;
    private static final int TEXT_X = 6;
    private static final int TEXT_Y = 24;
    private static final int TEXT_WIDTH = 164;
    private static final int GAP = 18;
    private static final double TICKER_PIXELS_PER_SECOND = 18.0D;

    private VoidOrreryInventoryDisplay() {
    }

    public static void render(GuiGraphicsExtractor graphics, int inventoryLeft, int inventoryTop,
                              int inventoryWidth, int inventoryHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !player.getInventory().contains(stack -> stack.is(QuietusItems.VOID_ORRERY.get()))) {
            return;
        }

        int x = inventoryLeft;
        // Join directly to the inventory's lower edge. At the usual 240px GUI height
        // this uses the complete remaining strip without pushing the panel off-screen.
        int y = Math.min(inventoryTop + inventoryHeight - 1, graphics.guiHeight() - HEIGHT);

        renderPanel(graphics, x, y);
    }

    /** Renders the shared panel contents for both inventory and HUD presentations. */
    public static void renderPanel(GuiGraphicsExtractor graphics, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CONTAINER, x, y, WIDTH, HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WAVE_CONTAINER,
                x + WAVE_X, y + WAVE_Y, WAVE_WIDTH, WAVE_HEIGHT);

        ClientPayloadHandler disturbance = ClientPayloadHandler.getInstance();
        int signalX = x + WAVE_X + 2;
        int signalY = y + WAVE_Y + 2;
        int signalWidth = WAVE_WIDTH - 4;
        int signalHeight = WAVE_HEIGHT - 4;
        renderEndPortal(graphics, signalX, signalY, signalWidth, signalHeight);
        renderWave(graphics, signalX, signalY, signalWidth, signalHeight, disturbance.getDisturbance());
        renderTicker(graphics, minecraft.font, x + TEXT_X, y + TEXT_Y, disturbance);
    }

    private static void renderEndPortal(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        AbstractTexture endSky = minecraft.getTextureManager().getTexture(END_SKY_TEXTURE);
        AbstractTexture endPortal = minecraft.getTextureManager().getTexture(END_PORTAL_TEXTURE);
        TextureSetup textures = TextureSetup.doubleTexture(
                endSky.getTextureView(), endSky.getSampler(),
                endPortal.getTextureView(), endPortal.getSampler()
        );

        EndPortalGuiRenderState portalState = new EndPortalGuiRenderState(
                textures,
                new org.joml.Matrix3x2f(graphics.pose()),
                x,
                y,
                x + width,
                y + height
        );
        ((GuiGraphicsExtractorAccessor) graphics).quietus$getGuiRenderState().addGuiElement(portalState);
    }

    private static void renderWave(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                   double disturbance) {
        double strength = Mth.clamp(disturbance / 100.0D, 0.0D, 1.0D);
        double time = System.currentTimeMillis() / 150.0D;
        int centerY = y + height / 2;
        int previousY = centerY;
        int color = colorFor(disturbance);

        for (int pixel = 0; pixel < width; pixel++) {
            double carrier = Math.sin(pixel * (0.10D + strength * 0.18D) + time);
            double interference = Math.sin(pixel * 0.43D - time * 1.37D)
                    + Math.sin(pixel * 0.71D + time * 0.63D);
            double signal = carrier * (0.7D + strength * 2.2D) + interference * strength * 1.25D;
            int currentY = Mth.clamp(centerY + (int) Math.round(signal), y, y + height - 1);
            graphics.fill(x + pixel, Math.min(previousY, currentY), x + pixel + 1,
                    Math.max(previousY, currentY) + 1, color);
            previousY = currentY;
        }
    }

    private static void renderTicker(GuiGraphicsExtractor graphics, Font font, int x, int y,
                                     ClientPayloadHandler disturbance) {
        int rounded = Mth.clamp((int) Math.round(disturbance.getDisturbance()), 0, 100);
        String levelKey = levelKey(rounded);
        Component message = Component.translatable(
                "gui.quietus.void_orrery.report",
                rounded,
                Component.translatable("gui.quietus.void_orrery.level." + levelKey),
                Component.translatable("gui.quietus.void_orrery.message." + levelKey),
                disturbance.getDisturbanceStage(),
                String.format(java.util.Locale.ROOT, "%.1f", disturbance.getDisturbanceVolatility())
        );
        int messageWidth = font.width(message);

        graphics.enableScissor(x, y, x + TEXT_WIDTH, y + font.lineHeight);
        if (messageWidth <= TEXT_WIDTH) {
            graphics.text(font, message, x, y, 0xFFF2D1B0, false);
        } else {
            int cycle = messageWidth + GAP;
            int offset = (int) ((System.currentTimeMillis() * TICKER_PIXELS_PER_SECOND / 1000.0D) % cycle);
            graphics.text(font, message, x - offset, y, 0xFFF2D1B0, false);
            graphics.text(font, message, x - offset + cycle, y, 0xFFF2D1B0, false);
        }
        graphics.disableScissor();
    }

    private static String levelKey(int disturbance) {
        if (disturbance < 20) return "calm";
        if (disturbance < 40) return "watchful";
        if (disturbance < 60) return "elevated";
        if (disturbance < 80) return "severe";
        return "catastrophic";
    }

    private static int colorFor(double disturbance) {
        if (disturbance < 20.0D) return 0xFF8CE6C0;
        if (disturbance < 40.0D) return 0xFFCAE68C;
        if (disturbance < 60.0D) return 0xFFF2D26B;
        if (disturbance < 80.0D) return 0xFFF09A62;
        return 0xFFFF665E;
    }
}
