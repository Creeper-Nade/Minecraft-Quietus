package com.quietus.client.tooltip;

import com.mojang.blaze3d.platform.InputConstants;
import com.quietus.item.tooltip.MagicWeaponControlsTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.quietus.Quietus.MODID;

public final class ClientMagicWeaponControlsTooltip implements ClientTooltipComponent {
    private static final Identifier LEFT_MOUSE = texture("left_mouse_chant.png");
    private static final Identifier RIGHT_MOUSE = texture("right_mouse_chant.png");
    private static final Component HINT = Component.translatable("tooltip.quietus.magic_weapon.controls.hint");
    private static final Component TITLE = Component.translatable("tooltip.quietus.magic_weapon.controls.title");
    private static final Component START = Component.translatable("tooltip.quietus.magic_weapon.controls.start");
    private static final Component LEFT_CHECKPOINT = Component.translatable("tooltip.quietus.magic_weapon.controls.left_checkpoint");
    private static final Component RIGHT_CHECKPOINT = Component.translatable("tooltip.quietus.magic_weapon.controls.right_checkpoint");
    private static final int ICON_SIZE = 16;
    private static final int TEXT_OFFSET_X = 20;
    private static final int ROW_HEIGHT = 18;
    private static final int EXPANDED_HEIGHT = 11 + ROW_HEIGHT * 3;

    public ClientMagicWeaponControlsTooltip(MagicWeaponControlsTooltip ignored) {
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/gui/sprites/extra/" + name);
    }

    @Override
    public int getHeight(Font font) {
        return hasShiftDown() ? EXPANDED_HEIGHT : font.lineHeight;
    }

    @Override
    public int getWidth(Font font) {
        if (!hasShiftDown()) {
            return font.width(HINT);
        }
        return Math.max(
                font.width(TITLE),
                TEXT_OFFSET_X + Math.max(font.width(START),
                        Math.max(font.width(LEFT_CHECKPOINT), font.width(RIGHT_CHECKPOINT)))
        );
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        if (!hasShiftDown()) {
            graphics.text(font, HINT, x, y, 0xFFA0A0A0, true);
        }
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        if (!hasShiftDown()) {
            return;
        }

        graphics.text(font, TITLE, x, y, 0xFFFFAA00, true);
        drawRow(graphics, font, RIGHT_MOUSE, START, x, y + 11);
        drawRow(graphics, font, LEFT_MOUSE, LEFT_CHECKPOINT, x, y + 11 + ROW_HEIGHT);
        drawRow(graphics, font, RIGHT_MOUSE, RIGHT_CHECKPOINT, x, y + 11 + ROW_HEIGHT * 2);
    }

    private static void drawRow(GuiGraphicsExtractor graphics, Font font, Identifier icon,
                                Component label, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y,
                0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.text(font, label, x + TEXT_OFFSET_X, y + 4, 0xFFFFFFFF, true);
    }

    private static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_RSHIFT);
    }
}
