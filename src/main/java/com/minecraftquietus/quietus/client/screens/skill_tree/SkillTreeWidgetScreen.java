package com.minecraftquietus.quietus.client.screens.skill_tree;

import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTreeListener;
import com.minecraftquietus.quietus.util.ServerPacketDistributor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

import static com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler.getSkillTree;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.List;

public class SkillTreeWidgetScreen implements SkillTreeDraggable, ClientSkillTreeListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation CONTAINER_CONTENTS_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_contents");
    private static final ResourceLocation CONTAINER_HEADER_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/container_header");
    private static final ResourceLocation UPGRADE_BUTTON_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/upgrade_button");
    private static final ResourceLocation CLOSE_BUTTON_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/cross_button");
    private static final ResourceLocation CLOSE_BUTTON_HIGHLIGHTED_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/cross_button_highlighted");
    private static final int CLOSE_BUTTON_WIDTH = 20;
    private static final int CLOSE_BUTTON_HEIGHT = 20;
    private static final int UPGRADE_BUTTON_WIDTH = 134;
    private static final int UPGRADE_BUTTON_HEIGHT = 18;

    /* In pixels (GUI) */
    private static final int WIDTH = 180;
    private static final int LINE_SPACING = 2;
    private static final int HEADER_CONTENTS_PADDING = 5;
    private static final int SECTION_V_MARGIN = 5;
    private static final int SECTION_H_MARGIN = 6;
    private static final int RESOURCE_V_MARGIN = 3; // vertically 3px left blank in resources 
    private static final int H_MARGIN = 5;
    private static final int CONTENT_H_MARGIN = H_MARGIN;
    private static final int V_MARGIN = 6;
    private static final int CONTENT_V_MARGIN = V_MARGIN;

    private final Minecraft minecraft;
    private final ClientSkillTree skillTree;
    private final Font font;
    private final SkillTreeWidget widget;
    private final SkillTreeScreen screen;
    private final Component header;
    private final Component description;

    private final int height;
    private final int headerTextHeight;

    private int x;
    private int y;
    private double scrollX;
    private double scrollY;

    private int amount;
    private int maxAmount;
    private int progressAmount;


    public SkillTreeWidgetScreen(Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeWidget widget, SkillTreeScreen screen, int widgetDrawnX, int widgetDrawnY) {
        this.minecraft = minecraft;
        this.font = this.minecraft.font;
        this.skillTree = clientSkillTree;
        this.widget = widget;
        this.screen = screen;
        this.x = widgetDrawnX;
        this.y = widgetDrawnY;
        this.scrollX = (double)this.x;
        this.scrollY = (double)this.y;
        this.header = Component.translatable(String.join(".", "skillTree", this.widget.getLanguageKey(), "header"));
        this.description = Component.translatable(String.join(".", "skillTree", this.widget.getLanguageKey(), "description"));

        this.headerTextHeight = this.font.split(this.header, WIDTH).size() * this.font.lineHeight + (this.font.split(this.header, WIDTH).size()-1) * LINE_SPACING;
        this.height = this.headerTextHeight
            + this.font.split(this.description, WIDTH).size() * this.font.lineHeight
            + (this.font.split(this.description, WIDTH).size()-1) * LINE_SPACING
            + SkillTreeWidget.ICON_HEIGHT 
            + SECTION_V_MARGIN
            + V_MARGIN * 2
            + CONTENT_V_MARGIN
            + HEADER_CONTENTS_PADDING * 2
            + RESOURCE_V_MARGIN * 2;

        /* Add listener. {@link ClientSkillTree#addListener} will do listener update, initiating this's parameters */
        this.skillTree.addListener(this.widget.getNode(), this);
    }

    public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        int render_x = this.x + offsetX;
        int render_y = this.y + offsetY;
        int window_x = render_x - H_MARGIN;
        int window_y = render_y - SECTION_V_MARGIN - this.headerTextHeight - V_MARGIN - RESOURCE_V_MARGIN;
        // contents container
        guiGraphics.blitSprite(RenderType::guiTextured, CONTAINER_CONTENTS_SPRITE_LOCATION, window_x, window_y, WIDTH, this.height);
        // header container
        guiGraphics.blitSprite(RenderType::guiTextured, CONTAINER_HEADER_SPRITE_LOCATION, window_x, window_y, WIDTH, this.headerTextHeight + V_MARGIN + SECTION_V_MARGIN + SkillTreeWidget.ICON_HEIGHT + HEADER_CONTENTS_PADDING);
        // header
        guiGraphics.drawWordWrap(font, header, render_x, render_y - SECTION_V_MARGIN - this.headerTextHeight, WIDTH, 0xFFFFFFFF, true);
        // description
        guiGraphics.drawWordWrap(font, description, render_x + CONTENT_H_MARGIN, render_y + SkillTreeWidget.ICON_HEIGHT + HEADER_CONTENTS_PADDING*2, WIDTH, 0xFFFFFFFF, true);
        // unlock button
        if (this.isMaxed()) {
            
        } else {
            guiGraphics.blitSprite(RenderType::guiTextured, UPGRADE_BUTTON_SPRITE_LOCATION, render_x + SECTION_H_MARGIN + SkillTreeWidget.ICON_WIDTH, render_y+4, UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_HEIGHT);
            //guiGraphics.fill(render_x + SECTION_H_MARGIN + SkillTreeWidget.ICON_WIDTH, render_y+4, render_x + SECTION_H_MARGIN + SkillTreeWidget.ICON_WIDTH + 134, render_y+18+4, 0xFF00BB20);
            if (this.amount == 0) 
                guiGraphics.drawCenteredString(font, Component.translatable("skillTree.quietus.unlock", this.amount, this.maxAmount), render_x + SECTION_H_MARGIN + SkillTreeWidget.ICON_WIDTH + 67, render_y+4+4, 0xFFFFFFFF);
            if (this.amount > 0) 
                guiGraphics.drawCenteredString(font, Component.translatable("skillTree.quietus.upgrade", this.amount, this.maxAmount), render_x + SECTION_H_MARGIN + SkillTreeWidget.ICON_WIDTH + 67, render_y+4+4, 0xFFFFFFFF);
        }
        // icon (drawn again in addition to the widget drawing itself in the tab)
        this.widget.drawAbsolute(guiGraphics, render_x, render_y);
        // close button
        if (this.isMouseOverCloseButton(offsetX, offsetY, mouseX, mouseY)) 
            guiGraphics.blitSprite(RenderType::guiTextured, CLOSE_BUTTON_HIGHLIGHTED_SPRITE_LOCATION, window_x + WIDTH - H_MARGIN - CLOSE_BUTTON_WIDTH, window_y + V_MARGIN, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);  
        else 
            guiGraphics.blitSprite(RenderType::guiTextured, CLOSE_BUTTON_SPRITE_LOCATION, window_x + WIDTH - H_MARGIN - CLOSE_BUTTON_WIDTH, window_y + V_MARGIN, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);
    }

    public boolean click(int offsetX, int offsetY, double mouseX, double mouseY, int mouseButton) {
        if (isMouseOverCloseButton(offsetX, offsetY, (int)mouseX, (int)mouseY)) {
            this.discard();
            return true;
        };
        if (isMouseOverUpgradeButton(offsetX, offsetY, (int)mouseX, (int)mouseY)) {
            ServerPacketDistributor.requestSkillTreeUpgrade(this.widget.getNode());
            return true;
        };
        return false;
    }

    private boolean isMouseOverUpgradeButton(int offsetX, int offsetY, int mouseX, int mouseY) {
        int buttonX = this.x + offsetX + SECTION_H_MARGIN + SkillTreeWidget.ICON_WIDTH;
        int buttonY = this.y + offsetY + 4;

        return 
            mouseX > buttonX 
            && mouseX < buttonX + 134
            && mouseY > buttonY
            && mouseY < buttonY + 18;
    }

    private boolean isMouseOverCloseButton(int offsetX, int offsetY, int mouseX, int mouseY) {
        int button_x = this.x + offsetX - H_MARGIN * 2 + WIDTH - CLOSE_BUTTON_WIDTH;
        int button_y = this.y + offsetY - SECTION_V_MARGIN - this.headerTextHeight - RESOURCE_V_MARGIN;
        return 
            mouseX > button_x 
            && mouseX < button_x + CLOSE_BUTTON_WIDTH
            && mouseY > button_y
            && mouseY < button_y + CLOSE_BUTTON_HEIGHT;
    }

    public boolean isMouseOverWindow(int offsetX, int offsetY, double mouseX, double mouseY) {
        int actual_x = this.x + offsetX - H_MARGIN;
        int actual_y = this.y + offsetY - SECTION_V_MARGIN - this.headerTextHeight - V_MARGIN - RESOURCE_V_MARGIN;
        /* System.out.println("xy: " + actual_x + "," + actual_y + " mouse: " + mouseX + "," + mouseY);
        if (mouseX > actual_x 
            && mouseX < actual_x + WIDTH
            && mouseY > actual_y
            && mouseY < actual_y + this.height)
        System.out.println("yes!"); */
        return 
            mouseX > actual_x 
            && mouseX < actual_x + WIDTH
            && mouseY > actual_y
            && mouseY < actual_y + this.height;
    }

    @Override
    public void drag(double dragX, double dragY) {
        this.scrollX += dragX;
        this.scrollY += dragY;

        this.x = (int)Math.round(this.scrollX);
        this.y = (int)Math.round(this.scrollY);
    }

    public void discard() {
        this.skillTree.removeListener(this.widget.getNode());
        this.screen.removeWidgetScreen(this.widget);
    }

    public void onClientSkillTreeUpdate(int amount, int maxAmount, int progressAmount) {
        this.amount = amount;
        this.maxAmount = maxAmount;
        this.progressAmount = progressAmount;
    }

    private boolean isMaxed() {
        if (this.amount > this.maxAmount) {
            this.amount = this.maxAmount;
            return true;
        } else {
            return this.amount == this.maxAmount;
        }
    }
}
