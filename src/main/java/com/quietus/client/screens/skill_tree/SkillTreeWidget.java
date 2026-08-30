package com.quietus.client.screens.skill_tree;

import java.util.List;
import java.util.Set;

import com.quietus.client.multiplayer.ClientSkillTree;
import com.quietus.client.multiplayer.ClientSkillTreeListener;
import com.quietus.client.util.GuiGraphicsExtractorUtil;
import com.quietus.skilltree.*;

import com.quietus.util.MathUtil;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import static com.quietus.Quietus.MODID;

public class SkillTreeWidget extends AbstractWidget implements ClientSkillTreeListener {

    /* Width and height responsible for calculation of hover and clicking */
    protected static final int HEIGHT = 26;
    protected static final int WIDTH = 26;

    /* Icon width and height */
    protected static final int ICON_HEIGHT = 26;
    protected static final int ICON_WIDTH = 26;

    /* Hover tooltip widths and heights */
    private static final Identifier ICON_CONTAINER_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/container_icon");
    private static final int ICON_CONTAINER_WIDTH = 28;
    private static final int ICON_CONTAINER_HEIGHT = 28;
    private static final int TOOLTIP_CONTAINER_X_OFFSET = -7;
    private static final Identifier HEADER_CONTAINER_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/container_header");
    private static final Identifier HEADER_CONTAINER_GRAYSCALE_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/container_header_grayscale");
    private static final Identifier HEADER_CONTAINER_GRAYSCALE_GLOW_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/container_header_grayscale_glow");
    private static final int HEADER_CONTAINER_RESOURCE_WIDTH = 57;
    private static final int HEADER_CONTAINER_RESOURCE_HEIGHT = 20;
    private static final int HEADER_CONTAINER_MIN_WIDTH = 128;
    private static final int HEADER_CONTAINER_MAX_WIDTH = 208;
    private static final int HEADER_CONTAINER_TOP_MARGIN = 6;
    private static final int HEADER_CONTAINER_BOTTOM_MARGIN = 5;
    private static final int HEADER_CONTAINER_H_MARGIN = 5;
    private static final int HEADER_ICON_TEXT_CONTAINER_H_MARGIN = 3;
    private static final int HEADER_CONTAINER_Y_OFFSET = 3;
    private static final int HEADER_TEXT_MAX_WIDTH = HEADER_CONTAINER_MAX_WIDTH - ICON_CONTAINER_WIDTH - Math.abs(TOOLTIP_CONTAINER_X_OFFSET) - HEADER_ICON_TEXT_CONTAINER_H_MARGIN - HEADER_CONTAINER_H_MARGIN;
    private static final int CONTENTS_CONTAINER_Y_OVERLAP_OFFSET = -3;
    private static final Identifier CONTENTS_CONTAINER_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/container_contents");
    private static final Identifier CONTENTS_CONTAINER_2_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/container_contents_flipped");
    private static final int CONTENTS_CONTAINER_RESOURCE_WIDTH = 57;
    private static final int CONTENTS_CONTAINER_RESOURCE_HEIGHT = 20;
    private static final int CONTENTS_CONTAINER_TOP_MARGIN = 10;
    private static final int CONTENTS_CONTAINER_BOTTOM_MARGIN = 5;
    private static final int CONTENTS_CONTAINER_H_MARGIN = 5;
    private static final int CONTENTS_DESCRIPTION_MAX_LINES = 3;
    private static final Identifier CONTAINERS_SHADOW_SPRITE_LOCATION = Identifier.fromNamespaceAndPath(MODID, "skill_tree/hover/containers_shadow");
    private static final int CONTAINERS_SHADOW_THICKNESS = 4;
    private static final int TEXT_LINE_SPACING = 2;

    private static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(MODID, "textures/gui/icons/skill_tree/node/none.png");

    private static final int TICKS_HEADER_FADEIN = 7;
    private static final int TICKS_CONTENTS_WAIT = 12;
    private static final int TICKS_CONTENTS_FADEIN = 25;
    private static final int TICKS_CONTENTS_LINE_FADEIN = 14;
    private static final int TICKS_TOTAL = + TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT + TICKS_CONTENTS_FADEIN + TICKS_CONTENTS_LINE_FADEIN;

    private int tooltipTicks;

