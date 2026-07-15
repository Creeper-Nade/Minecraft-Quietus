package com.quietus.client.screens.skill_tree;

import static com.quietus.Quietus.MODID;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.skilltree.SkillCategory;
import com.quietus.skilltree.SkillTreeNode;
import com.quietus.skilltree.TreePosition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;

public class SkillTreeTab extends AbstractWidget implements SkillTreeDraggable, SkillTreeScrollable {

    protected static final int TAB_DISPLAY_WIDTH = 38;
    protected static final int TAB_DISPLAY_HEIGHT = 28;
    private static final Identifier TAB_DISPLAY_LOCATION = Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/tab.png");
    private static final Identifier TAB_DISPLAY_SELECTED_LOCATION = Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/tab_selected.png");
    private static final Identifier TAB_DISPLAY_HOVERED_LOCATION = Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/tab_hovered.png");
    private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/skill_tree/tab/none.png");

    private static final boolean[] DOTTED_LINE_PATTERN = {true, true, false};
    private static final int SCROLL_EXTRA_MARGIN_X = 20;
    private static final int SCROLL_EXTRA_MARGIN_Y = 20;
    
    private final Minecraft minecraft;
    private ClientSkillTree skillTree;
    private final SkillTreeScreen screen;
    private final SkillCategory category;
    private final SkillCategory.DisplayInfo display;
    private final Identifier icon;

    private final Map<SkillTreeNode,SkillTreeWidget> widgets = new LinkedHashMap();
    protected double scrollX;
    protected double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    private TreePosition positioning;

    public SkillTreeTab(Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeScreen screen, int x, int y, SkillCategory category, SkillCategory.DisplayInfo display, TreePosition positioning, double scrollX, double scrollY) {
        super(x, y, TAB_DISPLAY_WIDTH, TAB_DISPLAY_HEIGHT, display.name());

        this.minecraft = minecraft;
        this.skillTree = clientSkillTree;
        this.screen = screen;

        this.category = category;
        this.display = display;
        this.icon = display.icon().isPresent() ? 
            display.icon().get().id() :
            category.getId().withPath((id) -> "textures/gui/icons/skill_tree/tab/" + id + ".png");

        this.scrollX = scrollX;
        this.scrollY = scrollY;

        this.positioning = positioning;
    }

    @Nullable
    public static SkillTreeTab create(Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeScreen screen, SkillCategory category, TreePosition positioning) {
        Optional<SkillCategory.DisplayInfo> display = category.display();
        return display.map(displayInfo -> new SkillTreeTab(minecraft, clientSkillTree, screen, 0, 0, category, displayInfo, positioning, 0.0d, 0.0d)).orElse(null);
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

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        int x = this.getX();
        int y = this.getY();
        
        if (this.isActive()) {
            if (this.isHovered()) {
                gui.blit(RenderPipelines.GUI_TEXTURED, TAB_DISPLAY_HOVERED_LOCATION, x, y, 0.0f, 0.0f, 38, 28, 38, 28);
            } else {
                gui.blit(RenderPipelines.GUI_TEXTURED, TAB_DISPLAY_LOCATION, x, y, 0.0f, 0.0f, 38, 28, 38, 28);
            }
        } else {
            gui.blit(RenderPipelines.GUI_TEXTURED, TAB_DISPLAY_SELECTED_LOCATION, x, y, 0.0f, 0.0f, 38, 28, 38, 28);
        }
        if (this.minecraft.getResourceManager().getResource(this.icon).isPresent()) {
            gui.blit(RenderPipelines.GUI_TEXTURED, this.icon, x+5, y+5, 0.0f, 0.0f, 18, 18, 18, 18);
        } else {
            gui.blit(RenderPipelines.GUI_TEXTURED, DEFAULT_ICON, x+5, y+5, 0.0f, 0.0f, 18, 18, 18, 18);
        }
    }

    public void drawContents(GuiGraphicsExtractor guiGraphicsExtractor, int offsetX, int offsetY, int width, int height, int mouseX, int mouseY, float delta) {
        guiGraphicsExtractor.enableScissor(offsetX, offsetY, offsetX + width, offsetY + height);

        guiGraphicsExtractor.fill(offsetX, offsetY, offsetX+width, offsetY+height, 0xFFFFFFFF);

        this.clampScroll(0.0d, 0.0d);
        int relX = offsetX + (int)this.scrollX;
        int relY = offsetY + (int)this.scrollY;
        
        this.drawEdges(guiGraphicsExtractor, relX, relY);

        for (SkillTreeWidget widget : this.widgets.values()) {
            widget.updatePositionOffset(relX, relY);
            widget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta);
            //widget.draw(guiGraphicsExtractor, relX, relY);
        }

