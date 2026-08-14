package com.quietus.client.screens.skill_tree;

import static com.quietus.Quietus.MODID;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.client.util.GuiGraphicsExtractorUtil;
import com.quietus.skilltree.SkillCategory;
import com.quietus.skilltree.SkillTreeNode;
import com.quietus.skilltree.TreePosition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;

public class SkillTreeTab extends AbstractWidget implements SkillTreeDraggable, SkillTreeScrollable {

    protected static final int TAB_DISPLAY_WIDTH = 38;
    protected static final int TAB_DISPLAY_HEIGHT = 28;
    protected static final int TAB_ICON_WIDTH = 18;
    protected static final int TAB_ICON_HEIGHT = 18;
    private static final int TAB_BACKGROUND_TILE_WIDTH = 32;
    private static final int TAB_BACKGROUND_TILE_HEIGHT = 32;
    private static final WidgetSprites TAB_SPRITES = new WidgetSprites(
        Identifier.fromNamespaceAndPath(MODID, "skill_tree/tab"),
        Identifier.fromNamespaceAndPath(MODID, "skill_tree/tab_selected"),
        Identifier.fromNamespaceAndPath(MODID, "skill_tree/tab_hovered"),
        Identifier.fromNamespaceAndPath(MODID, "skill_tree/tab_selected")
    );
    private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/skill_tree/tab/none.png");

    private static final boolean[] DOTTED_LINE_PATTERN = {true, true, false};
    private static final int SCROLL_EXTRA_MARGIN_X = 20;
    private static final int SCROLL_EXTRA_MARGIN_Y = 20;
    
    private final Minecraft minecraft;
    private final Font font;
    private ClientSkillTree skillTree;
    private final SkillTreeScreen screen;
    private final SkillCategory category;
    private final SkillCategory.DisplayInfo display;
    private final Identifier icon;

    private final Map<SkillTreeNode,SkillTreeWidget> widgets = new LinkedHashMap<>();
    protected double treeScrollX;
    protected double treeScrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    private int relX, relY = 0;

    private TreePosition positioning;

    public SkillTreeTab(Minecraft minecraft, Font font, ClientSkillTree clientSkillTree, SkillTreeScreen screen, int x, int y, SkillCategory category, SkillCategory.DisplayInfo display, TreePosition positioning, double scrollX, double scrollY) {
        super(x, y, TAB_DISPLAY_WIDTH, TAB_DISPLAY_HEIGHT, display.name());

        this.minecraft = minecraft;
        this.font = font;
        this.skillTree = clientSkillTree;
        this.screen = screen;

        this.category = category;
        this.display = display;
        this.icon = display.icon().isPresent() ? 
            display.icon().get().id() :
            category.getId().withPath((id) -> "textures/gui/icons/skill_tree/tab/" + id + ".png");

        this.treeScrollX = scrollX;
        this.treeScrollY = scrollY;

        this.positioning = positioning;

        this.setTooltip(this.buildTooltip());
    }

    private Tooltip buildTooltip() {
        Component name = this.display.name();
        Component description = this.display.description();
        return Tooltip.create(MutableComponent.create(name.getContents()).append("\n").append(description));
    }

    @Nullable
    public static SkillTreeTab create(Minecraft minecraft, Font font, ClientSkillTree clientSkillTree, SkillTreeScreen screen, SkillCategory category, TreePosition positioning) {
        Optional<SkillCategory.DisplayInfo> display = category.display();
        return display.map(displayInfo -> new SkillTreeTab(minecraft, font, clientSkillTree, screen, 0, 0, category, displayInfo, positioning, 0.0d, 0.0d)).orElse(null);
    }
    
    protected void applyScrollData(TabScrollData data) {
        this.treeScrollX = data.scrollX();
        this.treeScrollY = data.scrollY();
        this.clampTreeScroll(0.0d, 0.0d);
    }
    protected TabScrollData makeScrollData() {
        return new TabScrollData(this.treeScrollX, this.treeScrollY);
    }

