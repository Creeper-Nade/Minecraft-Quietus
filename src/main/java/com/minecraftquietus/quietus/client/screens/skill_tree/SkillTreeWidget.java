package com.minecraftquietus.quietus.client.screens.skill_tree;

import com.minecraftquietus.quietus.skill_tree.SkillTreeNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeWidget {
    /* Width and height responsible for calculation of hover and clicking */
    private static final int HEIGHT = 26;
    private static final int WIDTH = 26;

    /* Icon width and height */
    protected static final int ICON_HEIGHT = 26;
    protected static final int ICON_WIDTH = 26;

    private final SkillTreeTab tab;
    private final SkillTreeNode node;
    private final Minecraft minecraft;
    private final ResourceLocation icon;
    private final String languangeKey;

    private final int x;
    private final int y;

    private final WidgetType widgettype;

    public SkillTreeWidget(SkillTreeTab tab, Minecraft minecraft, SkillTreeNode node, int x, int y, WidgetType type) {
        this.tab = tab;
        this.minecraft = minecraft;
        this.node = node;
        this.icon = ResourceLocation.fromNamespaceAndPath(node.getId().getNamespace(), "textures/gui/icons/skill_tree/node/" + node.getId().getPath() + ".png");
        this.languangeKey = node.getId().toLanguageKey();
        this.x = x;
        this.y = y;

        this.widgettype = type;
    }

    public void draw(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        int render_x = this.x + offsetX;
        int render_y = this.y + offsetY;
        this.drawAbsolute(guiGraphics, render_x, render_y);
    }

    public void drawAbsolute(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(RenderType::guiTextured, this.widgettype.getLocation(false), x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        guiGraphics.blit(RenderType::guiTextured, this.icon, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
    }

    public boolean click(int offsetX, int offsetY, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isMouseOver(offsetX, offsetY, (int)mouseX, (int)mouseY)) {
            this.tab.getScreen().addWidgetScreen(this, new SkillTreeWidgetScreen(this.minecraft, this, this.tab.getScreen(), this.x, this.y));
            return true;
        }
        return false;
    }

    public boolean isMouseOver(int offsetX, int offsetY, int mouseX, int mouseY) {
        int actual_x = this.x + offsetX;
        int actual_y = this.y + offsetY;
        return 
            mouseX > actual_x 
            && mouseX < actual_x + WIDTH
            && mouseY > actual_y
            && mouseY < actual_y + HEIGHT;
    }

    public String getLanguageKey() {
        return this.languangeKey;
    }

}
