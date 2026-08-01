package com.quietus.client.hud;

import com.quietus.item.QuietusItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;

/** Top-of-screen Void Orrery display shown while the item is held. */
public final class VoidOrreryHudOverlay {
    private static final float SLIDE_IN_SECONDS = 0.35F;
    private static final float SLIDE_OUT_SECONDS = 0.20F;
    private static final int RESTING_Y = 4;

    private static float reveal;
    private static long previousFrameNanos;

    private VoidOrreryHudOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        long now = System.nanoTime();
        float elapsed = previousFrameNanos == 0L
                ? 0.0F
                : Math.min((now - previousFrameNanos) / 1_000_000_000.0F, 0.1F);
        previousFrameNanos = now;

        boolean held = player != null
                && (player.getMainHandItem().is(QuietusItems.VOID_ORRERY.get())
                || player.getOffhandItem().is(QuietusItems.VOID_ORRERY.get()));
        if (held) {
            reveal = Math.min(1.0F, reveal + elapsed / SLIDE_IN_SECONDS);
        } else {
            reveal = Math.max(0.0F, reveal - elapsed / SLIDE_OUT_SECONDS);
        }

        // InventoryScreenMixin renders the attached inventory version instead.
        if (reveal <= 0.0F || minecraft.options.hideGui || minecraft.screen instanceof InventoryScreen) {
            return;
        }

        float easedReveal = 1.0F - (float) Math.pow(1.0F - reveal, 3.0D);
        int x = (graphics.guiWidth() - VoidOrreryInventoryDisplay.WIDTH) / 2;
        int hiddenY = -VoidOrreryInventoryDisplay.HEIGHT;
        int y = Math.round(hiddenY + (RESTING_Y - hiddenY) * easedReveal);
        VoidOrreryInventoryDisplay.renderPanel(graphics, x, y);
    }
}
