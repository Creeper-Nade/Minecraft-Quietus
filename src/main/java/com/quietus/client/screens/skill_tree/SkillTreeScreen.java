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
    private int infoWindowDynamicWidth = WINDOW_WIDTH;
    private static final int WINDOW_INSIDE_X = 9;
    private int infoWindowInsideDynamicWidth = WINDOW_INSIDE_X;
    private static final int WINDOW_INSIDE_Y = 9;
    private static final int WINDOW_INSIDE_TOP_Y = 18;
    protected static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH-WINDOW_INSIDE_X*2;
    protected static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT-WINDOW_INSIDE_Y-WINDOW_INSIDE_TOP_Y;
    private static final int GAP_WINDOW_INFO = 7;

    protected static final int WIDGET_MARGIN_WIDTH = 6;
    protected static final int WIDGET_MARGIN_HEIGHT = 6;

    private static final int MAX_TABS_PER_PAGE = 6;

    private static final int INFO_DYNAMIC_OFFSET_FROM_CENTER = - (GAP_WINDOW_INFO + SkillTreeInfoScreen.WIDTH)/2;
    private int infoDynamicOffset = 0;
    private static final int DYNAMIC_POSITIONING_TICKS = 40;
    private int infoDynamicTicks = DYNAMIC_POSITIONING_TICKS;
    private int infoWindowY = 0;
    

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
        /* if (this.selectedTab != null && !this.tabs.values().stream().map(tab -> tab.getCategory()).collect(Collectors.toList()).contains(this.selectedTab.getCategory())) {
            LOGGER.info("The client has skill category {} selected but it exists no more!", this.selectedTab.getCategory().getId().toString());
            this.selectedTab = null;
        } */
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
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    /* @Override
    public void tick() {
    } */

    private void renderTick() {
        /* Animations */
        this.infoDynamicTicks = this.selectedNode == null ?
            Math.min(this.infoDynamicTicks+1, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.infoDynamicTicks-1, 0);
        this.infoWindowDynamicWidth = WINDOW_WIDTH + (int)Math.round((1.0d - (double)this.infoDynamicTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        this.infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + (int)Math.round((1.0d - (double)this.infoDynamicTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        
        this.infoDynamicOffset = calcInverse((double)INFO_DYNAMIC_OFFSET_FROM_CENTER,(double)DYNAMIC_POSITIONING_TICKS, 80.0d, this.infoDynamicTicks, this.selectedNode == null);

        this.infoWindowDynamicWidth = WINDOW_WIDTH + calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicks, this.selectedNode == null);
        this.infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicks, this.selectedNode == null);

        /* Offset calculation */
        int treeOffsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        int infoOffsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoWindowDynamicWidth + GAP_WINDOW_INFO + this.infoDynamicOffset;
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;


        /* Tabs tick */
        Iterator<SkillTreeTab> it = tabs.values().iterator();
        for (int i = 0; it.hasNext(); i++) { 
            SkillTreeTab tab = it.next();
            tab.visible = false;
            tab.active = !this.selectedTab.getCategory().equals(tab.getCategory());
            tab.setPosition(treeOffsetX-35, offsetY+12+28*i);
        }
        Iterator<SkillTreeTab> it2 = tabs.values().iterator();
        for (int i = 0; i < MAX_TABS_PER_PAGE && it2.hasNext(); i++) { // show only first MAX_TABS_PER_PAGE tabs (supported by this GUI)
            SkillTreeTab tab = it2.next();
            tab.visible = true;
            tab.setPosition(treeOffsetX-35, offsetY+12+28*i);
        }

        /* Info Screen tick */
        if (this.selectedWidgetInfo != null) {
            this.selectedWidgetInfo.update(this.skillTree);

            if (this.selectedTab.getPositioning().getVertices().containsKey(this.selectedNode)) { // only make dynamic info window y when there is positioning for the selected node from selected tab
                this.infoWindowY = offsetY + this.selectedTab.getPositioning().getVertices().get(this.selectedNode).y() + WINDOW_INSIDE_TOP_Y + (int)this.selectedTab.scrollY;
                this.infoWindowY += Math.min(0, (offsetY + WINDOW_HEIGHT) - (this.infoWindowY - selectedWidgetInfo.getTopHeight() + selectedWidgetInfo.getHeight())); // clamps the bottom if InfoScreen has lower bottom
                this.infoWindowY += Math.max(0, offsetY - (this.infoWindowY - selectedWidgetInfo.getTopHeight())); // clamps the top if InfoScreen has higher top
            }
        }
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta);

        this.renderTick();

        int treeOffsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        int infoOffsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoWindowDynamicWidth + GAP_WINDOW_INFO + this.infoDynamicOffset;
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;

        this.renderTreeWindow(guiGraphicsExtractor, mouseX, mouseY, delta, treeOffsetX, offsetY);

        guiGraphicsExtractor.nextStratum();

        this.tabs.values().forEach(tab -> tab.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta));

        guiGraphicsExtractor.nextStratum();

        if (this.selectedNode != null) {
            this.renderInfoWindow(guiGraphicsExtractor, mouseX, mouseY, delta, infoOffsetX, offsetY);
        }
    }

    private void renderTreeWindow(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float delta, int offsetX, int offsetY) {
        this.selectedTab.drawContents(GuiGraphicsExtractor, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, this.infoWindowInsideDynamicWidth, WINDOW_INSIDE_HEIGHT, mouseX, mouseY, delta);
        GuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_SPRITE_LOCATION, offsetX, offsetY, this.infoWindowDynamicWidth, WINDOW_HEIGHT);
    }

    private void renderInfoWindow(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float delta, int offsetX, int offsetY) {
        //LOGGER.info("[DDebug] selected tab: {}, selected widget: {}, widget info screen: {}", this.selectedTab.getCategory().getId().toString(), this.selectedNode.getId().toString(), this.selectedWidgetInfo);
        this.selectedWidgetInfo.draw(GuiGraphicsExtractor, mouseX, mouseY, offsetX, this.infoWindowY, delta, this.skillTree);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int offsetY = (this.height - WINDOW_HEIGHT) / 2;
        int treeOffsetX = WINDOW_INSIDE_X + (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        int treeOffsetY = WINDOW_INSIDE_TOP_Y + offsetY;
        int infoOffsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoWindowDynamicWidth + GAP_WINDOW_INFO + this.infoDynamicOffset;
        /* int infoOffsetY = offsetY;
        if (this.selectedTab != null && this.selectedNode != null) {
            infoOffsetY += this.selectedTab.getPositioning().getVertices().get(this.selectedNode).y() + WINDOW_INSIDE_TOP_Y + (int)this.selectedTab.scrollY;
            infoOffsetY += Math.min(0, (offsetY + WINDOW_HEIGHT) - (infoOffsetY - selectedWidgetInfo.getTopHeight() + selectedWidgetInfo.getHeight())); // snaps to the bottom if InfoScreen has lower bottom
            infoOffsetY += Math.max(0, offsetY - (infoOffsetY - selectedWidgetInfo.getTopHeight())); // snaps to the top if InfoScreen has higher top
        } */

        if (this.selectedWidgetInfo != null && this.selectedWidgetInfo.isMouseOverWindow(infoOffsetX, this.infoWindowY, mouseX, mouseY)) {
            this.focusedDraggable = this.selectedWidgetInfo;

            
            if (this.selectedWidgetInfo.mouseClicked(event, doubleClick)) {
                return true;
            }
        } else if (
            mouseX > treeOffsetX
            && mouseY > treeOffsetY
            && mouseX < treeOffsetX + this.width
            && mouseY < treeOffsetY + WINDOW_HEIGHT
        ) {
            this.focusedDraggable = this.selectedTab;
        } else {
            this.focusedDraggable = null;
        }

        if (this.selectedTab != null) {
            /* if (this.selectedTab.clickOnTree(treeOffsetX, treeOffsetY, mouseX, mouseY, event.button())) { */
            if (this.selectedTab.clickOnTree(event, doubleClick)) {
                return true;
            }
        }

        for (SkillTreeTab tab : this.tabs.values()) {
            if (tab.mouseClicked(event, doubleClick)) 
                return true;
        }

        /* for (SkillTreeTab tab : this.tabs.values()) {
            if (tab.clickOnTree(infoOffsetX, treeOffsetY, mouseX, mouseY, event.button())) {
                this.selectedTab = tab;
                return true;
            }
        } */

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
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;
        int treeOffsetX = WINDOW_INSIDE_X + (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        int treeOffsetY = WINDOW_INSIDE_TOP_Y + offsetY;
        int infoOffsetX =  + (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidth) / 2 + this.infoWindowDynamicWidth + GAP_WINDOW_INFO + this.infoDynamicOffset;
        /* int infoOffsetY = offsetY;
        if (this.selectedTab != null && this.selectedNode != null) {
            infoOffsetY += this.selectedTab.getPositioning().getVertices().get(this.selectedNode).y() + WINDOW_INSIDE_TOP_Y + (int)this.selectedTab.scrollY;
            infoOffsetY += Math.min(0, (offsetY + WINDOW_HEIGHT) - (infoOffsetY - selectedWidgetInfo.getTopHeight() + selectedWidgetInfo.getHeight())); // snaps to the bottom if InfoScreen has lower bottom
            infoOffsetY += Math.max(0, offsetY - (infoOffsetY - selectedWidgetInfo.getTopHeight())); // snaps to the top if InfoScreen has higher top
        } */

        if (this.selectedWidgetInfo != null && this.selectedWidgetInfo.isMouseOverWindow(infoOffsetX, this.infoWindowY, mouseX, mouseY)) {
            this.focusedScrollable = this.selectedWidgetInfo;
        } else if (
            mouseX > treeOffsetX 
            && mouseY > treeOffsetY
            && mouseX < treeOffsetX + this.width
            && mouseY < treeOffsetY + WINDOW_HEIGHT
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

    private int calcInverse(double yIntercept, double xIntercept, double smoothnessMult, double x, boolean pn) {
        //final double pn_mult = this.selectedWidget == null ? 1.0d : -1.0d; // positive or negative for √(sigma)
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
        return (int)Math.round(
            Math.signum(yIntercept) 
            * (smoothnessMult / (x - a) + b)
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void addWidgetScreen(SkillTreeWidget widget, SkillTreeWidgetScreen screen) {
        this.widgetScreens.put(widget, screen);
    }
    protected void removeWidgetScreen(SkillTreeWidget widget) {
        /* if (this.focusedScrollable.equals(this.widgetScreens.get(widget)))
            this.focusedScrollable = this.selectedTab; */
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