    private final SkillTreeTab tab;
    private final SkillTreeNode node;
    private final Minecraft minecraft;
    private final Font font;
    private final ClientSkillTree skillTree;
    private final Identifier icon;
    private final SkillPoint.DisplayInfo display;
    private final String languangeKey;

    private final TreePosition.Vertex vertexPos;
    private final Set<SkillTreeWidget> mustParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> orParents = new ReferenceOpenHashSet<>();
    private final Set<SkillTreeWidget> children = new ReferenceOpenHashSet<>();

    private final SkillPointType widgettype;

    private boolean unlocked = false;
    private boolean obtained = false;

    public SkillTreeWidget(SkillTreeTab tab, Minecraft minecraft, Font font, ClientSkillTree clientSkillTree, SkillTreeNode node, TreePosition.Vertex vertexPos, SkillPoint.DisplayInfo display) {
        super(vertexPos.x(), vertexPos.y(), WIDTH, HEIGHT, display.header());

        this.vertexPos = vertexPos;
        this.tab = tab;
        this.minecraft = minecraft;
        this.font = font;
        this.skillTree = clientSkillTree;
        this.node = node;
        this.icon = display.icon().isPresent() ? 
            display.icon().get().id() :
            node.getId().withPath((id) -> "textures/gui/icons/skill_tree/node/" + id + ".png");
            //Identifier.fromNamespaceAndPath(node.getId().getNamespace(), "textures/gui/icons/skill_tree/node/" + node.getId().getPath() + ".png");
        this.display = display;
        this.languangeKey = node.getId().toLanguageKey();

        this.widgettype = display.type();

        this.tooltipTicks = 0;

        this.updateStatus(this.skillTree);
        this.skillTree.addListener(this.node, this);
    }

    public void updateStatus(ClientSkillTree tree) {
        Prerequisites prereq = this.getNode().getSkillPoint().unlock().prerequisites();
        this.unlocked = prereq.requirements().test(Prerequisites.CompletionStatus.make(prereq, tree.getCompletedAdvancements(), tree.getCompletedParents()));
        this.obtained = tree.getOrStartProgress(this.node).isProgressing();
    }

    @Override
    public void onClientSkillTreeUpdate(int amount, int maxAmount, int progressAmount) {
        this.updateStatus(this.skillTree);
    }

    protected void tooltipRenderTick() {
        if (this.isHovered) {
            this.tooltipTicks = Math.min(TICKS_TOTAL, this.tooltipTicks+1);
        } else {
            this.tooltipTicks = Math.max(0, this.tooltipTicks-2);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        this.drawAbsolute(gui, this.getX(), this.getY(), this.unlocked);
    }

    public void updatePositionOffset(int offsetX, int offsetY) {
        this.setPosition(this.vertexPos.x() + offsetX, this.vertexPos.y() + offsetY);
    }

    public void extractSelectedHighlight(GuiGraphicsExtractor gui) {
        int x = this.getX();
        int y = this.getY();

        gui.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_CONTAINER_SPRITE_LOCATION, x + (ICON_WIDTH-ICON_CONTAINER_WIDTH)/2, y + (ICON_HEIGHT-ICON_CONTAINER_HEIGHT)/2, ICON_CONTAINER_WIDTH, ICON_CONTAINER_HEIGHT);
        this.drawAbsolute(gui, x, y, this.unlocked);
    }

