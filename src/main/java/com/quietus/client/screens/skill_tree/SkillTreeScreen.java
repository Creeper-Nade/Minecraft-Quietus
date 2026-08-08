package com.quietus.client.screens.skill_tree;

import static com.quietus.Quietus.MODID;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.quietus.util.MapUtil;
import com.quietus.util.ServerPacketDistributor;
import com.quietus.util.layouts.VerticalEvenGridLayout;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.quietus.Quietus;
import com.quietus.client.QuietusKeyBindings;
import com.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.client.screens.skill_tree.SkillTreeTab.TabSelectionElement;
import com.quietus.skilltree.SkillTreeNode;
import com.quietus.skilltree.TreePosition;
import com.ibm.icu.impl.locale.KeyTypeData.ValueType;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.logging.LogUtils;
import com.quietus.skilltree.SkillCategory;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SkillTreeScreen extends Screen implements SkillCategory.Listener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Identifier WINDOW_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/window");
    private static final Identifier WINDOW_LORE_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/window_lore");
    private static final Identifier WINDOW_LORE_GRAYSCALE_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/window_lore_grayscale");
    private static final Identifier WINDOW_LORE_GRAYSCALE_GLOW_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/window_lore_grayscale_glow");
    private static final Identifier WINDOW_LORECHAR_LOCATION = Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/imchar_skilltree.png");

    private static final int WINDOW_LORE_COLOUR = 0xFF343434;

    public static final int WINDOW_WIDTH = 248;
    public static final int WINDOW_HEIGHT = 186;
    public static final int WINDOW_WIDTH_INFO_CHANGE = -84;
    private static final int WINDOW_TITLE_X = 18;
    private static final int WINDOW_TITLE_Y = 6;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 9;
    private static final int WINDOW_INSIDE_TOP_Y = 18;
    protected static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH-WINDOW_INSIDE_X*2;
    protected static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT-WINDOW_INSIDE_Y-WINDOW_INSIDE_TOP_Y;
    private static final int WINDOW_LORECHAR_X = 3; // x from sides of the screen that lore characters should not render in
    private static final int WINDOW_LORECHAR_TOP_Y = 23; // y from top of the screen that lore characters should not render in
    private static final int WINDOW_LORECHAR_BOTTOM_Y = 13; // y from bottom of the screen that lore characters should not render in
    private static final int WINDOW_LORECHAR_GAP = 1;
    private static final int WINDOW_LORECHAR_VERTICAL_AMOUNT = Math.floorDiv((WINDOW_HEIGHT - WINDOW_LORECHAR_TOP_Y - WINDOW_LORECHAR_BOTTOM_Y), (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP)); // amount of lore chars that will be rendered, given the bounds
    private static final int GAP_WINDOW_INFO = 7;
    
    protected static final int WIDGET_MARGIN_WIDTH = 6;
    protected static final int WIDGET_MARGIN_HEIGHT = 9;
    
    private static final int MAX_TABS_PER_PAGE = 6;
    private static final int TABS_SELECTION_COLUMNS = 3;
    private static final double TABS_SELECTION_DESIRED_ROWS_PER_PAGE = 3.4;
    
    private static final int INFO_DYNAMIC_OFFSET_FROM_CENTER = - (GAP_WINDOW_INFO + SkillTreeInfoScreen.WIDTH)/2;
    private static final int DYNAMIC_POSITIONING_TICKS = 40;
    private static final int DYNAMIC_LORE_COLOUR_TICKS = 20;
    
    private static final double TAB_DYNAMIC_HIDE_OFFSET = SkillTreeTab.TAB_DISPLAY_WIDTH - 3;
    private static final double GRID_CENTER_OFFSET = - (SkillTreeTab.TAB_DISPLAY_WIDTH - 3) / 2.0;

    private int infoAnimTicks = DYNAMIC_POSITIONING_TICKS;
    private int gridAnimTicks = DYNAMIC_POSITIONING_TICKS;
    private int windowDynamicWidth = WINDOW_WIDTH;
    private int windowInsideDynamicWidth = WINDOW_INSIDE_WIDTH;
    private int windowDynamicOffset = 0;
    private int tabDynamicOffset = 0;
    private int gridCenterOffset = 0;

    private float infoDynamicTicksF = DYNAMIC_POSITIONING_TICKS;
    private float gridDynamicTicksF = DYNAMIC_POSITIONING_TICKS;
    private float infoWindowDynamicWidthF = WINDOW_WIDTH;
    private float infoWindowInsideDynamicWidthF = WINDOW_INSIDE_WIDTH;
    private float infoDynamicOffsetF = 0.0f;
    private float tabDynamicOffsetF = 0.0f;
    private float gridCenterOffsetF = 0.0f;

    private int loreColour = 0x00FFFFFF;
    private int loreColourTransitionTicks = DYNAMIC_LORE_COLOUR_TICKS;
    private int loreOpacityTransitionTicks = DYNAMIC_LORE_COLOUR_TICKS;
    private int lastLoreColour = 0xFFFFFFFF;
    private float lastLoreOpacity = 0.0f;
    private int targetLoreColour = 0xFFFFFFFF;
    private float targetLoreOpacity = 0.0f;

    private int offsetX, offsetY, offsetXTree, offsetYTree, offsetXInfo, offsetYInfo = 0;
    private float offsetXFTree, offsetXFInfo = 0.0f;
    

    private static final Component TITLE = Component.translatable("gui.skill_tree");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private SkillTreeScreen.TabsSelectionGridLayout tabsGridLayout = null;
    private final AbstractWidget tabsGridButton = new AbstractWidget(0, 0, 23, 23, Component.translatable("gui.skill_tree.button.moreTabs")) {
        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
            if (this.isHovered()) {
                gui.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/more_tabs_button_hovered.png"), this.getX(), this.getY(), 0.0f, 0.0f, 23, 23, 23, 23);
            } else {
                gui.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/more_tabs_button.png"), this.getX(), this.getY(), 0.0f, 0.0f, 23, 23, 23, 23);
            }
        }
        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if (SkillTreeScreen.this.tabsGridLayout == null) {
                SkillTreeScreen.this.openTabsSelectionGrid();
            } else {
                SkillTreeScreen.this.closeTabsSelectionGrid();
            }
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput arg0) {
            // TODO Auto-generated method stub
        }
        
    };

    private final ClientSkillTree skillTree;
    private final LinkedHashMap<Identifier,SkillTreeTab> tabs = new LinkedHashMap<>();
    
    private final Map<SkillTreeWidget,SkillTreeWidgetScreen> widgetScreens = new LinkedHashMap<>();
    
    private SkillTreeDraggable focusedDraggable = null;
    private SkillTreeScrollable focusedScrollable = null;
    private SkillTreeTab selectedTab = null;
    @Nullable private SkillTreeNode selectedNode;
    @Nullable private SkillTreeInfoScreen selectedWidgetInfo;


    public SkillTreeScreen(ClientSkillTree skillTree) {
        super(TITLE);

        this.skillTree = skillTree;
    }

    /* @Override
    public void tick() {
    }  */

    @Override
    public void onClose() {
        this.saveData();
        super.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (QuietusKeyBindings.SKILL_TREE_KEY.get().matches(event)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            this.onClose();
            return true;
        } else if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
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
            SkillTreeTab createdtab = SkillTreeTab.create(this.minecraft, this.font, this.skillTree, this, category, positioning);
            if (!Objects.isNull(createdtab)) {
                this.tabs.put(id, createdtab);
            }
            category.setListener(this); // adds widgets to the tab via SkillCategory's listener
        });
        if (!this.tabs.isEmpty()) {
            if (this.selectedTab == null) {
                this.setInitialSelectedTab(this.tabs.values().iterator().next());
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
        this.applyData(); // apply client-remembered tabs order data and scroll pos of each tab
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

    private void renderTick(float delta) {
        /* Animations */
        this.infoAnimTicks = this.selectedNode == null ?
            Math.min(this.infoAnimTicks+1, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.infoAnimTicks-1, 0);
        // int
        this.windowDynamicWidth = WINDOW_WIDTH + (int)Math.round((1.0d - (double)this.infoAnimTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        this.windowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + (int)Math.round((1.0d - (double)this.infoAnimTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        
        this.windowDynamicOffset = (int)Math.round(calcReciprocal((double)INFO_DYNAMIC_OFFSET_FROM_CENTER,(double)DYNAMIC_POSITIONING_TICKS, 100.0d, this.infoAnimTicks, this.selectedNode == null));

        this.windowDynamicWidth = WINDOW_WIDTH + (int)Math.round(calcReciprocal((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 40.0d, this.infoAnimTicks, this.selectedNode == null));
        this.windowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + (int)Math.round(calcReciprocal((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 40.0d, this.infoAnimTicks, this.selectedNode == null));
        
        this.gridAnimTicks = this.tabsGridLayout == null ?
            Math.min(this.gridAnimTicks + 1, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.gridAnimTicks - 1, 0);

        this.tabDynamicOffset = (int)Math.round(calcReciprocal(TAB_DYNAMIC_HIDE_OFFSET, (double)DYNAMIC_POSITIONING_TICKS, 100.0d, this.gridAnimTicks, this.tabsGridLayout == null));
        this.gridCenterOffset = (int)Math.round(calcReciprocal(GRID_CENTER_OFFSET, (double)DYNAMIC_POSITIONING_TICKS, 30.0d, this.gridAnimTicks, this.tabsGridLayout == null));

        // float - for smoother offset animation
        this.infoDynamicTicksF = this.selectedNode == null ?
            Math.min(this.infoAnimTicks-1+delta, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.infoAnimTicks+1-delta, 0);
        this.infoWindowDynamicWidthF = WINDOW_WIDTH + (float)((1.0d - this.infoDynamicTicksF / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        this.infoWindowInsideDynamicWidthF = WINDOW_INSIDE_WIDTH + (float)((1.0d - this.infoDynamicTicksF / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        
        this.infoDynamicOffsetF = (float)calcReciprocal((double)INFO_DYNAMIC_OFFSET_FROM_CENTER,(double)DYNAMIC_POSITIONING_TICKS, 100.0d, this.infoDynamicTicksF, this.selectedNode == null);

        this.infoWindowDynamicWidthF = WINDOW_WIDTH + (float)calcReciprocal((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 40.0d, this.infoDynamicTicksF, this.selectedNode == null);
        this.infoWindowInsideDynamicWidthF = WINDOW_INSIDE_WIDTH + (float)calcReciprocal((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 40.0d, this.infoDynamicTicksF, this.selectedNode == null);

        this.gridDynamicTicksF = this.tabsGridLayout == null ?
            Math.min(this.gridAnimTicks - 1 + delta, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.gridAnimTicks + 1 - delta, 0);

        this.tabDynamicOffsetF = (float)calcReciprocal(TAB_DYNAMIC_HIDE_OFFSET, (double)DYNAMIC_POSITIONING_TICKS, 100.0d, this.gridDynamicTicksF, this.tabsGridLayout == null);
        this.gridCenterOffsetF = (float)calcReciprocal(GRID_CENTER_OFFSET, (double)DYNAMIC_POSITIONING_TICKS, 30.0d, this.gridDynamicTicksF, this.tabsGridLayout == null);

        /* Offset calculation */
        this.offsetX = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.windowDynamicWidth) / 2 + this.windowDynamicOffset + this.gridCenterOffset;
        this.offsetY = (this.height - WINDOW_HEIGHT) / 2;
        this.offsetXTree = this.offsetX + WINDOW_INSIDE_X;
        this.offsetYTree = this.offsetY + WINDOW_INSIDE_TOP_Y;
        this.offsetXInfo = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.windowDynamicWidth) / 2 + this.windowDynamicWidth + GAP_WINDOW_INFO + this.windowDynamicOffset + this.gridCenterOffset;
        this.offsetXFTree = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidthF) / 2 + this.infoDynamicOffsetF + this.gridCenterOffsetF;
        this.offsetXFInfo = (this.width + SkillTreeTab.TAB_DISPLAY_WIDTH - this.infoWindowDynamicWidthF) / 2 + this.infoWindowDynamicWidthF + GAP_WINDOW_INFO + this.infoDynamicOffsetF + this.gridCenterOffsetF;


        /* Tabs selection button and layout */
        this.tabsGridButton.setPosition(this.offsetX-SkillTreeTab.TAB_DISPLAY_WIDTH+3+4, this.offsetY+12+SkillTreeTab.TAB_DISPLAY_HEIGHT*MAX_TABS_PER_PAGE+2);
        if (this.tabsGridLayout != null) {
            this.tabsGridLayout.setInitialPosition(this.offsetXTree, this.offsetYTree);
            this.tabsGridLayout.setViewportHeight(WINDOW_INSIDE_HEIGHT);
            this.tabsGridLayout.setWidth(this.windowInsideDynamicWidth);
            this.tabsGridLayout.arrangeElements();
        }

        /* Tabs tick */
        Iterator<SkillTreeTab> it = tabs.values().iterator();
        for (int i = 0; it.hasNext(); i++) { 
            SkillTreeTab tab = it.next();
            tab.visible = false;
            tab.active = (this.tabsGridLayout != null) ? false : (this.selectedTab == null ? true : !this.selectedTab.getCategory().equals(tab.getCategory()));
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
                this.offsetYInfo = offsetY + this.selectedTab.getPositioning().getVertices().get(this.selectedNode).y() + WINDOW_INSIDE_TOP_Y + (int)this.selectedTab.treeScrollY;
                this.offsetYInfo += Math.min(0, (offsetY + WINDOW_HEIGHT) - (this.offsetYInfo - selectedWidgetInfo.getTopHeight() + selectedWidgetInfo.getHeight())); // clamps the bottom if InfoScreen has lower bottom
                this.offsetYInfo += Math.max(0, offsetY - (this.offsetYInfo - selectedWidgetInfo.getTopHeight())); // clamps the top if InfoScreen has higher top
            }
        }

        /* Lore colour */
        float linearColourT = Math.max(0.0f, Math.min(1.0f, (float) this.loreColourTransitionTicks / DYNAMIC_LORE_COLOUR_TICKS));
        float smoothColourT = linearColourT * linearColourT * (3.0f - 2.0f*linearColourT);
        this.loreColour = ARGB.linearLerp(smoothColourT, this.lastLoreColour, this.targetLoreColour);
        float linearOpacityT = Math.max(0.0f, Math.min(1.0f, (float) this.loreOpacityTransitionTicks / DYNAMIC_LORE_COLOUR_TICKS));
        float smoothOpacityT = linearOpacityT * linearOpacityT * (3.0f - 2.0f*linearOpacityT);
        this.loreColour = ARGB.color((float)Mth.lerp(smoothOpacityT, this.lastLoreOpacity, this.targetLoreOpacity), this.loreColour);

        this.loreColourTransitionTicks += 1;
        this.loreOpacityTransitionTicks += 1;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        super.extractRenderState(gui, mouseX, mouseY, delta);

        this.renderTick(delta);

        // Render tabsGridButton
        gui.pose().pushMatrix();
        gui.pose().translate(this.offsetXFTree - this.offsetX/*  + this.tabDynamicOffsetF */, 0.0f);
        this.tabsGridButton.extractRenderState(gui, mouseX, mouseY, delta);
        gui.pose().popMatrix();

        // Render tab buttons with scissor from top-left of top tab to bottom-right of bottom tab
        int numTabs = 0;
        for (SkillTreeTab tab : this.tabs.values()) {
            if (tab.visible) numTabs++;
        }
        if (numTabs > 0) {
            int scissorMinX = (int)Math.round(this.offsetXFTree - SkillTreeTab.TAB_DISPLAY_WIDTH + 3);
            int scissorMinY = this.offsetY + 12;
            int scissorMaxX = (int)Math.round(this.offsetXFTree + 3);
            int scissorMaxY = this.offsetY + 12 + SkillTreeTab.TAB_DISPLAY_HEIGHT * numTabs;

            gui.enableScissor(scissorMinX, scissorMinY, scissorMaxX, scissorMaxY);
            gui.pose().pushMatrix();
            gui.pose().translate(this.offsetXFTree - this.offsetX + this.tabDynamicOffsetF, 0.0f);
            this.tabs.values().forEach(tab -> tab.extractRenderState(gui, mouseX, mouseY, delta));
            gui.pose().popMatrix();
            gui.disableScissor();
        }

        gui.nextStratum();

        // Render main tree window on top of unselected tabs
        gui.pose().pushMatrix();
        gui.pose().translate(this.offsetXFTree, 0.0f);
        this.renderTreeWindow(gui, mouseX, mouseY, delta, 0, this.offsetY);
        gui.pose().popMatrix();

        // Render selected tab again after the animation finishes, on top of main window if grid is not open
        if (this.tabsGridLayout == null && this.selectedTab != null && this.tabDynamicOffset == 0) {
            gui.nextStratum();
            gui.pose().pushMatrix();
            gui.pose().translate(this.offsetXFTree - this.offsetX + this.tabDynamicOffsetF, 0.0f);
            this.selectedTab.extractRenderState(gui, mouseX, mouseY, delta);
            gui.pose().popMatrix();
        }

        // Render info window if a node is selected
        if (this.selectedNode != null) {
            gui.nextStratum();
            gui.pose().pushMatrix();
            gui.pose().translate(this.offsetXFInfo, 0.0f);
            this.renderInfoWindow(gui, mouseX, mouseY, delta, 0, this.offsetYInfo);
            gui.pose().popMatrix();
        }
    }

    private void renderTreeWindow(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta, int offsetX, int offsetY) {
        /* render the tab's skill tree or tabs selection grid */
        if (this.tabsGridLayout != null) { // tabs selection grid
            gui.enableScissor(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, offsetX + WINDOW_INSIDE_X + this.windowInsideDynamicWidth, offsetY + WINDOW_INSIDE_TOP_Y + WINDOW_INSIDE_HEIGHT);
            gui.pose().translate(-this.offsetX, 0.0f);
            this.tabsGridLayout.visitWidgets(element -> element.extractRenderState(gui, mouseX, mouseY, delta));
            gui.disableScissor();
            gui.pose().translate(+this.offsetX, 0.0f);
            if (this.tabsGridLayout.equals(this.focusedDraggable) && this.isDragging() && this.tabsGridLayout.isScrolling()) {
                gui.requestCursor(CursorTypes.RESIZE_NS);
            }
        } else { // skill tree
            if (this.selectedTab != null) {
                this.selectedTab.drawTreeBackground(gui, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, this.windowInsideDynamicWidth, WINDOW_INSIDE_HEIGHT, mouseX, mouseY, delta);
                gui.enableScissor(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, offsetX + WINDOW_INSIDE_X + this.windowInsideDynamicWidth, offsetY + WINDOW_INSIDE_TOP_Y + WINDOW_INSIDE_HEIGHT);
                gui.pose().translate(-this.offsetX, 0.0f);
                this.selectedTab.drawTreeWidgetsAndEdges(gui, mouseX, mouseY, delta);
                gui.disableScissor();
                gui.pose().translate(+this.offsetX, 0.0f);
            }
        }

        /* render the window frame */
        // base frame
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_SPRITE_LOCATION, offsetX, offsetY, this.windowDynamicWidth, WINDOW_HEIGHT);
        // title 
        if (this.selectedTab != null && this.tabsGridLayout == null) {
            gui.text(this.font, this.selectedTab.getName(), offsetX + WINDOW_TITLE_X, offsetY + WINDOW_TITLE_Y, this.loreColour);
        }
        RandomSource lorecharRandom;
        // base lore
        UUID uuid = this.minecraft.player.getUUID();
        lorecharRandom = RandomSource.create(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_LORE_GRAYSCALE_SPRITE_LOCATION, offsetX, offsetY, this.windowDynamicWidth, WINDOW_HEIGHT, WINDOW_LORE_COLOUR);
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_LORE_GRAYSCALE_SPRITE_LOCATION, offsetX, offsetY, this.windowDynamicWidth, WINDOW_HEIGHT, this.loreColour);
        for (int i = 0; i < WINDOW_LORECHAR_VERTICAL_AMOUNT; i++) { // the left column of characters
            gui.blit(
              RenderPipelines.GUI_TEXTURED, 
              Quietus.IMCHAR_GUI_GRAYSCLALE_LOCATION, 
              offsetX + WINDOW_LORECHAR_X, 
              offsetY + WINDOW_LORECHAR_TOP_Y + i * (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP), 
              lorecharRandom.nextInt(0, Quietus.IMCHAR_AMOUNT-1)*Quietus.IMCHAR_SPRITE_WIDTH, 
              0.0f, 
              Quietus.IMCHAR_SPRITE_WIDTH, Quietus.IMCHAR_SPRITE_HEIGHT, Quietus.IMCHAR_RESOURCE_WIDTH, Quietus.IMCHAR_RESOURCE_HEIGHT, 
              WINDOW_LORE_COLOUR
            );
        }
        for (int i = 0; i < WINDOW_LORECHAR_VERTICAL_AMOUNT; i++) { // the right column of characters
            gui.blit(
              RenderPipelines.GUI_TEXTURED, 
              Quietus.IMCHAR_GUI_GRAYSCLALE_LOCATION, 
              offsetX + this.windowDynamicWidth - WINDOW_LORECHAR_X - Quietus.IMCHAR_SPRITE_WIDTH, 
              offsetY + WINDOW_LORECHAR_TOP_Y + i * (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP), 
              lorecharRandom.nextInt(0, Quietus.IMCHAR_AMOUNT-1)*Quietus.IMCHAR_SPRITE_WIDTH, 
              0.0f, 
              Quietus.IMCHAR_SPRITE_WIDTH, Quietus.IMCHAR_SPRITE_HEIGHT, Quietus.IMCHAR_RESOURCE_WIDTH, Quietus.IMCHAR_RESOURCE_HEIGHT, 
              WINDOW_LORE_COLOUR
            );
        }
        // coloured lore
        if (ARGB.alpha(this.loreColour) > 0) {
            lorecharRandom = RandomSource.create(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_LORE_GRAYSCALE_SPRITE_LOCATION, offsetX, offsetY, this.windowDynamicWidth, WINDOW_HEIGHT, this.loreColour);
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_LORE_GRAYSCALE_GLOW_SPRITE_LOCATION, offsetX, offsetY, this.windowDynamicWidth, WINDOW_HEIGHT, this.loreColour);
            for (int i = 0; i < WINDOW_LORECHAR_VERTICAL_AMOUNT; i++) { // the left column of characters
                int letterIndex = lorecharRandom.nextInt(0, Quietus.IMCHAR_AMOUNT-1);
                gui.blit(
                  RenderPipelines.GUI_TEXTURED, 
                  Quietus.IMCHAR_GUI_GRAYSCLALE_GLOW_LOCATION, 
                  offsetX + WINDOW_LORECHAR_X, 
                  offsetY + WINDOW_LORECHAR_TOP_Y + i * (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP), 
                  letterIndex*Quietus.IMCHAR_SPRITE_WIDTH, 
                  0.0f, 
                  Quietus.IMCHAR_SPRITE_WIDTH, Quietus.IMCHAR_SPRITE_HEIGHT, Quietus.IMCHAR_RESOURCE_WIDTH, Quietus.IMCHAR_RESOURCE_HEIGHT, 
                  this.loreColour
                );
                gui.blit(
                  RenderPipelines.GUI_TEXTURED, 
                  Quietus.IMCHAR_GUI_GRAYSCLALE_LOCATION, 
                  offsetX + WINDOW_LORECHAR_X, 
                  offsetY + WINDOW_LORECHAR_TOP_Y + i * (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP), 
                  letterIndex*Quietus.IMCHAR_SPRITE_WIDTH, 
                  0.0f, 
                  Quietus.IMCHAR_SPRITE_WIDTH, Quietus.IMCHAR_SPRITE_HEIGHT, Quietus.IMCHAR_RESOURCE_WIDTH, Quietus.IMCHAR_RESOURCE_HEIGHT, 
                  this.loreColour
                );
            }
            for (int i = 0; i < WINDOW_LORECHAR_VERTICAL_AMOUNT; i++) { // the right column of characters
                int letterIndex = lorecharRandom.nextInt(0, Quietus.IMCHAR_AMOUNT-1);
                gui.blit(
                  RenderPipelines.GUI_TEXTURED, 
                  Quietus.IMCHAR_GUI_GRAYSCLALE_GLOW_LOCATION, 
                  offsetX + this.windowDynamicWidth - WINDOW_LORECHAR_X - Quietus.IMCHAR_SPRITE_WIDTH, 
                  offsetY + WINDOW_LORECHAR_TOP_Y + i * (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP), 
                  letterIndex*Quietus.IMCHAR_SPRITE_WIDTH, 
                  0.0f, 
                  Quietus.IMCHAR_SPRITE_WIDTH, Quietus.IMCHAR_SPRITE_HEIGHT, Quietus.IMCHAR_RESOURCE_WIDTH, Quietus.IMCHAR_RESOURCE_HEIGHT, 
                  this.loreColour
                );
                gui.blit(
                  RenderPipelines.GUI_TEXTURED, 
                  Quietus.IMCHAR_GUI_GRAYSCLALE_LOCATION, 
                  offsetX + this.windowDynamicWidth - WINDOW_LORECHAR_X - Quietus.IMCHAR_SPRITE_WIDTH, 
                  offsetY + WINDOW_LORECHAR_TOP_Y + i * (Quietus.IMCHAR_HEIGHT+WINDOW_LORECHAR_GAP), 
                  letterIndex*Quietus.IMCHAR_SPRITE_WIDTH, 
                  0.0f, 
                  Quietus.IMCHAR_SPRITE_WIDTH, Quietus.IMCHAR_SPRITE_HEIGHT, Quietus.IMCHAR_RESOURCE_WIDTH, Quietus.IMCHAR_RESOURCE_HEIGHT, 
                  this.loreColour
                );
            }
        }
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

        if (this.tabsGridButton.mouseClicked(event, doubleClick)) {
            return true;
        }

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
            if (this.tabsGridLayout == null) {
                if (this.selectedTab != null) {
                    this.focusedDraggable = this.selectedTab;
                } else {
                    this.focusedDraggable = null;
                }
            } else {
                this.focusedDraggable = this.tabsGridLayout;
            }
        } else {
            this.focusedDraggable = null;
        }

        if (this.tabsGridLayout == null) {
            if (this.selectedTab != null) {
                if (this.selectedTab.clickOnTree(event, doubleClick)) {
                    return true;
                }
            }
        } else {
            this.tabsGridLayout.visitWidgets((widget) -> widget.mouseClicked(event, doubleClick));
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
                this.setDragging(true);
                return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.tabsGridLayout != null) {
            this.tabsGridLayout.visitWidgets((widget) -> widget.mouseReleased(event));
        }

        this.setDragging(false);
        return super.mouseReleased(event);
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
            if (this.tabsGridLayout == null) {
                if (this.selectedTab != null) {
                    this.focusedScrollable = this.selectedTab;
                } else {
                    this.focusedScrollable = null;
                }
            } else {
                this.focusedScrollable = this.tabsGridLayout;
            }
        } else {
            this.focusedScrollable = null;
        }

        if (this.focusedScrollable != null) {
            this.focusedScrollable.scroll(scrollX,scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * 
     * @param yIntercept
     * @param xIntercept
     * @param smoothnessMult the greater, the more linear is the animation. 
     *          When smoothnessMult appraoches infinity, the segment would
     *          be linear.
     * @param x
     * @param pn
     * @return
     */
    private double calcReciprocal(double yIntercept, double xIntercept, double smoothnessMult, double x, boolean pn) {
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

    // stop game pausing (including in singleplayer) when opening skill tree GUI
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class TabsSelectionGridLayout extends VerticalEvenGridLayout implements SkillTreeDraggable, SkillTreeScrollable {
        private int viewportHeight;
        
        private int initialY;
        private double scrollY;

        public TabsSelectionGridLayout(int x, int y, int width, int columns, int rowHeight, int viewportHeight) {
            this(x, y, width, columns, rowHeight, viewportHeight, 0.0d);
        }
        
        public TabsSelectionGridLayout(int x, int y, int width, int columns, int rowHeight, int viewportHeight, double scrollY) {
            super(x, y, width, columns, rowHeight);
            
            this.viewportHeight = viewportHeight;

            this.initialY = y;
            if (viewportHeight < this.height) {
                this.scrollY = scrollY;
                this.scrollY = Math.clamp(this.scrollY, -this.height + this.viewportHeight, 0);
            } else {
                this.scrollY = 0;
            }
        }

        public void setInitialPosition(int x, int y) {
            this.setX(x);
            this.initialY = y;
            this.setY((int)Math.round(this.initialY + this.scrollY));
        }

        public void setViewportHeight(int height) {
            this.viewportHeight = height;
        }

        @Override
        public void scroll(double scrollX, double scrollY) {
            if (scrollX != 0d && scrollY != 0d) {
                this.visitWidgets((widget) -> {
                    if (widget instanceof TabSelectionElement wid) {
                        wid.clicked = false;
                    }
                });
            }
            if (this.isScrolling()) {
                this.scrollY += scrollY*5;
                this.scrollY = Math.clamp(this.scrollY, -this.height + this.viewportHeight, 0);
            } 
            this.setY((int)Math.round(this.initialY + this.scrollY));
            this.arrangeElements();
        }

        @Override
        public void drag(double dragX, double dragY) {
            if (dragX != 0d && dragY != 0d) {
                this.visitWidgets((widget) -> {
                    if (widget instanceof TabSelectionElement wid) {
                        wid.clicked = false;
                    }
                });
            }
            if (this.isScrolling()) {
                this.scrollY += dragY;
                this.scrollY = Math.clamp(this.scrollY, -this.height + this.viewportHeight, 0);
            }
            this.setY((int)Math.round(this.initialY + this.scrollY));
            this.arrangeElements();
        }

        public boolean isScrolling() {
            return this.viewportHeight < this.height;
        }

    }

    protected void openTabsSelectionGrid() {
        SkillTreeScreen.TabsSelectionGridLayout createdLayout =  new SkillTreeScreen.TabsSelectionGridLayout(0, 0, WINDOW_INSIDE_WIDTH, TABS_SELECTION_COLUMNS, (int)Math.floor(WINDOW_INSIDE_HEIGHT / TABS_SELECTION_DESIRED_ROWS_PER_PAGE), WINDOW_INSIDE_HEIGHT);
        for (Entry<Identifier,SkillTreeTab> entry : SkillTreeScreen.this.tabs.entrySet()) {
            SkillTreeTab tab = entry.getValue();
            createdLayout.addChild(tab.createTabSelectionElement(0, 0, 2, 2));
              /* parameters in createdTabSelectionElement here don't matter, 
               * as TabsSelectionGridLayout will automatically fill its children width and height. */
        }
        this.tabsGridLayout = createdLayout;
        this.setSelectedNode(null);
        this.changeLoreOpacity(0.0f);
    }
    protected void closeTabsSelectionGrid() {
        this.setSelectedTab(this.selectedTab.getId());
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

    protected void saveData() {
        for (Entry<Identifier,SkillTreeTab> entry : this.tabs.entrySet()) {
            ClientSkillTreePayloadHandler.putScrollDataEntry(entry.getKey(), entry.getValue().makeScrollData());
        }
        ClientSkillTreePayloadHandler.putTabsOrderAndSelected(
            this.tabs.entrySet().stream().map(entry -> entry.getKey()).collect(Collectors.toList()), 
            this.selectedTab.getCategory().getId()
        );
    }
    protected void applyData() {
        /* tabs scroll */
        for (Entry<Identifier,SkillTreeTab.TabScrollData> entry : ClientSkillTreePayloadHandler.getScrollData().entrySet()) {
            SkillTreeTab tab = this.tabs.get(entry.getKey());
            if (Objects.nonNull(tab)) {
                tab.applyScrollData(entry.getValue());
            }
        }

        /* reorder tabs with given order (unmentioned tabs are placed at the end) */
        Map<Identifier, SkillTreeTab> reorderedMap = new LinkedHashMap<>();
        for (Identifier key : ClientSkillTreePayloadHandler.getTabsOrder()) {
            if (this.tabs.containsKey(key)) {
                reorderedMap.put(key, this.tabs.get(key));
            }
        }
        if (reorderedMap.size() < this.tabs.size()) {
            for (Map.Entry<Identifier, SkillTreeTab> entry : this.tabs.entrySet()) {
                if (!reorderedMap.containsKey(entry.getKey())) {
                    reorderedMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.tabs.clear();
        this.tabs.putAll(reorderedMap);

        /* set selected tab */
        SkillTreeTab newSelected;
        if (ClientSkillTreePayloadHandler.getTabSelected() != null) {
            newSelected = this.tabs.get(ClientSkillTreePayloadHandler.getTabSelected());
        } else {
            newSelected = reorderedMap.entrySet().iterator().next().getValue();
        }
        this.setInitialSelectedTab(newSelected);
    }

    protected void setInitialSelectedTab(@Nullable SkillTreeTab tab) {
        if (tab != null) {
            this.lastLoreColour = tab.getThemeColour();
            this.targetLoreColour = tab.getThemeColour();
            this.lastLoreOpacity = 0.0f;
            this.targetLoreOpacity = 1.0f;
            this.loreOpacityTransitionTicks = 0;
        }
        this.selectedTab = tab;
    }
    protected void setSelectedTab(@Nullable Identifier tabId) {
        this.tabsGridLayout = null;
        SkillTreeTab tab = this.tabs.get(tabId);
        if (tab != null) {
            this.changeLoreColour(tab.getThemeColour());
            this.changeLoreOpacity(1.0f);
        } else {
            this.changeLoreOpacity(0.0f);
        }
        this.selectedTab = tab;
    }
    protected void setSelectedTabAndTop(@Nullable Identifier tabId) {
        MapUtil.moveEntryToFirst(this.tabs, tabId);
        this.setSelectedTab(tabId);
    }
    public @Nullable SkillTreeTab getSelectedTab() {
        return this.selectedTab;
    }
    

    private void changeLoreColour(int colour) {
        this.loreColourTransitionTicks = 0;
        this.lastLoreColour = this.targetLoreColour;
        this.targetLoreColour = colour;
    }
    private void changeLoreOpacity(float opacity) {
        this.loreOpacityTransitionTicks = 0;
        this.lastLoreOpacity = this.targetLoreOpacity;
        this.targetLoreOpacity = opacity;
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

    protected SkillTreeDraggable focusedDraggable() {
        return this.focusedDraggable;
    }
    

    protected int dynamicInsideOffset() {
        return this.windowDynamicOffset;
    }
    protected int dynamicInsideWidth() {
        return this.windowInsideDynamicWidth;
    }
    protected int yOffset() {
        return this.offsetY;
    }

    protected ClientSkillTree getSkillTree() {
        return this.skillTree;
    }

}
