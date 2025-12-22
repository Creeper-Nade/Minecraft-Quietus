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

public class SkillTreeInfoScreen implements SkillTreeDraggable, SkillTreeScrollable {
    private static final ResourceLocation CONTENTS_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_contents");
    private static final ResourceLocation HEADER_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_header");

    protected static final int WIDTH = 200;
    protected static final int MAX_HEIGHT = SkillTreeScreen.WINDOW_HEIGHT;
    private static final int MAX_SCROLL_HEADING_LINES = 3;
    private static final int TEXT_LINE_SPACING = 2;
    private static final int CONTENTS_CONTAINER_PADDING = 3;
    private static final int SECTION_V_MARGIN = 5;
    private static final int HEADING_RESOURCE_V_MARGIN = 3; // vertically 3px left blank in resources, only the upper part 
    private static final int CONTENTS_RESOURCE_V_MARGIN = 3; // vertically 3px left blank in resources 
    private static final int H_MARGIN = 5;
    private static final int V_MARGIN = 4;
    private static final int HEADING_TEXT_MAX_WIDTH = WIDTH - H_MARGIN*2;
    private static final int DESCRIPTION_TEXT_MAX_WIDTH = WIDTH - CONTENTS_CONTAINER_PADDING*2 - H_MARGIN*2;
    private static final int SCROLL_BAR_WIDTH = 1;
    private static final int SCROLL_BAR_PADDING = 3;

    private final Font font;
    private final Component heading;
    private final Component description;
    private final SkillTreeWidget widget;
    private final SkillTreeScreen screen;

    private int height;

    private int headingLines;
    private int descriptionLines;
    private int totalHeadingParHeight;
    private int totalDescriptionParHeight;
    private int headingParHeight;
    private int descriptionParHeight;
    private boolean isScrollHeading;
    private boolean isScrollDescription;

    private boolean scrollingHeading = false;
    private boolean scrollingDescription = false;
    private double headingScrollY = 0.0d;
    private double descriptionScrollY = 0.0d;

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

        this.totalHeadingParHeight = this.headingLines * font.lineHeight + (headingLines-1) * TEXT_LINE_SPACING;
        this.totalDescriptionParHeight = this.descriptionLines * font.lineHeight + (descriptionLines-1) * TEXT_LINE_SPACING;

        this.isScrollHeading = (this.totalHeadingParHeight > MAX_SCROLL_HEADING_LINES * this.font.lineHeight + (MAX_SCROLL_HEADING_LINES-1) * TEXT_LINE_SPACING);
        if (this.isScrollHeading) {
            this.headingLines = font.split(heading, HEADING_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING).size();
            this.totalHeadingParHeight = this.headingLines * font.lineHeight + (this.headingLines-1) * TEXT_LINE_SPACING;
            this.headingParHeight = Math.min(this.totalHeadingParHeight, MAX_SCROLL_HEADING_LINES * this.font.lineHeight + (MAX_SCROLL_HEADING_LINES-1) * TEXT_LINE_SPACING);
        } else {
            this.headingParHeight = this.totalHeadingParHeight;
        }
        this.isScrollDescription = this.totalDescriptionParHeight > MAX_HEIGHT - headingParHeight - (V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2) - (V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + CONTENTS_CONTAINER_PADDING + SECTION_V_MARGIN);
        if (this.isScrollDescription) {
            this.descriptionLines = font.split(description, DESCRIPTION_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING).size();
            this.totalDescriptionParHeight = this.descriptionLines * font.lineHeight + (this.descriptionLines-1) * TEXT_LINE_SPACING;
            this.descriptionParHeight = Math.min(this.totalDescriptionParHeight, MAX_HEIGHT - headingParHeight - (V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2) - (V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + CONTENTS_CONTAINER_PADDING + SECTION_V_MARGIN));
        } else {
            this.descriptionParHeight = this.totalDescriptionParHeight;
        }
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
        /* // calculating actual heights
        boolean isScrollHeading = (this.headingParHeight > MAX_SCROLL_HEADING_LINES * this.font.lineHeight + (MAX_HEADING_LINES-1) * TEXT_LINE_SPACING);
        int actualHeadingParHeight = Math.min(this.headingParHeight, MAX_SCROLL_HEADING_LINES * this.font.lineHeight + (MAX_HEADING_LINES-1) * TEXT_LINE_SPACING);
        int actualDescriptionParHeight = Math.min(this.descriptionParHeight, MAX_HEIGHT - actualHeadingParHeight - (V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2) - (V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + CONTENTS_CONTAINER_PADDING + SECTION_V_MARGIN)); */
        
