package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.client.util.GuiGraphicsUtil;
import com.minecraftquietus.quietus.skilltree.Prerequisites;
import com.minecraftquietus.quietus.skilltree.SkillPoint;
import com.minecraftquietus.quietus.skilltree.SkillPointProgress;
import com.minecraftquietus.quietus.skilltree.Prerequisites.AndCondition;
import com.minecraftquietus.quietus.skilltree.Prerequisites.RequirementCondition;
import com.minecraftquietus.quietus.skilltree.Prerequisites.Requirements;
import com.minecraftquietus.quietus.util.ServerPacketDistributor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class SkillTreeInfoScreen implements SkillTreeDraggable, SkillTreeScrollable {
    private static final ResourceLocation CONTENTS_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_contents");
    private static final ResourceLocation HEADER_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_header");
    private static final ChatFormatting[] PREREQUISITES_STYLE = {ChatFormatting.GRAY};
    public static final ChatFormatting[] PREREQUISITES_CHECK_STYLE = {ChatFormatting.GREEN};
    public static final ChatFormatting[] PREREQUISITES_CROSS_STYLE = {ChatFormatting.RED};
    private static final String KEY_UPGRADEBUTTON_UNLOCK = "gui.skill_tree.upgrade_button.unlock";
    private static final String KEY_UPGRADEBUTTON_UPGRADE = "gui.skill_tree.upgrade_button.upgrade";
    private static final String KEY_UPGRADEBUTTON_LOCKED = "gui.skill_tree.upgrade_button.locked";
    private static final String KEY_UPGRADEBUTTON_LOCKEDUPGRADE = "gui.skill_tree.upgrade_button.locked_upgrade";

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
    private int topHeight;
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

    private UpgradeButtonState upgradeState = UpgradeButtonState.LOCKED;

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
        heading = Objects.requireNonNullElse(heading, SkillPoint.DisplayInfo.FUNC_DEFAULT_HEADING.apply(widget.getLanguageKey())); // default uses language key
        description = Objects.requireNonNullElse(description, SkillPoint.DisplayInfo.FUNC_DEFAULT_DESCRIPTION.apply(widget.getLanguageKey())); // default uses language key

        Component prerequisitesDescription = makePrerequisitesDescription(widget.getNode().getSkillPoint(), screen.getSkillTree());
        if (prerequisitesDescription != null) {
            description = MutableComponent.create(description.getContents()).append(Component.literal("\n\n")).append(prerequisitesDescription);
        }

        SkillTreeInfoScreen out = new SkillTreeInfoScreen(font, heading, description, widget, screen);
        return out;
    }

    private static Component makePrerequisitesDescription(SkillPoint skillPoint, ClientSkillTree tree) {
        Prerequisites nodePrerequisites = skillPoint.unlock().prerequisites();
        if (!nodePrerequisites.requirements().isEmpty()) {
            Set<ResourceLocation> completedAdvancements = tree.getCompletedAdvancements();
            Set<ResourceLocation> completedParents = tree.getCompletedParents();
            
            Prerequisites.CompletionStatus completionStatus = Prerequisites.CompletionStatus.make(skillPoint.unlock().prerequisites(), completedAdvancements, completedParents);

            MutableComponent out = MutableComponent.create(new TranslatableContents(Prerequisites.Requirements.KEY_DESCRIPTION_TEXT_NET, (String)null, TranslatableContents.NO_ARGS)).withStyle(PREREQUISITES_STYLE);
            Prerequisites.RequirementCondition cond = nodePrerequisites.requirements().makeNestedNode();
            if (cond instanceof Prerequisites.AndCondition and) {
                for (Prerequisites.RequirementCondition child : and.children()) {
                    out.append(Component.literal("\n")).append(child.makeDescriptionText(1, nodePrerequisites, skillPoint.display().map(SkillPoint.DisplayInfo::prerequisites), tree, completionStatus, PREREQUISITES_STYLE));
                }
            } else {
                Component prereqDescriptionText = cond.makeDescriptionText(1, nodePrerequisites, skillPoint.display().map(SkillPoint.DisplayInfo::prerequisites), tree, completionStatus, PREREQUISITES_STYLE);
                out.append(Component.literal("\n")).append(prereqDescriptionText);
            }
            return out;
        }
        return null;
    }

    public static MutableComponent statusSymbol(boolean done) {
        return done 
            ? Component.literal("✔").withStyle(PREREQUISITES_CHECK_STYLE) 
            : Component.literal("✘").withStyle(PREREQUISITES_CROSS_STYLE);
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
        this.topHeight = this.headingParHeight + V_MARGIN*2 + HEADING_RESOURCE_V_MARGIN;
        this.height = this.topHeight + (SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - CONTENTS_RESOURCE_V_MARGIN - 2) + (this.descriptionParHeight+V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2);
    }

    /**
     * Draws the info screen
     * @param guiGraphics
     * @param mouseX
     * @param mouseY
     * @param offsetX x offset, exactly where the icon will be drawn
     * @param offsetY y offset, exactly where the icon will be drawn
     */
    public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY, ClientSkillTree tree) {
        
        // calculate left and top x and y
        final int left_x = offsetX - H_MARGIN;
        final int top_y = offsetY - this.headingParHeight - V_MARGIN*2 - HEADING_RESOURCE_V_MARGIN;
        final int description_y = offsetY + SkillTreeWidget.ICON_HEIGHT + SECTION_V_MARGIN - CONTENTS_RESOURCE_V_MARGIN - 2;
        final int heading_inside_x = offsetX;
        final int upgradebutton_x = offsetX + SkillTreeWidget.ICON_WIDTH + 15;
        final int upgradebutton_y = offsetY + (SkillTreeWidget.ICON_HEIGHT - UpgradeButtonState.HEIGHT) / 2;
        final int heading_inside_y = top_y + V_MARGIN + HEADING_RESOURCE_V_MARGIN;
        final int description_inside_x = left_x + H_MARGIN + CONTENTS_CONTAINER_PADDING;
        final int description_inside_y = description_y + V_MARGIN + CONTENTS_RESOURCE_V_MARGIN + CONTENTS_CONTAINER_PADDING;

        // description container
        guiGraphics.blitSprite(RenderType::guiTextured, CONTENTS_SPRITE_LOCATION, left_x, description_y, WIDTH, this.descriptionParHeight+V_MARGIN*2+CONTENTS_CONTAINER_PADDING*2+CONTENTS_RESOURCE_V_MARGIN*2);
        // header container
        guiGraphics.blitSprite(RenderType::guiTextured, HEADER_SPRITE_LOCATION, left_x, top_y, WIDTH, this.headingParHeight + V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + CONTENTS_CONTAINER_PADDING + SECTION_V_MARGIN);
        // header
        if (this.isScrollHeading) {
            guiGraphics.enableScissor(heading_inside_x, heading_inside_y, heading_inside_x + HEADING_TEXT_MAX_WIDTH, heading_inside_y + this.headingParHeight);
            GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.heading, heading_inside_x, heading_inside_y + (int)Math.round(this.headingScrollY), HEADING_TEXT_MAX_WIDTH - SCROLL_BAR_PADDING, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
            guiGraphics.disableScissor();
            this.calcAndDrawHeadingScrollBar(guiGraphics, description_inside_x + DESCRIPTION_TEXT_MAX_WIDTH, heading_inside_y);
        } else {
            GuiGraphicsUtil.drawWordWrap(guiGraphics, this.font, this.heading, heading_inside_x, heading_inside_y, HEADING_TEXT_MAX_WIDTH, TEXT_LINE_SPACING, 0xFFFFFFFF, true);
        }
    
        // description
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

        // upgrade button
        Prerequisites widgetPrerequisite = this.widget.getNode().getSkillPoint().unlock().prerequisites();
        SkillPointProgress.ClientData progress = tree.getOrStartProgress(this.widget.getNode());
        this.upgradeState = UpgradeButtonState.get(
            progress.times() > 0, 
            widgetPrerequisite.requirements().test(Prerequisites.CompletionStatus.make(widgetPrerequisite, tree.getCompletedAdvancements(), tree.getCompletedParents()))
        );
        this.upgradeState.draw(
            guiGraphics, 
            upgradebutton_x, 
            upgradebutton_y, 
            this.isMouseOverUpgradeButton(offsetX, offsetY, mouseX, mouseY) && !progress.isMaxed(), 
            progress.times(), 
            progress.maxAmount(), 
            this.font
        );

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

    public boolean mouseClicked(int offsetX, int offsetY, double mouseX, double mouseY, int button) {
        if (this.isMouseOverUpgradeButton(offsetX, offsetY, mouseX, mouseY)) {
            if ((this.upgradeState == UpgradeButtonState.UNLOCK || this.upgradeState == UpgradeButtonState.UPGRADE)) {
                ServerPacketDistributor.requestSkillTreeUpgrade(this.widget.getNode());
            }
            return true;
        }
        return false;
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
     * Whether or not the mouse is hovering over the area of header text
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
     * Whether or not the mouse is hovering over the area of description text
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
    /**
     * Whether or not the mouse is hovering over the area of upgrade button
     * Used for determining if the hover effect on the button should appear
     * or if the click is on the upgrade button
     * @return true if mouse is hovering over heading, false otherwise
     */
    public boolean isMouseOverUpgradeButton(int offsetX, int offsetY, double mouseX, double mouseY) {
        return (
            mouseX > offsetX + SkillTreeWidget.ICON_WIDTH + 15 &&
            mouseY > offsetY + (SkillTreeWidget.ICON_HEIGHT - UpgradeButtonState.HEIGHT) / 2 &&
            mouseX < offsetX + SkillTreeWidget.ICON_WIDTH + 15 + UpgradeButtonState.WIDTH &&
            mouseY < offsetY + (SkillTreeWidget.ICON_HEIGHT - UpgradeButtonState.HEIGHT) / 2 + UpgradeButtonState.HEIGHT
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


    private enum UpgradeButtonState {
        UNLOCK(
            ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/upgrade_button/unlock"), 
            KEY_UPGRADEBUTTON_UNLOCK,
            true, 
            false
        ),
        UPGRADE(
            ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/upgrade_button/upgrade"), 
            KEY_UPGRADEBUTTON_UPGRADE,
            true, 
            true
        ),
        LOCKED(
            ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/upgrade_button/locked"), 
            KEY_UPGRADEBUTTON_UNLOCK,
            false, 
            false
        ),
        LOCKED_UPGRADE(
            ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/upgrade_button/locked_upgrade"), 
            KEY_UPGRADEBUTTON_LOCKEDUPGRADE,
            false, 
            true
        );

        private static final ResourceLocation FILL_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/upgrade_button_fill.png");
        protected static final int WIDTH = 134;
        protected static final int HEIGHT = 18;
        private static final int INSIDE_X = 4;
        private static final int INSIDE_Y = 4;

        private final ResourceLocation spriteLocation;
        private final String text;
        private final boolean hasHover;
        private final boolean doDrawLines;

        UpgradeButtonState(ResourceLocation sprite, String text, boolean hasHover, boolean doDrawLines) {
            this.spriteLocation = sprite;
            this.text = text;
            this.hasHover = hasHover;
            this.doDrawLines = doDrawLines;
        }

        private static UpgradeButtonState get(boolean hasProgress, boolean isUnlocked) {
            if (isUnlocked) {
                return hasProgress ? UPGRADE : UNLOCK;
            } else {
                return hasProgress ? LOCKED_UPGRADE : LOCKED;
            }
        }

        private void draw(GuiGraphics guiGraphics, int offsetX, int offsetY, boolean isHovered, int currentProgress, int maxProgress, Font font) {
            ResourceLocation loc = (isHovered && this.hasHover) ? this.spriteLocation.withPath(this.spriteLocation.getPath() + "_hovered") : this.spriteLocation;
            if (this.doDrawLines && currentProgress > 0) {
                int innerWidth = WIDTH - 2 * INSIDE_X;
                int innerHeight = HEIGHT - 2 * INSIDE_Y;

                // Draw progress fill
                int fillWidth = (int) Math.round((double) innerWidth * currentProgress / maxProgress);
                guiGraphics.blit(RenderType::guiTextured, FILL_LOCATION, offsetX + INSIDE_X, offsetY + INSIDE_Y, 0.0f, 0.0f, fillWidth, innerHeight, fillWidth, innerHeight);

                // Draw vertical dividers
                for (int i = 1; i < maxProgress; i++) {
                    int lineX = offsetX + INSIDE_X + (int) Math.round((double) i * innerWidth / maxProgress);
                    guiGraphics.vLine(lineX, offsetY + INSIDE_Y, offsetY + HEIGHT - INSIDE_Y - 1, 0xFF000000);
                }
            }
            guiGraphics.blitSprite(RenderType::guiTextured, loc, offsetX, offsetY, UpgradeButtonState.WIDTH, UpgradeButtonState.HEIGHT);
            guiGraphics.drawCenteredString(font, Component.translatable(this.text, currentProgress, maxProgress), offsetX + WIDTH/2, offsetY + HEIGHT/2 - font.lineHeight/2, 0xFFFFFFFF);
        }
    }

    public int getHeight() {
        return this.height;
    }
    public int getTopHeight() {
        return this.topHeight;
    }
}
