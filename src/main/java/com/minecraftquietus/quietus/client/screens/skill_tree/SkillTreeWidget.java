package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.Set;

import com.minecraftquietus.quietus.skilltree.SkillPoint;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
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
    private final Set<SkillTreeWidget> mustParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> orParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> children = new ReferenceOpenHashSet<>();

    private final SkillPointType widgettype;

    public SkillTreeWidget(SkillTreeTab tab, Minecraft minecraft, SkillTreeNode node, int x, int y, SkillPoint.DisplayInfo display) {
        this.tab = tab;
        this.minecraft = minecraft;
        this.node = node;
        this.icon = display.icon().isPresent() ? 
            display.icon().get().texturePath() : 
            node.getId().withPath((id) -> "textures/gui/icons/skill_tree/node/" + id + ".png");
            //ResourceLocation.fromNamespaceAndPath(node.getId().getNamespace(), "textures/gui/icons/skill_tree/node/" + node.getId().getPath() + ".png");
        this.languangeKey = node.getId().toLanguageKey();
        this.x = x;
        this.y = y;

        this.widgettype = display.type();
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

    public void addChild(SkillTreeWidget child) {
        this.children.add(child);
    }

    public void attachToParent() {
        if (this.node.parents().size() > 0) {
            this.node.mustParents().forEach((node) -> this.mustParents.add(this.tab.getWidget(node)));
            this.node.orParents().forEach((node) -> this.orParents.add(this.tab.getWidget(node)));

            this.node.parents().forEach((node) -> this.tab.getWidget(node).addChild(this));
        }
    }

    public String getLanguageKey() {
        return this.languangeKey;
    }

}
