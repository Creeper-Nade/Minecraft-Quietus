package com.minecraftquietus.quietus.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class GuiGraphicsUtil {
    
    public static void drawWordWrap(GuiGraphics guiGraphics, Font font, FormattedText text, int x, int y, int lineWidth, int lineSpacing, int color, boolean dropShadow) {
        for (FormattedCharSequence formattedcharsequence : font.split(text, lineWidth)) {
            guiGraphics.drawString(font, formattedcharsequence, x, y, color, dropShadow);
            y += font.lineHeight;
            y += lineSpacing;
        }
    }
}
