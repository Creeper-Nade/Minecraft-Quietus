package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.Objects;

import com.minecraftquietus.quietus.client.util.GuiGraphicsUtil;
import com.minecraftquietus.quietus.skilltree.SkillPoint;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class SkillTreeInfoScreen {
    private static final ResourceLocation CONTENTS_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_contents");
    private static final ResourceLocation HEADER_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_header");

    protected static final int WIDTH = 200;
    protected static final int MAX_HEIGHT = SkillTreeScreen.WINDOW_HEIGHT;
    private static final int TEXT_LINE_SPACING = 2;
    private static final int CONTENTS_CONTAINER_PADDING = 3;
    private static final int SECTION_V_MARGIN = 5;
    private static final int SECTION_H_MARGIN = 6;
    private static final int HEADING_RESOURCE_V_MARGIN = 3; // vertically 3px left blank in resources, only the upper part 
    private static final int CONTENTS_RESOURCE_V_MARGIN = 3; // vertically 3px left blank in resources 
    private static final int H_MARGIN = 5;
    private static final int V_MARGIN = 5;
    private static final int HEADING_TEXT_MAX_WIDTH = WIDTH - H_MARGIN*2;
    private static final int DESCRIPTION_TEXT_MAX_WIDTH = WIDTH - CONTENTS_CONTAINER_PADDING*2 - H_MARGIN*2;

    private final Font font;
    private final Component heading;
    private final Component description;
    private final SkillTreeWidget widget;
    private final SkillTreeScreen screen;

    private int headingLines;
    private int descriptionLines;
    private int headingParHeight;
    private int descriptionParHeight;

    private SkillTreeInfoScreen(Font font, Component heading, Component description, SkillTreeWidget widget, SkillTreeScreen screen) {
        this.font = font;
        this.heading = heading;
        this.description = description;
        this.widget = widget;
        this.screen = screen;

        this.calcLinesHeights(font, heading, description);
    }

    protected static SkillTreeInfoScreen create(SkillTreeWidget widget, Font font, SkillTreeScreen screen) {
        Component heading = null;
        Component description = null;
        if (widget.getDisplay().isPresent()) {
            SkillPoint.DisplayInfo display = widget.getDisplay().get();
            heading = display.header();
            description = display.description();
        }
        heading = Objects.requireNonNullElse(heading, Component.translatable(String.join(".", "skillTree", widget.getLanguageKey(), "header")));
        description = Objects.requireNonNullElse(description, Component.translatable(String.join(".", "skillTree", widget.getLanguageKey(), "description")));

        SkillTreeInfoScreen out = new SkillTreeInfoScreen(font, heading, description, widget, screen);
        return out;
    }

    private void calcLinesHeights(Font font, Component heading, Component description) {
        this.headingLines = font.split(heading, HEADING_TEXT_MAX_WIDTH).size();
        this.descriptionLines = font.split(description, DESCRIPTION_TEXT_MAX_WIDTH).size();
        this.headingParHeight = this.headingLines * font.lineHeight + (headingLines-1) * TEXT_LINE_SPACING;
        this.descriptionParHeight = this.descriptionLines * font.lineHeight + (descriptionLines-1) * TEXT_LINE_SPACING;
    }

    /**
     * Draws the info screen
     * @param guiGraphics
     * @param mouseX
     * @param mouseY
     * @param offsetX x offset, exactly where the icon will be drawn
     * @param offsetY y offset, exactly where the icon will be drawn
     */
    public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        int left_x = offsetX - H_MARGIN;
        int top_y = offsetY - SECTION_V_MARGIN - this.headingParHeight - V_MARGIN - HEADING_RESOURCE_V_MARGIN;
        int description_y = offsetY + SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - CONTENTS_RESOURCE_V_MARGIN - 2;
        // description container
        guiGraphics.blitSprite(RenderType::guiTextured, CONTENTS_SPRITE_LOCATION, left_x, description_y, WIDTH, this.descriptionParHeight+V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2);
        // header container
        guiGraphics.blitSprite(RenderType::guiTextured, HEADER_SPRITE_LOCATION, left_x, top_y, WIDTH, this.headingParHeight + V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + CONTENTS_CONTAINER_PADDING + SECTION_V_MARGIN);
        // header
        GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.heading, offsetX, top_y + V_MARGIN + HEADING_RESOURCE_V_MARGIN, HEADING_TEXT_MAX_WIDTH, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
        // description
        GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.description, left_x + H_MARGIN + CONTENTS_CONTAINER_PADDING, description_y + V_MARGIN + CONTENTS_RESOURCE_V_MARGIN + CONTENTS_CONTAINER_PADDING, DESCRIPTION_TEXT_MAX_WIDTH, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
        
        // icon 
        this.widget.drawAbsolute(guiGraphics, offsetX, offsetY);
    }
}