        // calculate left and top x and y
        int left_x = offsetX - H_MARGIN;
        int top_y = offsetY - this.headingParHeight - V_MARGIN*2 - HEADING_RESOURCE_V_MARGIN;
        int description_y = offsetY + SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - CONTENTS_RESOURCE_V_MARGIN - 2;
        int heading_inside_x = offsetX;
        int heading_inside_y = top_y + V_MARGIN + HEADING_RESOURCE_V_MARGIN;
        int description_inside_x = left_x + H_MARGIN + CONTENTS_CONTAINER_PADDING;
        int description_inside_y = description_y + V_MARGIN + CONTENTS_RESOURCE_V_MARGIN + CONTENTS_CONTAINER_PADDING;

        // description container
        guiGraphics.blitSprite(RenderType::guiTextured, CONTENTS_SPRITE_LOCATION, left_x, description_y, WIDTH, this.descriptionParHeight+V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2);
        // header container
        guiGraphics.blitSprite(RenderType::guiTextured, HEADER_SPRITE_LOCATION, left_x, top_y, WIDTH, this.headingParHeight + V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + CONTENTS_CONTAINER_PADDING + SECTION_V_MARGIN);
        // header
            //guiGraphics.fill(offsetX, heading_inside_y, offsetX + HEADING_TEXT_MAX_WIDTH, heading_inside_y + this.actualHeadingParHeight, 0xBBFF0000);
        if (this.isScrollHeading) {
            guiGraphics.enableScissor(heading_inside_x, heading_inside_y, heading_inside_x + HEADING_TEXT_MAX_WIDTH, heading_inside_y + this.headingParHeight);
            GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.heading, heading_inside_x, heading_inside_y + (int)Math.round(this.headingScrollY), HEADING_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
            guiGraphics.disableScissor();
            this.calcAndDrawHeadingScrollBar(guiGraphics, description_inside_x + DESCRIPTION_TEXT_MAX_WIDTH, heading_inside_y);
        } else {
            GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.heading, heading_inside_x, heading_inside_y, HEADING_TEXT_MAX_WIDTH, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
        }
    
        // description
            //guiGraphics.fill(offsetX, description_inside_y, offsetX + HEADING_TEXT_MAX_WIDTH, description_inside_y + this.actualDescriptionParHeight, 0xBB0000FF);
        if (this.isScrollDescription) {
            guiGraphics.enableScissor(description_inside_x, description_inside_y, description_inside_x + DESCRIPTION_TEXT_MAX_WIDTH, description_inside_y + this.descriptionParHeight);
            GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.description, description_inside_x, description_inside_y + (int)Math.round(this.descriptionScrollY), DESCRIPTION_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
            guiGraphics.disableScissor();
            this.calcAndDrawDescriptionScrollBar(guiGraphics, description_inside_x + DESCRIPTION_TEXT_MAX_WIDTH, description_inside_y);
        } else {
            GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.description, description_inside_x, description_inside_y, DESCRIPTION_TEXT_MAX_WIDTH, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
        }
        
