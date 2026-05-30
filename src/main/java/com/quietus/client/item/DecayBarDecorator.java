package com.quietus.client.item;

import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;

import com.quietus.item.QuietusComponents;
import com.quietus.item.component.CanDecay;
import com.quietus.util.client.ContainerScreenUtil;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class DecayBarDecorator implements IItemDecorator {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DecayBarDecorator INSTANCE = new DecayBarDecorator();
    
    private DecayBarDecorator() {

    }

    @Override
    @SuppressWarnings("null") 
    public boolean render(GuiGraphicsExtractor guiGraphicsExtractor, Font font, ItemStack itemstack, int offsetX, int offsetY) {
        if (!itemstack.has(QuietusComponents.CAN_DECAY.get())) // skip immediately if this item does not have component
            return false;


        Minecraft minecraft = Minecraft.getInstance();
        CanDecay decayComponent = itemstack.get(QuietusComponents.CAN_DECAY.get());
        int decay = itemstack.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue();
        float fraction = decayComponent.getDecayFraction(decay); // CanDecay Component existance is already checked 5 lines above
        int width = 16; // 16 px for items
        int height = 16; // 16 px for items
        int outline_width = 1; // must not be negative
        int outline_height = 1; // must not be negative
        int height_offset = Math.round((height-1-outline_height*2) * fraction); // the height depending on how decayed is this item
        int color = decayComponent.getDisplayColor(decay, 0x88);
        int color_transparent = decayComponent.getDisplayColor(decay, 0x48);

        guiGraphicsExtractor.nextStratum();
        Matrix3x2fStack poseStack = guiGraphicsExtractor.pose();
        poseStack.pushMatrix();

        guiGraphicsExtractor.fill( 
            RenderPipelines.GUI, 
            offsetX + outline_width, 
            offsetY + height - height_offset - outline_height*2, 
            offsetX + width - outline_width, 
            offsetY + height - outline_height,
            color
        ); 
        boolean rendered = false;
        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) { // only in container GUI
            if (ContainerScreenUtil.isMouseDraggingItem(containerScreen, itemstack)) {
            //if (ContainerScreenUtil.isMouseInArea(containerScreen, offsetX-1, offsetY-1, offsetX + width, offsetY + height)) {
                guiGraphicsExtractor.fill(RenderPipelines.GUI, offsetX + outline_width, offsetY + outline_height, offsetX + width - outline_width, offsetY + height - height_offset - outline_height*2, color_transparent);
            } else {
                guiGraphicsExtractor.fill(RenderPipelines.GUI, offsetX, offsetY, offsetX + outline_width, offsetY + height, color);
                guiGraphicsExtractor.fill(RenderPipelines.GUI, offsetX + width - outline_width, offsetY, offsetX + width, offsetY + height, color);
                guiGraphicsExtractor.fill(RenderPipelines.GUI, offsetX + outline_width, offsetY, offsetX + width - outline_width, offsetY + outline_height, color);
                guiGraphicsExtractor.fill(RenderPipelines.GUI, offsetX + outline_width, offsetY + height - outline_height, offsetX + width - outline_width, offsetY + height, color);
            }
            rendered = true;
        }

        poseStack.popMatrix();
        
        return rendered;
    }
}