    public void extractHoverTooltip(GuiGraphicsExtractor gui) {
        SkillPointProgress.ClientData data = this.skillTree.getOrStartProgress(this.node);
        Component progressComponent;

        if (!this.unlocked) {
            progressComponent = Component.literal("[")
                .append(Component.translatable("gui.skill_tree.description.progress.locked", data.times(), data.maxAmount()))
                .append("]")
                .withColor(this.getTab().getThemeColour());
        } else if (data.isProgressing()) {
            progressComponent = Component.literal("[")
                .append(Component.translatable("gui.skill_tree.description.progress.obtained", data.times(), data.maxAmount()))
                .append("]")
                .withColor(this.getTab().getThemeColour());
        } else {
            progressComponent = Component.literal("[")
                .append(Component.translatable("gui.skill_tree.description.progress.unobtained", data.times(), data.maxAmount()))
                .append("]")
                .withColor(this.getTab().getThemeColour());
        }

        int x = this.getX();
        int y = this.getY();

        SkillTreeScreen screen = this.getTab().getScreen();
        int iconScreenX = x + (int)Math.round(screen.offsetXFTree - screen.offsetX);
        int iconScreenY = y;

        int windowMinX = screen.offsetX + SkillTreeScreen.WINDOW_INSIDE_X;
        int windowMaxX = windowMinX + screen.dynamicInsideWidth();
        int windowMinY = screen.offsetY + SkillTreeScreen.WINDOW_INSIDE_TOP_Y;
        int windowMaxY = windowMinY + SkillTreeScreen.WINDOW_INSIDE_HEIGHT;

        int containersWidth = Math.max(HEADER_CONTAINER_MIN_WIDTH, GuiGraphicsExtractorUtil.getWordWrapWidth(this.font, this.display.header(), HEADER_TEXT_MAX_WIDTH) + ICON_CONTAINER_WIDTH + Math.abs(TOOLTIP_CONTAINER_X_OFFSET) + HEADER_ICON_TEXT_CONTAINER_H_MARGIN + HEADER_CONTAINER_H_MARGIN*2);
        int headerTextHeight = GuiGraphicsExtractorUtil.getWordWrapHeight(this.font, this.display.header(), HEADER_TEXT_MAX_WIDTH, TEXT_LINE_SPACING);
        int headerContainerHeight = HEADER_CONTAINER_TOP_MARGIN + HEADER_CONTAINER_BOTTOM_MARGIN + headerTextHeight;
        int descriptionTextMaxWidth = containersWidth - CONTENTS_CONTAINER_H_MARGIN*2;

        Component displayDesc = this.display.description();
        if (displayDesc != null && !displayDesc.getString().isEmpty()) {
            displayDesc = displayDesc.copy().withColor(0xFFFFFFFF);
            if (this.font.split(displayDesc, descriptionTextMaxWidth).size() > CONTENTS_DESCRIPTION_MAX_LINES) {
                String fullText = displayDesc.getString();
                String truncated = fullText;
                while (truncated.length() > 0 && this.font.split(Component.literal(truncated + " ... ").withColor(0xFFFFFFFF), descriptionTextMaxWidth).size() > CONTENTS_DESCRIPTION_MAX_LINES) {
                    truncated = truncated.substring(0, truncated.length() - 1);
                }
                displayDesc = Component.literal(truncated.trim() + " ... ").withColor(0xFFFFFFFF);
            }
        }

        Component description = (displayDesc == null || displayDesc.getString().isEmpty())
                ? progressComponent
                : Component.empty().append(progressComponent).append("\n\n").append(displayDesc);

        int contentsTextHeight = GuiGraphicsExtractorUtil.getWordWrapHeight(this.font, description, descriptionTextMaxWidth, TEXT_LINE_SPACING);
        int contentsContainerSupposedHeight = contentsTextHeight + CONTENTS_CONTAINER_TOP_MARGIN + CONTENTS_CONTAINER_BOTTOM_MARGIN;
        int contentsContainerHeight = (int)Math.ceil(MathUtil.cubicLerp(TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT, 1, TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT + TICKS_CONTENTS_FADEIN, contentsContainerSupposedHeight, this.tooltipTicks));
        int totalTooltipSupposedHeight = headerContainerHeight + contentsContainerSupposedHeight + CONTENTS_CONTAINER_Y_OVERLAP_OFFSET;

        boolean renderRight = (iconScreenX + containersWidth <= windowMaxX) || (iconScreenX - windowMinX < windowMaxX - iconScreenX);
        boolean renderBelow = (iconScreenY + ICON_HEIGHT + totalTooltipSupposedHeight <= windowMaxY) || (iconScreenY - windowMinY < windowMaxY - iconScreenY);

        int containerX = renderRight
                ? x + TOOLTIP_CONTAINER_X_OFFSET
                : x + ICON_WIDTH - TOOLTIP_CONTAINER_X_OFFSET - containersWidth;

        int headerTextX = renderRight
                ? containerX + ICON_CONTAINER_WIDTH + Math.abs(TOOLTIP_CONTAINER_X_OFFSET) + HEADER_ICON_TEXT_CONTAINER_H_MARGIN
                : containerX + HEADER_CONTAINER_H_MARGIN;

        int headerContainerY;
        int contentsContainerY;
        int contentsTextY;
        Identifier contentsContainerSprite;
        int contentsTextMargin;
        int shadowY;

        if (renderBelow) {
            headerContainerY = y + HEADER_CONTAINER_Y_OFFSET;
            contentsContainerY = headerContainerY + headerContainerHeight + CONTENTS_CONTAINER_Y_OVERLAP_OFFSET;
            contentsContainerSprite = CONTENTS_CONTAINER_SPRITE_LOCATION;
            contentsTextMargin = CONTENTS_CONTAINER_TOP_MARGIN;
            contentsTextY = contentsContainerY + contentsTextMargin;
            shadowY = headerContainerY - CONTAINERS_SHADOW_THICKNESS;
        } else {
            headerContainerY = y + ICON_HEIGHT - HEADER_CONTAINER_Y_OFFSET - headerContainerHeight;
            contentsContainerY = headerContainerY - contentsContainerHeight - CONTENTS_CONTAINER_Y_OVERLAP_OFFSET;
            contentsContainerSprite = CONTENTS_CONTAINER_2_SPRITE_LOCATION;
            contentsTextMargin = CONTENTS_CONTAINER_BOTTOM_MARGIN;
            contentsTextY = headerContainerY - contentsContainerSupposedHeight - CONTENTS_CONTAINER_Y_OVERLAP_OFFSET + contentsTextMargin;
            shadowY = contentsContainerY - CONTAINERS_SHADOW_THICKNESS;
        }

        int headerTextY = headerContainerY + HEADER_CONTAINER_TOP_MARGIN;
        int contentsTextX = containerX + CONTENTS_CONTAINER_H_MARGIN;
        int shadowX = containerX - CONTAINERS_SHADOW_THICKNESS;

        int headerWhiteMask = ARGB.color(Math.clamp((float) this.tooltipTicks / TICKS_HEADER_FADEIN, 0.0f, 1.0f), 0xFFFFFFFF);

        // shadow
        gui.enableScissor(
            windowMinX,
            windowMinY,
            windowMaxX + 1,
            windowMaxY
        );
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, CONTAINERS_SHADOW_SPRITE_LOCATION, shadowX, shadowY, containersWidth + CONTAINERS_SHADOW_THICKNESS*2, headerContainerHeight + contentsContainerHeight + CONTAINERS_SHADOW_THICKNESS*2, headerWhiteMask);
        gui.disableScissor();

