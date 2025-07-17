package com.minecraftquietus.quietus.client.item;

import org.slf4j.Logger;

import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.component.CanDecay;
import com.minecraftquietus.quietus.util.client.ContainerScreenUtil;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class DecayBarDecorator implements IItemDecorator {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DecayBarDecorator INSTANCE = new DecayBarDecorator();
    
    private DecayBarDecorator() {

    }

    @Override
    @SuppressWarnings("null") 
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemstack, int xOffset, int yOffset) {
        if (!itemstack.has(QuietusComponents.CAN_DECAY.get())) // skip immediately if this item does not have component
            return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) { // only in container GUI
            CanDecay decayComponent = itemstack.get(QuietusComponents.CAN_DECAY.get());
            int decay = itemstack.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue();
            float fraction = decayComponent.getDecayFraction(decay); // CanDecay Component existance is already checked 5 lines above
            int width = 16; // 16 px for items
            int height = 16; // 16 px for items
            int outline_width = 1; // must not be negative
            int outline_height = 1; // must not be negative
            int height_offset = Math.round((height-1-outline_height*2) * fraction); // the height depending on how decayed is this item
            int color = decayComponent.getDisplayColor(decay, 0x66);
            int color_transparent = decayComponent.getDisplayColor(decay, 0x48);
            /* int color = 0x66000038 | Mth.hsvToRgb((float)Math.round(fraction*2.5) / 2.5f * 0.33f, 1.0f, 1.0f); 
            int color_transparent = 0x48000098 | Mth.hsvToRgb((float)Math.round(fraction*3) / 3.0f * 0.33f, 1.0f, 1.0f); // green (normal) -> red (decayed) */

            guiGraphics.fill( 
                xOffset + outline_width, 
                yOffset + height - height_offset - outline_height*2, 
                xOffset + width - outline_width, 
                yOffset + height - outline_height, 
                color
            ); 
            if (ContainerScreenUtil.isMouseDraggingItem(containerScreen, itemstack)) {
            //if (ContainerScreenUtil.isMouseInArea(containerScreen, xOffset-1, yOffset-1, xOffset + width, yOffset + height)) {
                guiGraphics.fill(xOffset + outline_width, yOffset + outline_height, xOffset + width - outline_width, yOffset + height - height_offset - outline_height*2, color_transparent);
            } else {
                guiGraphics.fill(xOffset, yOffset, xOffset + outline_width, yOffset + height, color);
                guiGraphics.fill(xOffset + width - outline_width, yOffset, xOffset + width, yOffset + height, color);
                guiGraphics.fill(xOffset + outline_width, yOffset, xOffset + width - outline_width, yOffset + outline_height, color);
                guiGraphics.fill(xOffset + outline_width, yOffset + height - outline_height, xOffset + width - outline_width, yOffset + height, color);
            }
            return true;
        }
        
        return false;
    }
}