package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.skilltree.ConnectivityPosition;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeTab implements SkillTreeScrollable {
    
    private final Minecraft minecraft;
    private final SkillTreeScreen screen;
    private final int index;
    private final SkillCategory.DisplayInfo display;

    private final Map<SkillTreeNode,SkillTreeWidget> widgets = new LinkedHashMap();
    protected double scrollX;
    protected double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private final ConnectivityPosition connectivity;

    public SkillTreeTab(Minecraft minecraft, SkillTreeScreen screen, int index, SkillCategory category, SkillCategory.DisplayInfo display, ConnectivityPosition connectivity, double scrollX, double scrollY) {
        this.minecraft = minecraft;
        this.screen = screen;

        this.index = index;
        this.display = display;

        this.scrollX = scrollX;
        this.scrollY = scrollY;

        this.connectivity = connectivity;
    }

    @Nullable
    public static SkillTreeTab create(Minecraft minecraft, SkillTreeScreen screen, int index, SkillCategory category, ConnectivityPosition connectivity) {
        Optional<SkillCategory.DisplayInfo> display = category.getDisplay();
        return display.map(displayInfo -> new SkillTreeTab(minecraft, screen, index, category, displayInfo, connectivity, 0.0d, 0.0d)).orElse(null);
    }

    /* public void testAdd(SkillTreeNode node, SkillTreeWidget widget) {
        this.widgets.put(node, widget);
    } */
    
    public void addWidget(SkillTreeNode node) {
        if (node.getSkillPoint().display().isPresent()) {
            this.widgets.put(node, new SkillTreeWidget(this, this.minecraft, node, node.getTreeX(), node.getTreeY(), node.getSkillPoint().display().get()));
            for (SkillTreeWidget widget : this.widgets.values()) {
                widget.attachToParent();
            }
        }
    }

    public void drawContents(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.enableScissor(offsetX, offsetY, offsetX + SkillTreeScreen.WINDOW_INSIDE_WIDTH, offsetY + SkillTreeScreen.WINDOW_INSIDE_HEIGHT);
        /* guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)offsetX, (float)offsetY, 0.0F); */


        guiGraphics.fill(offsetX, offsetY, offsetX+SkillTreeScreen.WINDOW_INSIDE_WIDTH, offsetY+SkillTreeScreen.WINDOW_INSIDE_HEIGHT, 0xFFFFFFFF);

        int relX = offsetX + (int)this.scrollX;
        int relY = offsetY + (int)this.scrollY;
        //guiGraphics.vLine(relX + 13 + 25, relY + 13 + 25, relY + 13 + 25 + 26 + 6, 0xFF000000);
        
        /* for (SkillTreeWidget widget : this.widgets.values()) {
            widget.drawConnectivity(guiGraphics, relX, relY);
        }
        for (SkillTreeWidget widget : this.widgets.values()) {
            widget.drawConnectivity(guiGraphics, relX, relY);
        } */
        for (SkillTreeWidget widget : this.widgets.values()) {
            widget.draw(guiGraphics, relX, relY);
        }

        //guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    @Override
    public void scroll(double dragX, double dragY) {
        this.scrollX += dragX;
        this.scrollY += dragY;
    }

    public boolean click(int offsetX, int offsetY, double mouseX, double mouseY, int mouseButton) {
        for (SkillTreeWidget widget : this.widgets.values()) {
            if (widget.click(offsetX, offsetY, mouseX, mouseY, mouseButton)) 
                return true;
        }
        return false;
    }

    public SkillTreeWidget getWidget(SkillTreeNode node) {
        return this.widgets.get(node);
    }
    public SkillTreeWidget getWidget(ResourceLocation id) {
        for (SkillTreeNode node : this.widgets.keySet()) {
            if (node.getId().equals(id))
                return this.getWidget(node);
        }
        return null;
    }

    public SkillTreeScreen getScreen() {
        return this.screen;
    }
}
