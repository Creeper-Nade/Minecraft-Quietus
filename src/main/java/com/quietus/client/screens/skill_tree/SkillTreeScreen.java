package com.quietus.client.screens.skill_tree;

import static com.quietus.Quietus.MODID;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.quietus.util.ServerPacketDistributor;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.quietus.client.QuietusKeyBindings;
import com.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.skilltree.SkillTreeNode;
import com.quietus.skilltree.TreePosition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.logging.LogUtils;
import com.quietus.skilltree.SkillCategory;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SkillTreeScreen extends Screen implements SkillCategory.Listener {
    private static final Logger LOGGER = LogUtils.getLogger();

    //private static final Identifier WINDOW_LOCATION = Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/window.png");
    private static final Identifier WINDOW_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/window");

    public static final int WINDOW_WIDTH = 248;
    public static final int WINDOW_HEIGHT = 186;
    public static final int WINDOW_WIDTH_INFO_CHANGE = -84;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 9;
    private static final int WINDOW_INSIDE_TOP_Y = 18;
    protected static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH-WINDOW_INSIDE_X*2;
    protected static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT-WINDOW_INSIDE_Y-WINDOW_INSIDE_TOP_Y;
    private static final int GAP_WINDOW_INFO = 7;
    
    protected static final int WIDGET_MARGIN_WIDTH = 6;
    protected static final int WIDGET_MARGIN_HEIGHT = 9;
    
    private static final int MAX_TABS_PER_PAGE = 6;
    
    private static final int INFO_DYNAMIC_OFFSET_FROM_CENTER = - (GAP_WINDOW_INFO + SkillTreeInfoScreen.WIDTH)/2;
    private static final int DYNAMIC_POSITIONING_TICKS = 40;
    
    private int infoDynamicTicks = DYNAMIC_POSITIONING_TICKS;
    private int infoWindowDynamicWidth = WINDOW_WIDTH;
    private int infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH;
    private int infoDynamicOffset = 0;
    private float infoDynamicTicksF = DYNAMIC_POSITIONING_TICKS;
    private float infoWindowDynamicWidthF = WINDOW_WIDTH;
    private float infoWindowInsideDynamicWidthF = WINDOW_INSIDE_WIDTH;
    private float infoDynamicOffsetF = 0.0f;

    private int offsetX, offsetY, offsetXTree, offsetYTree, offsetXInfo, offsetYInfo = 0;
    private float offsetXFTree, offsetXFInfo = 0.0f;
    

    private static final Component TITLE = Component.translatable("gui.skill_tree");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private final ClientSkillTree skillTree;
    private final Map<Identifier,SkillTreeTab> tabs = new LinkedHashMap<>();

    private final Map<SkillTreeWidget,SkillTreeWidgetScreen> widgetScreens = new LinkedHashMap<>();

    private SkillTreeDraggable focusedDraggable = null;
    private SkillTreeScrollable focusedScrollable = null;
    @Nullable private SkillTreeTab selectedTab = null;
    @Nullable private SkillTreeNode selectedNode;
    @Nullable private SkillTreeInfoScreen selectedWidgetInfo;


    public SkillTreeScreen(ClientSkillTree skillTree) {
        super(TITLE);

        this.skillTree = skillTree;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (QuietusKeyBindings.SKILL_TREE_KEY.get().matches(event)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        } else if (this.minecraft.options.keyInventory.matches(event)) {
            if (this.minecraft.gameMode.isServerControlledInventory()) {
                this.minecraft.player.sendOpenInventory();
            } else {
                this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            }
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_ESCAPE && this.selectedWidgetInfo != null) {
            this.selectedNode = null;
            this.selectedWidgetInfo = null;
            return true;
        } else {
            return super.keyPressed(event);
        }
    }

    public void makeTabs() {
        this.tabs.clear();
        ClientSkillTreePayloadHandler.getCategories().forEach((id, category) -> {
            TreePosition positioning = new TreePosition(SkillTreeWidget.ICON_WIDTH, SkillTreeWidget.ICON_WIDTH, WIDGET_MARGIN_WIDTH, WIDGET_MARGIN_HEIGHT, category.seed());
            positioning.makeGraphOf(category); 
            SkillTreeTab createdtab = SkillTreeTab.create(this.minecraft, this.skillTree, this, category, positioning);
            if (!Objects.isNull(createdtab))
                this.tabs.put(id, createdtab);
            category.setListener(this); // adds widgets to the tab via SkillCategory's listener
        });
        if (!this.tabs.isEmpty()) {
            if (this.selectedTab == null) {
                this.selectedTab = this.tabs.values().iterator().next();
            } else { // already has selected tab
                SkillTreeTab rebuiltSelectedTab = this.tabs.get(this.selectedTab.getCategory().getId());
                if (rebuiltSelectedTab == null) {
                    LOGGER.info("The client has skill category {} selected but it exists no more!", this.selectedTab.getCategory().getId().toString());
                    this.selectedTab = null;
                } else {
                    this.selectedTab = rebuiltSelectedTab;
                }
            }
            if (this.selectedNode == null) {
                this.selectedWidgetInfo = null; // just in case
            } else {
                SkillTreeTab rebuiltSelectedNodeTab = this.tabs.get(this.selectedNode.getCategoryId());
                if (rebuiltSelectedNodeTab == null) {
                    this.setSelectedNode(null);
                    LOGGER.info("The client has skill node {} selected but the category {} to which it belongs exists no more!", this.selectedNode.getId().toString(), this.selectedNode.getCategoryId().toString());
                } else {
                    SkillTreeWidget rebuiltSelectedWidget = rebuiltSelectedNodeTab.getWidget(this.selectedNode);
                    if (rebuiltSelectedWidget == null) {
                        LOGGER.info("The client has skill node {} selected but the category {} to which it belongs does not have such node!", this.selectedNode.getId().toString(), this.selectedNode.getCategoryId().toString());
                    } else {
                        this.selectedWidgetInfo = SkillTreeInfoScreen.create(rebuiltSelectedWidget, this.font, this);
                    }
                }
            }
        }
    }

    @Override
    public void init() {
        ServerPacketDistributor.requestSkillTreeUpdate();
        /* Header */
        this.layout.addTitleHeader(TITLE, this.font);
        /* Setup */ 
        this.makeTabs(); 
        /* Footer */
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        this.layout.visitWidgets(widget -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(widget);
        });
        this.repositionElements();

        /* Initial render tick */
        this.renderTick(0.0f);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    /* @Override
    public void tick() {
    } */

    private void renderTick(float delta) {
        /* Animations */
        // int
        this.infoDynamicTicks = this.selectedNode == null ?
            Math.min(this.infoDynamicTicks+1, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.infoDynamicTicks-1, 0);
        this.infoWindowDynamicWidth = WINDOW_WIDTH + (int)Math.round((1.0d - (double)this.infoDynamicTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        this.infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + (int)Math.round((1.0d - (double)this.infoDynamicTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        
        this.infoDynamicOffset = (int)Math.round(calcInverse((double)INFO_DYNAMIC_OFFSET_FROM_CENTER,(double)DYNAMIC_POSITIONING_TICKS, 80.0d, this.infoDynamicTicks, this.selectedNode == null));

        this.infoWindowDynamicWidth = WINDOW_WIDTH + (int)Math.round(calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicks, this.selectedNode == null));
        this.infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + (int)Math.round(calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicks, this.selectedNode == null));
        
        // float
        this.infoDynamicTicksF = this.selectedNode == null ?
            Math.min(this.infoDynamicTicks-1+delta, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.infoDynamicTicks+1-delta, 0);
        this.infoWindowDynamicWidthF = WINDOW_WIDTH + (float)((1.0d - this.infoDynamicTicksF / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        this.infoWindowInsideDynamicWidthF = WINDOW_INSIDE_WIDTH + (float)((1.0d - this.infoDynamicTicksF / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        
        this.infoDynamicOffsetF = (float)calcInverse((double)INFO_DYNAMIC_OFFSET_FROM_CENTER,(double)DYNAMIC_POSITIONING_TICKS, 80.0d, this.infoDynamicTicksF, this.selectedNode == null);

        this.infoWindowDynamicWidthF = WINDOW_WIDTH + (float)calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicksF, this.selectedNode == null);
        this.infoWindowInsideDynamicWidthF = WINDOW_INSIDE_WIDTH + (float)calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicksF, this.selectedNode == null);

        /* Offset calculation */
        this.offsetY = (this.height - WINDOW_HEIGHT) / 2;
        this.offsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        this.offsetXTree = this.offsetX + WINDOW_INSIDE_X;
        this.offsetYTree = this.offsetY + WINDOW_INSIDE_TOP_Y;
        this.offsetXInfo = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoWindowDynamicWidth + GAP_WINDOW_INFO + this.infoDynamicOffset;
        this.offsetXFTree = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidthF) / 2 + this.infoDynamicOffsetF;
        this.offsetXFInfo = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidthF) / 2 + this.infoWindowDynamicWidthF + GAP_WINDOW_INFO + this.infoDynamicOffsetF;


        /* Tabs tick */
        Iterator<SkillTreeTab> it = tabs.values().iterator();
        for (int i = 0; it.hasNext(); i++) { 
            SkillTreeTab tab = it.next();
            tab.visible = false;
            tab.active = this.selectedTab == null ? true : !this.selectedTab.getCategory().equals(tab.getCategory());
            tab.setPosition(this.offsetX-SkillTreeTab.TAB_DISPLAY_WIDTH+3, this.offsetY+12+SkillTreeTab.TAB_DISPLAY_HEIGHT*i);
        }
        Iterator<SkillTreeTab> it2 = tabs.values().iterator();
        for (int i = 0; i < MAX_TABS_PER_PAGE && it2.hasNext(); i++) { // show only first MAX_TABS_PER_PAGE tabs (supported by this GUI)
            SkillTreeTab tab = it2.next();
            tab.visible = true;
        }
        if (this.selectedTab != null) {
            this.selectedTab.renderTick(this.offsetXTree, this.offsetYTree, delta);
        }

        /* Info Screen tick */
        if (this.selectedWidgetInfo != null) {
            this.selectedWidgetInfo.update(this.skillTree);
            this.selectedWidgetInfo.renderTick(this.offsetXInfo, this.offsetYInfo, delta);

            if (this.selectedTab != null && this.selectedTab.getPositioning().getVertices().containsKey(this.selectedNode)) { // only make dynamic info window y when there is positioning for the selected node from selected tab
                this.offsetYInfo = offsetY + this.selectedTab.getPositioning().getVertices().get(this.selectedNode).y() + WINDOW_INSIDE_TOP_Y + (int)this.selectedTab.scrollY;
                this.offsetYInfo += Math.min(0, (offsetY + WINDOW_HEIGHT) - (this.offsetYInfo - selectedWidgetInfo.getTopHeight() + selectedWidgetInfo.getHeight())); // clamps the bottom if InfoScreen has lower bottom
                this.offsetYInfo += Math.max(0, offsetY - (this.offsetYInfo - selectedWidgetInfo.getTopHeight())); // clamps the top if InfoScreen has higher top
            }
        }
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.extractRenderState(gui, mouseX, mouseY, delta);

        this.renderTick(delta);

        gui.pose().pushMatrix();
        gui.pose().translate(this.offsetXFTree, 0.0f);
        this.renderTreeWindow(gui, mouseX, mouseY, delta, 0, this.offsetY);
        gui.pose().popMatrix();

        gui.nextStratum();

        gui.pose().pushMatrix();
        gui.pose().translate(this.offsetXFTree - this.offsetX, 0.0f);
        this.tabs.values().forEach(tab -> tab.extractRenderState(gui, mouseX, mouseY, delta));
        gui.pose().popMatrix();

        gui.nextStratum();

        if (this.selectedNode != null) {
            gui.pose().pushMatrix();
            gui.pose().translate(this.offsetXFInfo, 0.0f);
            this.renderInfoWindow(gui, mouseX, mouseY, delta, 0, this.offsetYInfo);
            gui.pose().popMatrix();
        }
    }

    private void renderTreeWindow(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta, int offsetX, int offsetY) {
        if (this.selectedTab == null) {

        } else {
            this.selectedTab.drawBackground(gui, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, this.infoWindowInsideDynamicWidth, WINDOW_INSIDE_HEIGHT, mouseX, mouseY, delta);
            gui.enableScissor(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, offsetX + WINDOW_INSIDE_X + this.infoWindowInsideDynamicWidth, offsetY + WINDOW_INSIDE_TOP_Y + WINDOW_INSIDE_HEIGHT);
            gui.pose().translate(-this.offsetX, 0.0f);
            this.selectedTab.drawWidgetsAndEdges(gui, mouseX, mouseY, delta);
            gui.disableScissor();
            gui.pose().translate(+this.offsetX, 0.0f);
        }
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_SPRITE_LOCATION, offsetX, offsetY, this.infoWindowDynamicWidth, WINDOW_HEIGHT);
    }

    private void renderInfoWindow(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta, int offsetX, int offsetY) {
        this.selectedWidgetInfo.draw(gui, mouseX, mouseY, offsetX, this.offsetYInfo, delta, this.skillTree);
        gui.pose().translate(-this.offsetXInfo, 0.0f);
        this.selectedWidgetInfo.drawWidgets(gui, mouseX, mouseY, delta);
        gui.pose().translate(+this.offsetXInfo, 0.0f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (this.selectedWidgetInfo != null && this.selectedWidgetInfo.isMouseOverWindow(this.offsetXInfo, this.offsetYInfo, mouseX, mouseY)) {
            this.focusedDraggable = this.selectedWidgetInfo;

            if (this.selectedWidgetInfo.mouseClicked(event, doubleClick)) {
                return true;
            }
        } else if (
            mouseX > this.offsetX
            && mouseY > this.offsetYTree
            && mouseX < this.offsetX + this.width
            && mouseY < this.offsetYTree + WINDOW_HEIGHT
        ) {
            this.focusedDraggable = this.selectedTab;
        } else {
            this.focusedDraggable = null;
        }

        if (this.selectedTab != null) {
            if (this.selectedTab.clickOnTree(event, doubleClick)) {
                return true;
            }
        }

        for (SkillTreeTab tab : this.tabs.values()) {
            if (tab.mouseClicked(event, doubleClick)) 
                return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0) {
            if (this.focusedDraggable != null) {
                this.focusedDraggable.drag(dragX, dragY); 
                return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.selectedWidgetInfo != null && this.selectedWidgetInfo.isMouseOverWindow(this.offsetXInfo, this.offsetYInfo, mouseX, mouseY)) {
            this.focusedScrollable = this.selectedWidgetInfo;
        } else if (
            mouseX > this.offsetX 
            && mouseY > this.offsetYTree
            && mouseX < this.offsetX + this.width
            && mouseY < this.offsetYTree + WINDOW_HEIGHT
        ) {
            this.focusedScrollable = this.selectedTab;
        } else {
            this.focusedScrollable = null;
        }

        if (this.focusedScrollable != null) {
            this.focusedScrollable.scroll(scrollX,scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private double calcInverse(double yIntercept, double xIntercept, double smoothnessMult, double x, boolean pn) {
        double pn_mult = pn ? 1.0d : -1.0d; // positive or negative for √(sigma)
        double yIntercept_abs = Math.abs(yIntercept);
        float sigma = (float) (
            Math.pow(xIntercept,2)*Math.pow(yIntercept,2) 
            + 4*xIntercept*smoothnessMult*yIntercept_abs
        );
        float a = (float) (
            xIntercept/2 
            + pn_mult * (Math.sqrt(sigma)) / ((-2)*yIntercept_abs)
        );
        float b = (float) (
            yIntercept_abs/2 
            + pn_mult * (Math.sqrt(sigma)) / ((-2)*xIntercept)
        );
        return
            Math.signum(yIntercept) 
            * (smoothnessMult / (x - a) + b);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void addWidgetScreen(SkillTreeWidget widget, SkillTreeWidgetScreen screen) {
        this.widgetScreens.put(widget, screen);
    }
    protected void removeWidgetScreen(SkillTreeWidget widget) {
        this.widgetScreens.remove(widget);
    }

    protected void setSelectedNode(@Nullable SkillTreeNode node) {
        this.selectedNode = null;
        this.selectedWidgetInfo = null;
        if (node != null && this.tabs.containsKey(node.getCategoryId())) {
            this.selectedNode = node;
            SkillTreeWidget widget = this.tabs.get(node.getCategoryId()).getWidget(node);
            this.selectedWidgetInfo = widget == null ? null : this.createInfoScreen(widget);
        }
    }
    private SkillTreeInfoScreen createInfoScreen(SkillTreeWidget widget) {
        return SkillTreeInfoScreen.create(widget, this.font, this);
    }
    public SkillTreeNode getSelectedNode() {
        return this.selectedNode;
    }

    protected void setSelectedTab(@Nullable SkillTreeTab tab) {
        this.selectedTab = tab;
    }
    public @Nullable SkillTreeTab getSelectedTab() {
        return this.selectedTab;
    }

    /* SkillCategory.Listener method */
    @Override
    public void onAddRootSkillNode(Identifier categoryId, SkillTreeNode node) {
        this.tabs.get(categoryId).addWidget(node);
    }

    /* SkillCategory.Listener method */
    @Override
    public void onAddDependantSkillNode(Identifier categoryId, SkillTreeNode node) {
        this.tabs.get(categoryId).addWidget(node);
    }

    protected int dynamicInsideOffset() {
        return this.infoDynamicOffset;
    }
    protected int dynamicInsideWidth() {
        return this.infoWindowInsideDynamicWidth;
    }

    protected ClientSkillTree getSkillTree() {
        return this.skillTree;
    }

}
