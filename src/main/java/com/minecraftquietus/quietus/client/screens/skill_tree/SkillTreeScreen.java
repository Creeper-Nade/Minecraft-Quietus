package com.minecraftquietus.quietus.client.screens.skill_tree;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minecraftquietus.quietus.skilltree.SkillPoint;
import com.minecraftquietus.quietus.util.ServerPacketDistributor;
import org.slf4j.Logger;

import com.minecraftquietus.quietus.client.QuietusKeyBindings;
import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.mojang.logging.LogUtils;
import com.minecraftquietus.quietus.skilltree.ConnectivityPosition;
import com.minecraftquietus.quietus.skilltree.SkillCategory;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeScreen extends Screen implements SkillCategory.Listener {
    private static final Logger LOGGER = LogUtils.getLogger();

    //private static final ResourceLocation WINDOW_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/window.png");
    private static final ResourceLocation WINDOW_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "skill_tree/window");

    public static final int WINDOW_WIDTH = 248;
    public static final int WINDOW_HEIGHT = 186;
    public static final int WINDOW_WIDTH_INFO_CHANGE = -68;
    private int infoWindowDynamicWidth = WINDOW_WIDTH;
    private static final int WINDOW_INSIDE_X = 9;
    private int infoWindowInsideDynamicWidth = WINDOW_INSIDE_X;
    private static final int WINDOW_INSIDE_Y = 9;
    private static final int WINDOW_INSIDE_TOP_Y = 18;
    protected static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH-WINDOW_INSIDE_X*2;
    protected static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT-WINDOW_INSIDE_Y-WINDOW_INSIDE_TOP_Y;
    private static final int GAP_WINDOW_INFO = 7;

    private static final int INFO_DYNAMIC_OFFSET_FROM_CENTER = - (GAP_WINDOW_INFO + SkillTreeInfoScreen.WIDTH)/2;
    private int infoDynamicOffset = 0;
    private static final int DYNAMIC_POSITIONING_TICKS = 40;
    private int infoDynamicTicks = DYNAMIC_POSITIONING_TICKS;
    

    private static final Component TITLE = Component.translatable("gui.skill_tree");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private final ClientSkillTree skillTree;
    private final Map<ResourceLocation,SkillTreeTab> tabs = new LinkedHashMap<>();

    private final Map<SkillTreeWidget,SkillTreeWidgetScreen> widgetScreens = new LinkedHashMap<>();

    //private SkillTreeScrollable focusedScrollable;
    @Nullable private SkillTreeTab selectedTab;
    @Nullable private SkillTreeWidget selectedWidget;
    @Nullable private SkillTreeInfoScreen selectedWidgetInfo;


    public SkillTreeScreen(ClientSkillTree skillTree) {
        super(TITLE);

        this.skillTree = skillTree;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (QuietusKeyBindings.SKILL_TREE_KEY.get().matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (this.minecraft.gameMode.isServerControlledInventory()) {
                this.minecraft.player.sendOpenInventory();
            } else {
                this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            }
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }


    @Override
    public void init() {
        ServerPacketDistributor.requestSkillTreeUpdate();
        /* Header */
        this.layout.addTitleHeader(TITLE, this.font);
        /* Setup */ 
        this.tabs.clear();
        ClientSkillTreePayloadHandler.getCategories().forEach((id, category) -> {
            ConnectivityPosition connectivityPosition = category.positionNodes(SkillTreeWidget.WIDTH, SkillTreeWidget.HEIGHT);
            SkillTreeTab createdtab = SkillTreeTab.create(this.minecraft, this.skillTree, this, WINDOW_HEIGHT, category, connectivityPosition);
            if (!Objects.isNull(createdtab))
                this.tabs.put(id, createdtab);
            category.setListener(this);
            this.selectedTab = createdtab; // TODO: debug temp
        });
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

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.tickRender();

        int treeOffsetX = (this.width - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        int infoOffsetX = (this.width - this.infoWindowDynamicWidth) / 2 + this.infoWindowDynamicWidth + GAP_WINDOW_INFO + this.infoDynamicOffset;
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;

        this.renderTreeWindow(guiGraphics, mouseX, mouseY, treeOffsetX, offsetY);
        if (this.selectedWidget != null) {
            this.renderInfoWindow(guiGraphics, mouseX, mouseY, infoOffsetX, offsetY);
        }
        /* this.renderWindow(guiGraphics, offsetX, offsetY); */
        /* this.renderScreens(guiGraphics, mouseX, mouseY, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y); // offset from the inside content */
        //LOGGER.info("partialTick: {}",partialTick);
    }

    /* private void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        //guiGraphics.blit(RenderType::guiTextured, WINDOW_LOCATION, offsetX, offsetY, 0.0F, 0.0F, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT);
        guiGraphics.blitSprite(RenderType::guiTextured, WINDOW_SPRITE_LOCATION, offsetX, offsetY, WINDOW_WIDTH, WINDOW_HEIGHT);
    } */

    private void renderTreeWindow(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        this.selectedTab.drawContents(guiGraphics, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, this.infoWindowInsideDynamicWidth, WINDOW_INSIDE_HEIGHT);
        guiGraphics.blitSprite(RenderType::guiTextured, WINDOW_SPRITE_LOCATION, offsetX, offsetY, this.infoWindowDynamicWidth, WINDOW_HEIGHT);
    }

    private void renderInfoWindow(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        this.selectedWidgetInfo.draw(guiGraphics, mouseX, mouseY, offsetX, offsetY);
        //guiGraphics.blitSprite(RenderType::guiTextured, INFO_CONTENTS_SPRITE_LOCATION, offsetX, offsetY, INFO_WIDTH, INFO_HEIGHT);
    }

    /* private void renderScreens(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        for (SkillTreeWidgetScreen screen : this.widgetScreens.values()) {
            screen.draw(guiGraphics, mouseX, mouseY, offsetX, offsetY);
        }
    } */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // int offsetX = (this.width - WINDOW_WIDTH) / 2;
        // int offsetY = (this.height - WINDOW_HEIGHT) / 2;
        // List<SkillTreeWidgetScreen> list = new ArrayList<>(this.widgetScreens.values());
        // SkillTreeWidgetScreen outmost_focused_screen = null;
        // for (SkillTreeWidgetScreen screen : list) {
        //     if (screen.isMouseOverWindow(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, mouseX, mouseY)) {
        //         this.focusedScrollable = screen;
        //         outmost_focused_screen = screen;
        //     }
        // }
        // if (outmost_focused_screen != null) {
        //     /* no matter clicked or not, return the interaction boolean, 
        //     to avoid clicking onto screens below the outmost screen, or the Widgets on selected tab. */
        //     return outmost_focused_screen.click(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, mouseX, mouseY, button); 
        // } else {
        //     if (!Objects.isNull(this.selectedTab)) {
        //         this.focusedScrollable = this.selectedTab;
        //         if (this.selectedTab.click(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, mouseX, mouseY, button)) {
        //             return true;
        //         }
        //     }
        // }
        // return super.mouseClicked(mouseX, mouseY, button);
        
        int offsetX = WINDOW_INSIDE_X + (this.width - this.infoWindowDynamicWidth) / 2 + this.infoDynamicOffset;
        int offsetY = WINDOW_INSIDE_TOP_Y + (this.height - WINDOW_HEIGHT) / 2;

        if (this.selectedTab != null) {
            if (this.selectedTab.click(offsetX, offsetY, mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) {
            return false;
        } else {
            if (this.selectedTab != null) this.selectedTab.scroll(dragX, dragY);
            /* this.focusedScrollable.scroll(dragX, dragY); */
            return true;
        }
    }

    private void tickRender() {
        this.infoDynamicTicks = this.selectedWidget == null ?
            Math.min(this.infoDynamicTicks+1, DYNAMIC_POSITIONING_TICKS)
            : Math.max(this.infoDynamicTicks-1, 0);
        this.infoWindowDynamicWidth = WINDOW_WIDTH + (int)Math.round((1.0d - (double)this.infoDynamicTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        this.infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + (int)Math.round((1.0d - (double)this.infoDynamicTicks / (double)DYNAMIC_POSITIONING_TICKS) * WINDOW_WIDTH_INFO_CHANGE);
        
        this.infoDynamicOffset = calcInverse((double)INFO_DYNAMIC_OFFSET_FROM_CENTER,(double)DYNAMIC_POSITIONING_TICKS, 80.0d, this.infoDynamicTicks, this.selectedWidget == null);

        this.infoWindowDynamicWidth = WINDOW_WIDTH + calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicks, this.selectedWidget == null);
        this.infoWindowInsideDynamicWidth = WINDOW_INSIDE_WIDTH + calcInverse((double)WINDOW_WIDTH_INFO_CHANGE,(double)DYNAMIC_POSITIONING_TICKS, 45.0d, this.infoDynamicTicks, this.selectedWidget == null);
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

    protected void setSelectedWidget(SkillTreeWidget widget) {
        this.selectedWidget = widget;
        this.selectedWidgetInfo = widget == null ? null : SkillTreeInfoScreen.create(widget, this.font, this);
    }
    public SkillTreeWidget getSelectedWidget() {
        return this.selectedWidget;
    }

    /* SkillCategory.Listener method */
    @Override
    public void onAddRootSkillNode(ResourceLocation categoryId, SkillTreeNode node) {
        this.tabs.get(categoryId).addWidget(node);
    }

    /* SkillCategory.Listener method */
    @Override
    public void onAddDependantSkillNode(ResourceLocation categoryId, SkillTreeNode node) {
        this.tabs.get(categoryId).addWidget(node);
    }

}
