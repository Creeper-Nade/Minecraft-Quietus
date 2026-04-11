package com.minecraftquietus.quietus.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class GuiGraphicsExtractorUtil {
    
    public static void drawWordWrap(GuiGraphicsExtractor GuiGraphicsExtractor, Font font, FormattedText text, int x, int y, int lineWidth, int lineSpacing, int color, boolean dropShadow) {
        for (FormattedCharSequence formattedcharsequence : font.split(text, lineWidth)) {
            GuiGraphicsExtractor.text(font, formattedcharsequence, x, y, color, dropShadow);
            y += font.lineHeight;
            y += lineSpacing;
        }
    }
}
