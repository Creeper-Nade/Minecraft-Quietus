package com.minecraftquietus.quietus.client.screens.skill_tree;

import static com.minecraftquietus.quietus.Quietus.MODID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.minecraftquietus.quietus.client.QuietusKeyBindings;
import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.mojang.logging.LogUtils;
import com.minecraftquietus.quietus.skilltree.ConnectivityPosition;
import com.minecraftquietus.quietus.skilltree.SkillCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeScreen extends Screen implements SkillCategory.Listener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation WINDOW_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/skill_tree/window.png");
    public static final int WINDOW_WIDTH = 248;
    public static final int WINDOW_HEIGHT = 166;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 9;
    private static final int WINDOW_INSIDE_TOP_Y = 18;
    protected static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH-WINDOW_INSIDE_X*2;
    protected static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT-WINDOW_INSIDE_Y*2;

    private static final Component TITLE = Component.translatable("gui.skill_tree");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private final ClientSkillTree skillTree;
    private final Map<ResourceLocation,SkillTreeTab> tabs = new LinkedHashMap<>();

    private final Map<SkillTreeWidget,SkillTreeWidgetScreen> widgetScreens = new LinkedHashMap<>();

    private SkillTreeScrollable focusedScrollable;
    @Nullable private SkillTreeTab selectedTab;

    /* SkillTreeTab testTab;
    SkillTreeNode testNode;
    SkillTreeWidget testWidget;
    SkillTreeNode testNode2;
    SkillTreeWidget testWidget2; */

    public SkillTreeScreen(ClientSkillTree skillTree) {
        super(TITLE);

        this.skillTree = skillTree;

        /* // for testing
        testTab = new SkillTreeTab(Minecraft.getInstance(), this, 0, WINDOW_LOCATION, TITLE);
        testNode = new SkillTreeNode(ResourceLocation.fromNamespaceAndPath(MODID, "none"), null);
        testWidget = new SkillTreeWidget(testTab, Minecraft.getInstance(), testNode, 25, 25, WidgetType.SQUARE);
        testNode2 = new SkillTreeNode(ResourceLocation.fromNamespaceAndPath(MODID, "example"), null);
        testWidget2 = new SkillTreeWidget(testTab, Minecraft.getInstance(), testNode2, 25, 25 + 26 + 6, WidgetType.SQUARE);
        this.testTab.testAdd(testNode, testWidget);
        this.testTab.testAdd(testNode2, testWidget2);
        this.focusedScrollable = testTab;
        
        this.selectedTab = testTab; */
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
        /* Header */
        this.layout.addTitleHeader(TITLE, this.font);
        /* Setup */ 
        this.tabs.clear();
        ClientSkillTreePayloadHandler.getCategories().forEach((id, category) -> {
            ConnectivityPosition connectivityPosition = category.positionNodes(SkillTreeWidget.WIDTH, SkillTreeWidget.HEIGHT);
            SkillTreeTab createdtab = SkillTreeTab.create(minecraft, this, WINDOW_HEIGHT, category, connectivityPosition);
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
        int offsetX = (this.width - WINDOW_WIDTH) / 2;
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;
        this.renderInside(guiGraphics, mouseX, mouseY, offsetX, offsetY);
        this.renderWindow(guiGraphics, offsetX, offsetY);
        this.renderScreens(guiGraphics, mouseX, mouseY, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y); // offset from the inside content
    }

    private void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.blit(RenderType::guiTextured, WINDOW_LOCATION, offsetX, offsetY, 0.0F, 0.0F, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void renderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        this.selectedTab.drawContents(guiGraphics, offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y);
    }

    private void renderScreens(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        for (SkillTreeWidgetScreen screen : this.widgetScreens.values()) {
            screen.draw(guiGraphics, mouseX, mouseY, offsetX, offsetY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int offsetX = (this.width - WINDOW_WIDTH) / 2;
        int offsetY = (this.height - WINDOW_HEIGHT) / 2;
        List<SkillTreeWidgetScreen> list = new ArrayList<>(this.widgetScreens.values());
        SkillTreeWidgetScreen outmost_focused_screen = null;
        for (SkillTreeWidgetScreen screen : list) {
            if (screen.isMouseOverWindow(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, mouseX, mouseY)) {
                this.focusedScrollable = screen;
                outmost_focused_screen = screen;
            }
        }
        if (outmost_focused_screen != null) {
            /* no matter clicked or not, return the interaction boolean, 
            to avoid clicking onto screens below the outmost screen, or the Widgets on selected tab. */
            return outmost_focused_screen.click(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, mouseX, mouseY, button); 
        } else {
            if (!Objects.isNull(this.selectedTab)) {
                this.focusedScrollable = this.selectedTab;
                if (this.selectedTab.click(offsetX + WINDOW_INSIDE_X, offsetY + WINDOW_INSIDE_TOP_Y, mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) {
            return false;
        } else {
            this.focusedScrollable.scroll(dragX, dragY);
            return true;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void addWidgetScreen(SkillTreeWidget widget, SkillTreeWidgetScreen screen) {
        this.widgetScreens.put(widget, screen);
    }
    protected void removeWidgetScreen(SkillTreeWidget widget) {
        if (this.focusedScrollable.equals(this.widgetScreens.get(widget)))
            this.focusedScrollable = this.selectedTab;
        this.widgetScreens.remove(widget);
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