        guiGraphicsExtractor.disableScissor();
    }

    private void drawEdges(GuiGraphicsExtractor guiGraphicsExtractor, int offsetX, int offsetY) {
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate((float)offsetX, (float)offsetY);
        final int black = 0xFF000000;
        final int white = 0xFFFFFFFF;
        this.positioning.getEdges().forEach((edge) -> {
            if (edge.dotted()) {
                int patternOffset = 0;

                // Draw black outline parts (cross shape for each dot)
                // segment 1
                for (int y = edge.startY(); y < edge.midY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.startY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphicsExtractor.fill(edge.startX() - 1, y, edge.startX() + 2, y + 1, black);
                        /* GuiGraphicsExtractor.fill(edge.startX(), y - 1, edge.startX() + 1, y + 2, black); */
                    }
                }
                patternOffset += (edge.midY() - edge.startY());
                // segment 2
                int x1 = edge.startX();
                int x2 = edge.finalX();
                if (x1 > x2) { int temp = x1; x1 = x2; x2 = temp; }
                for (int x = x1; x < x2; ++x) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + x - x1) % DOTTED_LINE_PATTERN.length]) {
                        /* GuiGraphicsExtractor.fill(x - 1, edge.midY(), x + 2, edge.midY() + 1, black); */
                        guiGraphicsExtractor.fill(x, edge.midY() - 1, x + 1, edge.midY() + 2, black);
                    }
                }
                patternOffset += (x2 - x1);
                // segment 3
                for (int y = edge.midY(); y < edge.finalY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.midY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphicsExtractor.fill(edge.finalX() - 1, y, edge.finalX() + 2, y + 1, black);
                        /* GuiGraphicsExtractor.fill(edge.finalX(), y - 1, edge.finalX() + 1, y + 2, black); */
                    }
                }
            } else {
                // Draw thick black background path
                guiGraphicsExtractor.fill(edge.startX() - 1, edge.startY(), edge.startX() + 2, edge.midY(), black);
                guiGraphicsExtractor.fill(edge.finalX() - 1, edge.midY(), edge.finalX() + 2, edge.finalY(), black);
                if (edge.startX() < edge.finalX()) {
                    guiGraphicsExtractor.fill(edge.startX() - 1, edge.midY() - 1, edge.finalX() + 2, edge.midY() + 2, black);
                } else {
                    guiGraphicsExtractor.fill(edge.finalX() - 1, edge.midY() - 1, edge.startX() + 2, edge.midY() + 2, black);
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
                        guiGraphicsExtractor.fill(edge.startX(), y, edge.startX() + 1, y + 1, white);
                    }
                }
                patternOffset += (edge.midY() - edge.startY());
                // segment 2
                int x1 = edge.startX();
                int x2 = edge.finalX();
                if (x1 > x2) { int temp = x1; x1 = x2; x2 = temp; }
                for (int x = x1; x < x2; ++x) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + x - x1) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphicsExtractor.fill(x, edge.midY(), x + 1, edge.midY() + 1, white);
                    }
                }
                patternOffset += (x2 - x1);
                // segment 3
                for (int y = edge.midY(); y < edge.finalY(); ++y) {
                    if (DOTTED_LINE_PATTERN[(patternOffset + y - edge.midY()) % DOTTED_LINE_PATTERN.length]) {
                        guiGraphicsExtractor.fill(edge.finalX(), y, edge.finalX() + 1, y + 1, white);
                    }
                }
            } else {
                // Draw thin white line on top
                guiGraphicsExtractor.verticalLine(edge.startX(), edge.startY(), edge.midY(), white);
                guiGraphicsExtractor.horizontalLine(edge.startX(), edge.finalX(), edge.midY(), white);
                guiGraphicsExtractor.verticalLine(edge.finalX(), edge.midY(), edge.finalY(), white);
            }
        });
        guiGraphicsExtractor.pose().popMatrix();
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

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.screen.setSelectedTab(this);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        //soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
    
    public boolean clickOnTree(MouseButtonEvent event, boolean doubleClick) {
        for (SkillTreeWidget widget : this.widgets.values()) {
            if (widget.mouseClicked(event, doubleClick))
                return true;
        }
        return false;
    }

    public SkillTreeWidget getWidget(SkillTreeNode node) {
        return this.widgets.get(node);
    }
    public SkillTreeWidget getWidget(Identifier id) {
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
    protected TreePosition getPositioning() {
        return this.positioning;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateWidgetNarration'");
    }
}