        // contents
        int contentsStartTick = TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT;
        if (this.tooltipTicks > contentsStartTick) {
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, contentsContainerSprite, containerX, contentsContainerY, containersWidth, contentsContainerHeight);

            gui.enableScissor(containerX, contentsContainerY, containerX + containersWidth, contentsContainerY + contentsContainerHeight);

            List<FormattedCharSequence> descLines = this.font.split(description, descriptionTextMaxWidth);
            int lineY = contentsTextY;

            for (int i = 0; i < descLines.size(); i++) {
                FormattedCharSequence lineSeq = descLines.get(i);
                float lineStartTick = renderBelow ? // starts when the container fully covers the space of this line of text
                    (int)MathUtil.inverseCubicLerp(TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT, 1, TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT + TICKS_CONTENTS_FADEIN, contentsContainerSupposedHeight, (i+1) * this.font.lineHeight + contentsTextMargin)
                    : (int)MathUtil.inverseCubicLerp(TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT, contentsContainerSupposedHeight, TICKS_HEADER_FADEIN + TICKS_CONTENTS_WAIT + TICKS_CONTENTS_FADEIN, 1, (i+1) * this.font.lineHeight + contentsTextMargin);
                float lineProgress = Math.clamp((float)(this.tooltipTicks - lineStartTick) / TICKS_CONTENTS_LINE_FADEIN, 0.0f, 1.0f);
                float smoothLineT = (float) MathUtil.cubicLerp(0.0f, 0.0f, 1.0f, 1.0f, lineProgress);

                if (smoothLineT > 0.0f) {
                    int lineMask = ARGB.color(smoothLineT, 0xFFFFFFFF);
                    int animY = renderBelow ? lineY - (int)Math.round((1.0f - smoothLineT) * 3.0f) : lineY + (int)Math.round((1.0f - smoothLineT) * 3.0f);
                    gui.text(this.font, lineSeq, contentsTextX, animY, lineMask, true);
                }

                lineY += this.font.lineHeight + TEXT_LINE_SPACING;
            }

