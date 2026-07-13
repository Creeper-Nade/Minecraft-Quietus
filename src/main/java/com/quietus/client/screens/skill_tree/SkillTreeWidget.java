package com.quietus.client.screens.skill_tree;

import java.util.Optional;
import java.util.Set;

import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.skilltree.SkillPoint;
import com.quietus.skilltree.SkillTreeNode;
import com.quietus.skilltree.TreePosition;
import com.quietus.skilltree.LegacyPosition;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import static com.quietus.Quietus.MODID;

public class SkillTreeWidget extends AbstractWidget {

    /* Width and height responsible for calculation of hover and clicking */
    protected static final int HEIGHT = 26;
    protected static final int WIDTH = 26;

    /* Icon width and height */
    protected static final int ICON_HEIGHT = 26;
    protected static final int ICON_WIDTH = 26;

    private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/skill_tree/node/none.png");

    private final SkillTreeTab tab;
    private final SkillTreeNode node;
    private final Minecraft minecraft;
    private final ClientSkillTree skillTree;
    private final Identifier icon;
    private final String languangeKey;

    private final TreePosition.Vertex vertexPos;
    private final Set<SkillTreeWidget> mustParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> orParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> children = new ReferenceOpenHashSet<>();

    private final SkillPointType widgettype;

    public SkillTreeWidget(SkillTreeTab tab, Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeNode node, TreePosition.Vertex vertexPos, SkillPoint.DisplayInfo display) {
        super(vertexPos.x(), vertexPos.y(), WIDTH, HEIGHT, display.header());

        this.vertexPos = vertexPos;
        this.tab = tab;
        this.minecraft = minecraft;
        this.skillTree = clientSkillTree;
        this.node = node;
        this.icon = display.icon().isPresent() ? 
            display.icon().get().id() :
            node.getId().withPath((id) -> "textures/gui/icons/skill_tree/node/" + id + ".png");
            //Identifier.fromNamespaceAndPath(node.getId().getNamespace(), "textures/gui/icons/skill_tree/node/" + node.getId().getPath() + ".png");
        this.languangeKey = node.getId().toLanguageKey();

        this.widgettype = display.type();
    }

    /* public SkillTreeWidget(SkillTreeTab tab, Minecraft minecraft, ClientSkillTree clientSkillTree, SkillTreeNode node, TreePosition.Vertex vertexPos, SkillPoint.DisplayInfo display) {
        this(tab, minecraft, clientSkillTree, node, vertexPos.x(), vertexPos.y(), display);
    } */

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        this.drawAbsolute(gui, this.getX(), this.getY());
    }

    public void updatePositionOffset(int offsetX, int offsetY) {
        this.setPosition(this.vertexPos.x() + offsetX, this.vertexPos.y() + offsetY);
    }

    /* public void draw(GuiGraphicsExtractor GuiGraphicsExtractor, int offsetX, int offsetY) {
        int render_x = this.getX() + offsetX;
        int render_y = this.getY() + offsetY;
        this.drawAbsolute(GuiGraphicsExtractor, render_x, render_y);
    } */

    public void drawAbsolute(GuiGraphicsExtractor gui, int x, int y) {
        gui.blit(RenderPipelines.GUI_TEXTURED, this.widgettype.getLocation(false), x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        if (this.minecraft.getResourceManager().getResource(this.icon).isPresent()) {
            gui.blit(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        } else {
            gui.blit(RenderPipelines.GUI_TEXTURED, DEFAULT_ICON, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        }
    }

    public void drawConnectivity(GuiGraphicsExtractor gui, LegacyPosition graph, int offsetX, int offsetY) {
        
    }
    
    /* public void drawConnectivity(GuiGraphicsExtractor gui, int offsetX, int offsetY) {
        int toX = offsetX + this.x + WIDTH/2;
        int toY = offsetY + this.y + HEIGHT/2;
        for (SkillTreeWidget widget : this.orParents) {
            int fromX = offsetX + widget.x + WIDTH/2;
            int fromY = offsetY + widget.y + HEIGHT/2;
            if (this.x == widget.x) {
                gui.verticalLine(fromX, fromY, toY, 0xFF000000);
            } else {
                int tp_1_x = fromX;
                int tp_1_y = fromY + HEIGHT;
                int tp_2_x = toX;
                int tp_2_y = tp_1_y;
                gui.verticalLine(fromX, fromY, tp_1_y, 0xFF000000);
                gui.horizontalLine(tp_1_x, tp_2_x, tp_1_y, 0xFF000000);
                gui.verticalLine(tp_2_x, tp_2_y, toY, 0xFF000000);
            }
        }
        for (SkillTreeWidget widget : this.mustParents) {
            int fromX = offsetX + widget.x + WIDTH/2;
            int fromY = offsetY + widget.y + HEIGHT/2;
            if (this.x == widget.x) {
                gui.verticalLine(fromX, fromY, toY, 0xFF000000);
            } else {
                int tp_1_x = fromX;
                int tp_1_y = fromY + HEIGHT;
                int tp_2_x = toX;
                int tp_2_y = tp_1_y;
                gui.verticalLine(fromX, fromY, tp_1_y, 0xFF000000);
                gui.horizontalLine(tp_1_x, tp_2_x, tp_1_y, 0xFF000000);
                gui.verticalLine(tp_2_x, tp_2_y, toY, 0xFF000000);
            }
        }
    } */

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.node.equals(this.tab.getScreen().getSelectedNode())) {
            this.tab.getScreen().setSelectedNode(null);
        } else {
            this.tab.getScreen().setSelectedNode(this);
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        //soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /* public boolean click(int offsetX, int offsetY, double mouseX, double mouseY, int mouseButton) {
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
    } */

    public boolean isMouseOver(int offsetX, int offsetY, int mouseX, int mouseY) {
        int actual_x = this.getX() + offsetX;
        int actual_y = this.getY() + offsetY;
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
    protected SkillTreeTab getTab() {
        return this.tab;
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateWidgetNarration'");
    }

}
