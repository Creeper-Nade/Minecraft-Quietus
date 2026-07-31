package com.quietus.client.hud;

import com.quietus.util.GrapplingHookCurios;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public final class GrapplingHookHudOverlay {
    private static final Identifier HOTBAR_OFFHAND_RIGHT_SPRITE =
            Identifier.withDefaultNamespace("hud/hotbar_offhand_right");

    private GrapplingHookHudOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.gameMode == null
                || minecraft.options.hideGui
                || minecraft.player.isSpectator()) {
            return;
        }

        GrapplingHookCurios.findEquippedHook(minecraft.player).ifPresent(result -> {
            ItemStack hook = result.stack();
            int screenCenter = graphics.guiWidth() / 2;
            int y = graphics.guiHeight() - 23;

            // If the player's offhand is already rendered on the right, put the hook
            // immediately after it instead of drawing both slots on top of each other.
            boolean rightOffhandVisible = !minecraft.player.getOffhandItem().isEmpty()
                    && minecraft.player.getMainArm().getOpposite() == HumanoidArm.RIGHT;
            int slotX = screenCenter + 91 + (rightOffhandVisible ? 24 : 0);

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    HOTBAR_OFFHAND_RIGHT_SPRITE,
                    slotX,
                    y,
                    29,
                    24
            );

            int itemX = slotX + 10;
            int itemY = graphics.guiHeight() - 19;
            float pop = hook.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
            if (pop > 0.0F) {
                float squeeze = 1.0F + pop / 5.0F;
                graphics.pose().pushMatrix();
                graphics.pose().translate(itemX + 8, itemY + 12);
                graphics.pose().scale(1.0F / squeeze, (squeeze + 1.0F) / 2.0F);
                graphics.pose().translate(-(itemX + 8), -(itemY + 12));
            }

            graphics.item(minecraft.player, hook, itemX, itemY, 10);
            if (pop > 0.0F) {
                graphics.pose().popMatrix();
            }
            graphics.itemDecorations(minecraft.font, hook, itemX, itemY);
        });
    }
}