        // icon 
        this.widget.drawAbsolute(guiGraphics, offsetX, offsetY);
    }
    private void calcAndDrawDescriptionScrollBar(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.vLine(offsetX, offsetY, offsetY + this.descriptionParHeight, 0xFF555555); // track bar

        int thumbHeight = Math.max(8, (int)((float)this.descriptionParHeight / this.totalDescriptionParHeight * this.descriptionParHeight));
        float scrollRatio = (float)this.descriptionScrollY / (this.totalDescriptionParHeight - this.descriptionParHeight);
        int thumb_offsetY = (int)(scrollRatio * (this.descriptionParHeight - thumbHeight));
        guiGraphics.vLine(offsetX, offsetY-thumb_offsetY, offsetY-thumb_offsetY+thumbHeight, 0xFFFAFAFA);
    }
    private void calcAndDrawHeadingScrollBar(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.vLine(offsetX, offsetY, offsetY + this.headingParHeight, 0xFF555555); // track bar

        int thumbHeight = Math.max(8, (int)((float)this.headingParHeight / this.totalHeadingParHeight * this.headingParHeight));
        float scrollRatio = (float)this.headingScrollY / (this.totalHeadingParHeight - this.headingParHeight);
        int thumb_offsetY = (int)(scrollRatio * (this.headingParHeight - thumbHeight));
        guiGraphics.vLine(offsetX, offsetY-thumb_offsetY, offsetY-thumb_offsetY+thumbHeight, 0xFFFAFAFA);
    }

    public boolean isMouseOverWindow(int offsetX, int offsetY, double mouseX, double mouseY) {
        this.scrollingHeading = this.isMouseOverHeading(offsetX, offsetY, mouseX, mouseY);
        this.scrollingDescription = this.isMouseOverDescription(offsetX, offsetY, mouseX, mouseY);
        return (
            mouseX > offsetX - V_MARGIN &&
            mouseY > offsetY - this.headingParHeight - V_MARGIN*2 - HEADING_RESOURCE_V_MARGIN &&
            mouseX < offsetX - V_MARGIN + WIDTH &&
            mouseY < offsetY + SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - 2 + V_MARGIN*2 + CONTENTS_CONTAINER_PADDING*2 + this.descriptionParHeight
        );
    }

    /**
     * Whether or not the mouse is hovering over the are of header text
     * Used for determining if the mouse should scroll heading
     * @return true if mouse is hovering over heading, false otherwise
     */
    public boolean isMouseOverHeading(int offsetX, int offsetY, double mouseX, double mouseY) {
        int actualHeadingWidth = this.isScrollHeading ? HEADING_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING : HEADING_TEXT_MAX_WIDTH;
        return (
            mouseX > offsetX - H_MARGIN &&
            mouseY > offsetY - V_MARGIN - this.headingParHeight &&
            mouseX < offsetX + actualHeadingWidth &&
            mouseY < offsetY - V_MARGIN
        );
    }
    /**
     * Whether or not the mouse is hovering over the are of description text
     * Used for determining if the mouse should scroll description
     * @return true if mouse is hovering over heading, false otherwise
     */
    public boolean isMouseOverDescription(int offsetX, int offsetY, double mouseX, double mouseY) {
        int actualDescriptionWidth = this.isScrollDescription ? DESCRIPTION_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING : DESCRIPTION_TEXT_MAX_WIDTH;
        return (
            mouseX > offsetX + CONTENTS_CONTAINER_PADDING &&
            mouseY > offsetY + SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - 2 + V_MARGIN + CONTENTS_CONTAINER_PADDING &&
            mouseX < offsetX + CONTENTS_CONTAINER_PADDING + actualDescriptionWidth &&
            mouseY < offsetY + SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - 2 + V_MARGIN + CONTENTS_CONTAINER_PADDING + this.descriptionParHeight
        );
    }

    @Override
    public void drag(double dragX, double dragY) {
        if (this.scrollingHeading) {
            this.headingScrollY += dragY;
            this.headingScrollY = Math.clamp(this.headingScrollY, -(this.totalHeadingParHeight-this.headingParHeight), 0);
        }
        if (this.scrollingDescription) {
            this.descriptionScrollY += dragY;
            this.descriptionScrollY = Math.clamp(this.descriptionScrollY, -(this.totalDescriptionParHeight-this.descriptionParHeight), 0);
        }
    }
    @Override
    public void scroll(double scrollX, double scollY) {
        if (this.scrollingHeading) {
            this.headingScrollY += scollY*5;
            this.headingScrollY = Math.clamp(this.headingScrollY, -(this.totalHeadingParHeight-this.headingParHeight), 0);
        }
        if (this.scrollingDescription) {
            this.descriptionScrollY += scollY*12;
            this.descriptionScrollY = Math.clamp(this.descriptionScrollY, -(this.totalDescriptionParHeight-this.descriptionParHeight), 0);
        }
    }
}
