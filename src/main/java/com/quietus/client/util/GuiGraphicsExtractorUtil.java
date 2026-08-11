package com.quietus.client.util;

import java.util.List;

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
    public static void drawCenteredWordWrap(GuiGraphicsExtractor gui, Font font, FormattedText text, int centerX, int y, int maxWidth, int lineSpacing, int color) {
        for (FormattedCharSequence formattedcharsequence : font.split(text, maxWidth)) {
            gui.centeredText(font, formattedcharsequence, centerX, y, color);
            y += font.lineHeight;
            y += lineSpacing;
        }
    }
    public static int getWordWrapHeight(Font font, FormattedText text, int maxWidth, int lineSpacing) {
        List<FormattedCharSequence> lines = font.split(text, maxWidth);
        if (lines.isEmpty()) return 0;
        return lines.size() * font.lineHeight + (lines.size() - 1) * lineSpacing;
    }
    public static int getWordWrapWidth(Font font, FormattedText text, int maxWidth) {
        int maxLineWidth = 0;
        for (FormattedCharSequence line : font.split(text, maxWidth)) {
            int lineWidth = font.width(line);
            if (lineWidth > maxLineWidth) {
                maxLineWidth = lineWidth;
            }
        }
        return maxLineWidth;
    }
}
