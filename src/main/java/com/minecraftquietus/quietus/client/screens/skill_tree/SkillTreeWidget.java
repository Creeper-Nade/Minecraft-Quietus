package com.minecraftquietus.quietus.client.screens.skill_tree;

import java.util.Optional;
import java.util.Set;

import com.minecraftquietus.quietus.client.multiplayer.ClientSkillTree;
import com.minecraftquietus.quietus.skilltree.SkillPoint;
import com.minecraftquietus.quietus.skilltree.SkillTreeNode;
import com.minecraftquietus.quietus.skilltree.TreeNodePosition;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class SkillTreeWidget {

    /* Width and height responsible for calculation of hover and clicking */
    protected static final int HEIGHT = 26;
    protected static final int WIDTH = 26;

    /* Icon width and height */
    protected static final int ICON_HEIGHT = 26;
    protected static final int ICON_WIDTH = 26;

    private static final ResourceLocation DEFAULT_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/icons/skill_tree/node/none.png");

    private final SkillTreeTab tab;
    private final SkillTreeNode node;
    private final Minecraft minecraft;
    private final ClientSkillTree skillTree;
    private final ResourceLocation icon;
    private final String languangeKey;

    private final int x;
    private final int y;
    private final Set<SkillTreeWidget> mustParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> orParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> children = new ReferenceOpenHashSet<>();

    private final SkillPointType widgettype;

    public SkillTreeWidget(SkillTreeTab tab, Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeNode node, int x, int y, SkillPoint.DisplayInfo display) {
        this.tab = tab;
        this.minecraft = minecraft;
        this.skillTree = clientSkillTree;
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
        if (this.minecraft.getResourceManager().getResource(this.icon).isPresent()) {
            guiGraphics.blit(RenderType::guiTextured, this.icon, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        } else {
            guiGraphics.blit(RenderType::guiTextured, DEFAULT_ICON, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        }
        //guiGraphics.blit(RenderType::guiTextured, this.icon, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
    }

    public void drawConnectivity(GuiGraphics guiGraphics, TreeNodePosition graph, int offsetX, int offsetY) {
        
    }
    
    /* public void drawConnectivity(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        int toX = offsetX + this.x + WIDTH/2;
        int toY = offsetY + this.y + HEIGHT/2;
        for (SkillTreeWidget widget : this.orParents) {
            int fromX = offsetX + widget.x + WIDTH/2;
            int fromY = offsetY + widget.y + HEIGHT/2;
            if (this.x == widget.x) {
                guiGraphics.vLine(fromX, fromY, toY, 0xFF000000);
            } else {
                int tp_1_x = fromX;
                int tp_1_y = fromY + HEIGHT;
                int tp_2_x = toX;
                int tp_2_y = tp_1_y;
                guiGraphics.vLine(fromX, fromY, tp_1_y, 0xFF000000);
                guiGraphics.hLine(tp_1_x, tp_2_x, tp_1_y, 0xFF000000);
                guiGraphics.vLine(tp_2_x, tp_2_y, toY, 0xFF000000);
            }
        }
        for (SkillTreeWidget widget : this.mustParents) {
            int fromX = offsetX + widget.x + WIDTH/2;
            int fromY = offsetY + widget.y + HEIGHT/2;
            if (this.x == widget.x) {
                guiGraphics.vLine(fromX, fromY, toY, 0xFF000000);
            } else {
                int tp_1_x = fromX;
                int tp_1_y = fromY + HEIGHT;
                int tp_2_x = toX;
                int tp_2_y = tp_1_y;
                guiGraphics.vLine(fromX, fromY, tp_1_y, 0xFF000000);
                guiGraphics.hLine(tp_1_x, tp_2_x, tp_1_y, 0xFF000000);
                guiGraphics.vLine(tp_2_x, tp_2_y, toY, 0xFF000000);
            }
        }
    } */

    public boolean click(int offsetX, int offsetY, double mouseX, double mouseY, int mouseButton) {
        int offsetWithScrollX = offsetX + (int)this.tab.scrollX;
        int offsetWithScrollY = offsetY + (int)this.tab.scrollY;
        if (mouseButton == 0 && this.isMouseOver(offsetWithScrollX, offsetWithScrollY, (int)mouseX, (int)mouseY)) {
            //this.tab.getScreen().addWidgetScreen(this, new SkillTreeWidgetScreen(this.minecraft, this.skillTree, this, this.tab.getScreen(), this.x + (int)this.tab.scrollX, this.y + (int)this.tab.scrollY));
            if (this.matches(this.tab.getScreen().getSelectedWidget())) {
                this.tab.getScreen().setSelectedWidget(null);
            } else {
                this.tab.getScreen().setSelectedWidget(this);
            }
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

            this.node.parents().forEach((node) -> {
                if (this.tab.getWidget(node) != null) this.tab.getWidget(node).addChild(this);});
        }
    }

    public String getLanguageKey() {
        return this.languangeKey;
    }
    protected SkillTreeNode getNode() {
        return this.node;
    }
    public Optional<SkillPoint.DisplayInfo> getDisplay() {
        return this.node.getSkillPoint().display();
    }

    /**
     * Checks if this widget has equal node as other widget using {@link SkillTreeNode#equals(Object)}
     * @param other other widget
     * @return true if matching, else false
     */
    public boolean matches(Object other) {
        if (other == null) return false;
        if (this == other) {
            return true;
        } else if (other instanceof SkillTreeWidget otherWidget) {
            if (this.node.equals(otherWidget.node)) {
                return true;
            }
        }
        return false;
    }

}