            gui.disableScissor();
        }

        // header: fades in (increasing opacity)
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, HEADER_CONTAINER_SPRITE_LOCATION, containerX, headerContainerY, containersWidth, headerContainerHeight, headerWhiteMask);
        if (this.unlocked) {
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, HEADER_CONTAINER_GRAYSCALE_GLOW_SPRITE_LOCATION, containerX, headerContainerY, containersWidth, headerContainerHeight, ARGB.multiply(headerWhiteMask, this.getTab().getThemeColour()));
            int glimmerWaveCenterY = screen.glimmerWaveCenterY();
            int glimmerSteps = 4;
            for (int step = 1; step <= glimmerSteps; step++) {
                float factor = 1.0f - (float) (step - 1) / glimmerSteps;
                int halfHeight = Math.max(1, Math.round(SkillTreeScreen.GLIMMER_WAVE_HALF_HEIGHT * factor));
                int scissorTop = Math.max(headerContainerY, glimmerWaveCenterY - halfHeight);
                int scissorBottom = Math.min(headerContainerY + headerContainerHeight, glimmerWaveCenterY + halfHeight);

                if (scissorBottom > scissorTop) {
                    gui.enableScissor(containerX, scissorTop, containerX + containersWidth, scissorBottom);
                    gui.blitSprite(RenderPipelines.GUI_TEXTURED, HEADER_CONTAINER_GRAYSCALE_GLOW_SPRITE_LOCATION, containerX, headerContainerY, containersWidth, headerContainerHeight, ARGB.multiply(headerWhiteMask, this.getTab().getThemeColour()));
                    gui.disableScissor();
                }
            }
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, HEADER_CONTAINER_GRAYSCALE_SPRITE_LOCATION, containerX, headerContainerY, containersWidth, headerContainerHeight, ARGB.multiply(headerWhiteMask, this.getTab().getThemeColour()));
        }

        GuiGraphicsExtractorUtil.drawWordWrap(gui, this.font, this.display.header(), headerTextX, headerTextY, HEADER_TEXT_MAX_WIDTH, TEXT_LINE_SPACING, headerWhiteMask, true);

        if (this.isHovered) {
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_CONTAINER_SPRITE_LOCATION, x + (ICON_WIDTH-ICON_CONTAINER_WIDTH)/2, y + (ICON_HEIGHT-ICON_CONTAINER_HEIGHT)/2, ICON_CONTAINER_WIDTH, ICON_CONTAINER_HEIGHT);
        }
        this.drawAbsolute(gui, x, y, this.unlocked);
    }

    public void drawAbsolute(GuiGraphicsExtractor gui, int x, int y) {
        this.drawAbsolute(gui, x, y, this.unlocked);
    }

    public void drawAbsolute(GuiGraphicsExtractor gui, int x, int y, boolean unlocked) {
        int mask = unlocked ? 0xFFFFFFFF : 0xFF909090;
        gui.blit(RenderPipelines.GUI_TEXTURED, this.widgettype.getLocation(this.obtained), x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT, mask);
        if (this.minecraft.getResourceManager().getResource(this.icon).isPresent()) {
            gui.blit(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT, mask);
        } else {
            gui.blit(RenderPipelines.GUI_TEXTURED, DEFAULT_ICON, x, y, 0.0f, 0.0f, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT, mask);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.node.equals(this.tab.getScreen().getSelectedNode())) {
            this.tab.getScreen().setSelectedNode(null);
        } else {
            this.tab.getScreen().setSelectedNode(this.node);
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        //soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /*public boolean isMouseOver(int offsetX, int offsetY, int mouseX, int mouseY) {
        int actual_x = this.getX() + offsetX;
        int actual_y = this.getY() + offsetY;
        return 
            mouseX > actual_x 
            && mouseX < actual_x + WIDTH
            && mouseY > actual_y
            && mouseY < actual_y + HEIGHT;
    }*/

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

    public boolean isUnlocked() {
        return this.unlocked;
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
    public SkillPoint.DisplayInfo getDisplay() {
        return this.display;
    }

    protected int getTooltipTicks() {
        return  this.tooltipTicks;
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
    }

}