    public void addWidget(SkillTreeNode node) {
        if (node.getSkillPoint().display().isPresent()) {
            TreePosition.Vertex vertexPos = this.positioning.getVertices().get(node);
            this.widgets.put(node, new SkillTreeWidget(this, this.minecraft, this.font, this.skillTree, node, vertexPos, node.getSkillPoint().display().get()));
            for (SkillTreeWidget widget : this.widgets.values()) {
                widget.attachToParent();
            }
            this.maxX = Math.max(this.maxX, vertexPos.x() + SkillTreeWidget.ICON_WIDTH + SCROLL_EXTRA_MARGIN_X);
            this.minX = Math.min(this.minX, vertexPos.x() - SCROLL_EXTRA_MARGIN_X);
            this.maxY = Math.max(this.maxY, vertexPos.y() + SkillTreeWidget.ICON_HEIGHT + SCROLL_EXTRA_MARGIN_Y);
            this.minY = Math.min(this.minY, vertexPos.y() - SCROLL_EXTRA_MARGIN_Y);
        }
    }

    protected void renderTick(int offsetX, int offsetY, float delta) {
        this.clampTreeScroll(0.0d, 0.0d);
        this.relX = offsetX + (int)this.treeScrollX;
        this.relY = offsetY + (int)this.treeScrollY;

        this.widgets.values().forEach(widget -> {
            widget.updatePositionOffset(this.relX, this.relY);
            
            if (
                widget.getX() + SkillTreeWidget.WIDTH < offsetX ||
                widget.getY() + SkillTreeWidget.HEIGHT < offsetY ||
                widget.getX() > offsetX + this.screen.dynamicInsideWidth() ||
                widget.getY() > offsetY + SkillTreeScreen.WINDOW_INSIDE_HEIGHT
            ) { // hide and deactivate widget when it would be cropped out by scissors (see {@link SkillTreeScreen})
                widget.active = false;
                widget.visible = false;
            } else {
                widget.active = true;
                widget.visible = true;
            }

            if (widget.getTooltipTicks() > 0 || widget.isHovered()) { // widget's tooltip tick for tooltip animations
                widget.tooltipRenderTick();
            }
        });
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        int x = this.getX();
        int y = this.getY();
        
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, TAB_SPRITES.get(this.isActive(), this.isHovered()), x, y, 38, 28);
        if (this.isActive() && this.isHovered()) {
            gui.requestCursor(CursorTypes.POINTING_HAND);
        }
        gui.blit(RenderPipelines.GUI_TEXTURED, this.getIconLocation(), x + 5, y + 5, 0.0f, 0.0f, 18, 18, 18, 18);
    }

    public void drawTreeWidgetsAndEdges(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        
        this.drawTreeEdges(gui, this.relX, this.relY);

        for (SkillTreeWidget widget : this.widgets.values()) {
            widget.extractRenderState(gui, mouseX, mouseY, delta);
            if (widget.getNode().equals(this.screen.getSelectedNode())) {
                widget.extractSelectedHighlight(gui);
            }
        }

        boolean overClickable = false;
        for (SkillTreeWidget widget : this.widgets.values()) {
            if (widget.isHovered() && widget.isActive()) {
                overClickable = true;
            }
        }
        if (overClickable) gui.requestCursor(CursorTypes.POINTING_HAND);
    }

    public void drawTreeBackground(GuiGraphicsExtractor gui, int offsetX, int offsetY, int width, int height, int mouseX, int mouseY, float delta) {
        gui.enableScissor(offsetX, offsetY, offsetX + width, offsetY + height);
        if (this.display.background().isPresent()) {
            Identifier bgLocation = this.display.background().get();
            int tileOffsetX = Math.floorMod((int) Math.floor(this.treeScrollX), TAB_BACKGROUND_TILE_WIDTH);
            int tileOffsetY = Math.floorMod((int) Math.floor(this.treeScrollY), TAB_BACKGROUND_TILE_HEIGHT);

            int startX = offsetX + tileOffsetX - TAB_BACKGROUND_TILE_WIDTH;
            int startY = offsetY + tileOffsetY - TAB_BACKGROUND_TILE_HEIGHT;
            int endX = offsetX + width;
            int endY = offsetY + height;

            for (int px = startX; px < endX; px += TAB_BACKGROUND_TILE_WIDTH) {
                for (int py = startY; py < endY; py += TAB_BACKGROUND_TILE_HEIGHT) {
                    gui.blit(RenderPipelines.GUI_TEXTURED, bgLocation, px, py, 0.0f, 0.0f, TAB_BACKGROUND_TILE_WIDTH, TAB_BACKGROUND_TILE_HEIGHT, TAB_BACKGROUND_TILE_WIDTH, TAB_BACKGROUND_TILE_HEIGHT);
                }
            }
        } else {
            gui.fill(offsetX, offsetY, offsetX + width, offsetY + height, SkillTreeScreen.WINDOW_BACKGROUND_COLOUR);
        }
        gui.disableScissor();
    }

    private void drawTreeEdges(GuiGraphicsExtractor guiGraphicsExtractor, int offsetX, int offsetY) {
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

    public @Nullable void drawWidgetsTooltips(GuiGraphicsExtractor gui, int mouseX, int mouseY, SkillTreeNode selectedNode, ClientSkillTree tree) {
        SkillTreeWidget hoveredWidget = null;
        for (SkillTreeWidget widget : this.widgets.values()) {
            if (widget.getNode().equals(selectedNode)) {
                continue;
            }
            if (widget.isMouseOver(mouseX, mouseY)) {
                hoveredWidget = widget;
            } else {
                if (widget.getTooltipTicks() > 0) {
                    widget.extractHoverTooltip(gui);
                }
            }
        }
        if (hoveredWidget != null) {
            hoveredWidget.extractHoverTooltip(gui);
        }
    }

    @Override
    public void drag(double dragX, double dragY) {
        this.clampTreeScroll(dragX, dragY);
    }
    @Override 
    public void scroll(double scrollX, double scrollY) {
        this.clampTreeScroll(scrollX*16, scrollY*16);
    }

    private void clampTreeScroll(double changeX, double changeY) {
        int innerWidth = this.screen.dynamicInsideWidth();
        int innerHeight = SkillTreeScreen.WINDOW_INSIDE_HEIGHT;
        int contentWidth = this.maxX - this.minX;
        int contentHeight = this.maxY - this.minY;

        if (contentWidth > innerWidth) {
            this.treeScrollX = Math.clamp(this.treeScrollX + changeX, (double) innerWidth - this.maxX, -this.minX);
        } else {
            this.treeScrollX = (innerWidth - (this.maxX + this.minX)) / 2.0;
        }

        if (contentHeight > innerHeight) {
            this.treeScrollY = Math.clamp(this.treeScrollY + changeY, (double) innerHeight - this.maxY, -this.minY);
        } else {
            this.treeScrollY = (innerHeight - (this.maxY + this.minY)) / 2.0;
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.screen.setSelectedTab(this.getId());
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

    protected TabSelectionElement createTabSelectionElement(int x, int y, int width, int height) {
        return this.new TabSelectionElement(x, y, width, height);
    }

    protected class TabSelectionElement extends AbstractWidget {
        private static final Identifier TAB_ELEMENT_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/tab_selection");
        private static final int TOP_PADDING = 4;
        private static final int ICON_TEXT_PADDING = 2;
        private static final int TEXT_LINE_SPACING = 2;
        private static final int TEXT_HORIZONTAL_PADDING = 3;

        protected boolean clicked = false; // used for determining a full click (mouse on top, mouse down followed by mouse up)

        TabSelectionElement(int x, int y, int width, int height) {
            super(x, y, width, height, SkillTreeTab.this.display.name());

            this.clicked = false;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouesX, int mouseY, float delta) {
            Component text = SkillTreeTab.this.display.name();

            int textMaxWidth = Math.max(5,this.width - TEXT_HORIZONTAL_PADDING*2);

            int textHeight = GuiGraphicsExtractorUtil.getWordWrapHeight(SkillTreeTab.this.font, text, textMaxWidth, TEXT_LINE_SPACING);
            int iconAndTextHeight = SkillTreeTab.TAB_ICON_HEIGHT + ICON_TEXT_PADDING + textHeight;
            
            int x = this.getX();
            int y = this.getY();
            int iconX = this.getX() + (int)Math.floor(this.width/2d - SkillTreeTab.TAB_ICON_WIDTH/2d);
            int iconY = Math.max(this.getY() + TOP_PADDING, this.getY() + (int)Math.floor(this.height/2d - iconAndTextHeight/2d));
            int textCenterX = this.getX() + (int)Math.floor(this.width/2d);
            int textY = iconY + SkillTreeTab.TAB_ICON_HEIGHT + ICON_TEXT_PADDING;

            // fill
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, TAB_ELEMENT_SPRITE_LOCATION, x, y, this.width, this.height);
            /* white fill if hovered */
            if (this.isHovered) {
                gui.fill(iconX, iconY, iconX + TAB_ICON_WIDTH, iconY + TAB_ICON_HEIGHT, 0x40FFFFFF);
            }
            // icon
            gui.blit(RenderPipelines.GUI_TEXTURED, SkillTreeTab.this.getIconLocation(), iconX, iconY, 0.0f, 0.0f, SkillTreeTab.TAB_ICON_WIDTH, SkillTreeTab.TAB_ICON_HEIGHT, SkillTreeTab.TAB_ICON_WIDTH, SkillTreeTab.TAB_ICON_HEIGHT);
            // text
            GuiGraphicsExtractorUtil.drawCenteredWordWrap(gui, font, text, textCenterX, textY, textMaxWidth, TEXT_LINE_SPACING, ARGB.opaque(SkillTreeTab.this.getThemeColour()));

            // cursor
            if (this.isHovered() && this.isActive() && !SkillTreeTab.this.screen.isDragging()) {
                gui.requestCursor(CursorTypes.POINTING_HAND);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!this.isActive()) {
                return false;
            } else {
                if (this.isValidClickButton(event.buttonInfo())) {
                    boolean isMouseOver = this.isMouseOver(event.x(), event.y());
                    if (isMouseOver) {
                        this.onClick(event, doubleClick);
                        return true;
                    }
                }

                return false;
            }
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            this.clicked = true;
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            if (this.clicked && this.isMouseOver(event.x(), event.y())) {
                SkillTreeTab.this.screen.setSelectedTabAndTop(SkillTreeTab.this.getId());
                SkillTreeTab.this.screen.closeTabsSelectionGrid();
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            this.clicked = false;
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            //soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput arg0) {
            // TODO Auto-generated method stub
        }
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
    public @Nullable SkillTreeWidget getHoveredWidget(int mouseX, int mouseY) {
        for (SkillTreeWidget widget : this.widgets.values()) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget;
            }
        }
        return null;
    }

    public SkillCategory getCategory() {
        return this.category;
    }
    public Identifier getId() {
        return this.category.getId();
    }
    public Component getName() {
        return this.display.name();
    }
    public Identifier getIconLocation() {
        return this.minecraft.getResourceManager().getResource(this.icon).isPresent() ?
          this.icon :
          DEFAULT_ICON;
    }
    public int getThemeColour() {
        return this.display.themeColour();
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

    public record TabScrollData(
        double scrollX,
        double scrollY
    ) {}
}
