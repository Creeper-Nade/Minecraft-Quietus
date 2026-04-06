package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.skilltree.ConnectivityPosition;
import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.minecraftquietus.quietus.skilltree.TreePosition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeTab implements SkillTreeDraggable, SkillTreeScrollable {

    private static final boolean[] DOTTED_LINE_PATTERN = {true, true, false};
    private static final int SCROLL_EXTRA_MARGIN_X = 20;
    private static final int SCROLL_EXTRA_MARGIN_Y = 20;
    
    private final Minecraft minecraft;
    private final ClientSkillTree skillTree;
    private final SkillTreeScreen screen;
    private final int index;
    private final SkillCategory category;
    private final SkillCategory.DisplayInfo display;

    private final Map<SkillTreeNode,SkillTreeWidget> widgets = new LinkedHashMap();
    protected double scrollX;
    protected double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    private final TreePosition positioning;

    public SkillTreeTab(Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeScreen screen, int index, SkillCategory category, SkillCategory.DisplayInfo display, TreePosition positioning, double scrollX, double scrollY) {
        this.minecraft = minecraft;
        this.skillTree = clientSkillTree;
        this.screen = screen;

        this.index = index;
        this.category = category;
        this.display = display;

        this.scrollX = scrollX;
        this.scrollY = scrollY;

        this.positioning = positioning;
    }

    @Nullable
    public static SkillTreeTab create(Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeScreen screen, int index, SkillCategory category, TreePosition positioning) {
        Optional<SkillCategory.DisplayInfo> display = category.display();
        return display.map(displayInfo -> new SkillTreeTab(minecraft, clientSkillTree, screen, index, category, displayInfo, positioning, 0.0d, 0.0d)).orElse(null);
    }
    
    public void addWidget(SkillTreeNode node) {
        if (node.getSkillPoint().display().isPresent()) {
            TreePosition.Vertex vertexPos = this.positioning.getVertices().get(node);
            this.widgets.put(node, new SkillTreeWidget(this, this.minecraft, this.skillTree, node, vertexPos, node.getSkillPoint().display().get()));
            for (SkillTreeWidget widget : this.widgets.values()) {
                widget.attachToParent();
            }
            this.maxX = Math.max(this.maxX, vertexPos.x() + SkillTreeWidget.ICON_WIDTH + SCROLL_EXTRA_MARGIN_X);
            this.minX = Math.min(this.minX, vertexPos.x() - SCROLL_EXTRA_MARGIN_X);
            this.maxY = Math.max(this.maxY, vertexPos.y() + SkillTreeWidget.ICON_HEIGHT + SCROLL_EXTRA_MARGIN_Y);
            this.minY = Math.min(this.minY, vertexPos.y() - SCROLL_EXTRA_MARGIN_Y);
        }
    }

    public void drawContents(GuiGraphics guiGraphics, int offsetX, int offsetY, int width, int height) {
        guiGraphics.enableScissor(offsetX, offsetY, offsetX + width, offsetY + height);
        /* guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)offsetX, (float)offsetY, 0.0F); */

        guiGraphics.fill(offsetX, offsetY, offsetX+width, offsetY+height, 0xFFFFFFFF);

        this.clampScroll(0.0d, 0.0d);
        int relX = offsetX + (int)this.scrollX;
        int relY = offsetY + (int)this.scrollY;
        
        this.drawEdges(guiGraphics, relX, relY);

        for (SkillTreeWidget widget : this.widgets.values()) {
            widget.draw(guiGraphics, relX, relY);
        }

        guiGraphics.disableScissor();
    }

    private void drawEdges(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)offsetX, (float)offsetY, 0.0F);
        final int black = 0xFF000000;
        final int white = 0xFFFFFFFF;
        this.positioning.getEdges().forEach((edge) -> {
            if (edge.dotted()) {
                int patternOffset = 0;

                // Draw black outline parts (cross shape for each dot)
                // segment 1
                for (int y = edge.startY(); y < edge.midY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.startY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphics.fill(edge.startX() - 1, y, edge.startX() + 2, y + 1, black);
                        /* guiGraphics.fill(edge.startX(), y - 1, edge.startX() + 1, y + 2, black); */
                    }
                }
                patternOffset += (edge.midY() - edge.startY());
                // segment 2
                int x1 = edge.startX();
                int x2 = edge.finalX();
                if (x1 > x2) { int temp = x1; x1 = x2; x2 = temp; }
                for (int x = x1; x < x2; ++x) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + x - x1) % DOTTED_LINE_PATTERN.length]) {
                        /* guiGraphics.fill(x - 1, edge.midY(), x + 2, edge.midY() + 1, black); */
                        guiGraphics.fill(x, edge.midY() - 1, x + 1, edge.midY() + 2, black);
                    }
                }
                patternOffset += (x2 - x1);
                // segment 3
                for (int y = edge.midY(); y < edge.finalY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.midY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphics.fill(edge.finalX() - 1, y, edge.finalX() + 2, y + 1, black);
                        /* guiGraphics.fill(edge.finalX(), y - 1, edge.finalX() + 1, y + 2, black); */
                    }
                }
            } else {
                // Draw thick black background path
                guiGraphics.fill(edge.startX() - 1, edge.startY(), edge.startX() + 2, edge.midY(), black);
                guiGraphics.fill(edge.finalX() - 1, edge.midY(), edge.finalX() + 2, edge.finalY(), black);
                if (edge.startX() < edge.finalX()) {
                    guiGraphics.fill(edge.startX() - 1, edge.midY() - 1, edge.finalX() + 2, edge.midY() + 2, black);
                } else {
                    guiGraphics.fill(edge.finalX() - 1, edge.midY() - 1, edge.startX() + 2, edge.midY() + 2, black);
                }
            }
        });
        this.positioning.getEdges().forEach((edge) -> {
            if (edge.dotted()) {
                // Draw white fill parts
                int patternOffset = 0;
                // segment 1
                for (int y = edge.startY(); y < edge.midY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.startY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphics.fill(edge.startX(), y, edge.startX() + 1, y + 1, white);
                    }
                }
                patternOffset += (edge.midY() - edge.startY());
                // segment 2
                int x1 = edge.startX();
                int x2 = edge.finalX();
                if (x1 > x2) { int temp = x1; x1 = x2; x2 = temp; }
                for (int x = x1; x < x2; ++x) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + x - x1) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphics.fill(x, edge.midY(), x + 1, edge.midY() + 1, white);
                    }
                }
                patternOffset += (x2 - x1);
                // segment 3
                for (int y = edge.midY(); y < edge.finalY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.midY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphics.fill(edge.finalX(), y, edge.finalX() + 1, y + 1, white);
                    }
                }
            } else {
                // Draw thin white line on top
                guiGraphics.vLine(edge.startX(), edge.startY(), edge.midY(), white);
                guiGraphics.hLine(edge.startX(), edge.finalX(), edge.midY(), white);
                guiGraphics.vLine(edge.finalX(), edge.midY(), edge.finalY(), white);
            }
        });
        guiGraphics.pose().popPose();
    }

    @Override
    public void drag(double dragX, double dragY) {
        this.clampScroll(dragX, dragY);
    }
    @Override 
    public void scroll(double scrollX, double scrollY) {
        this.clampScroll(scrollX*16, scrollY*16);
    }

    private void clampScroll(double changeX, double changeY) {
        int innerWidth = this.screen.dynamicInsideWidth();
        int innerHeight = SkillTreeScreen.WINDOW_INSIDE_HEIGHT;
        int contentWidth = this.maxX - this.minX;
        int contentHeight = this.maxY - this.minY;
        if (contentWidth > innerWidth) {
            this.scrollX = Math.clamp(this.scrollX + changeX, (double) innerWidth - this.maxX, -this.minX);
        } else {
            this.scrollX = (innerWidth - (this.maxX + this.minX)) / 2.0;
        }

        if (contentHeight > innerHeight) {
            this.scrollY = Math.clamp(this.scrollY + changeY, (double) innerHeight - this.maxY, -this.minY);
        } else {
            this.scrollY = (innerHeight - (this.maxY + this.minY)) / 2.0;
        }
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

    public SkillCategory getCategory() {
        return this.category;
    }

    public SkillTreeScreen getScreen() {
        return this.screen;
    }
}
