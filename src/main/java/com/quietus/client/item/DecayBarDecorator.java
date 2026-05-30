package com.quietus.client.item;

import org.slf4j.Logger;

import com.quietus.item.QuietusComponents;
import com.quietus.item.component.CanDecay;
import com.quietus.util.client.ContainerScreenUtil;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public boolean render(GuiGraphicsExtractor GuiGraphicsExtractor, Font font, ItemStack itemstack, int offsetX, int offsetY) {
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
        /* int color = 0x66000038 | Mth.hsvToRgb((float)Math.round(fraction*2.5) / 2.5f * 0.33f, 1.0f, 1.0f); 
        int color_transparent = 0x48000098 | Mth.hsvToRgb((float)Math.round(fraction*3) / 3.0f * 0.33f, 1.0f, 1.0f); // green (normal) -> red (decayed) */
        
        GuiGraphicsExtractor.fill( 
            offsetX + outline_width, 
            offsetY + height - height_offset - outline_height*2, 
            offsetX + width - outline_width, 
            offsetY + height - outline_height, 
            color
        ); 
        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) { // only in container GUI
            if (ContainerScreenUtil.isMouseDraggingItem(containerScreen, itemstack)) {
            //if (ContainerScreenUtil.isMouseInArea(containerScreen, offsetX-1, offsetY-1, offsetX + width, offsetY + height)) {
                GuiGraphicsExtractor.fill(offsetX + outline_width, offsetY + outline_height, offsetX + width - outline_width, offsetY + height - height_offset - outline_height*2, color_transparent);
            } else {
                GuiGraphicsExtractor.fill(offsetX, offsetY, offsetX + outline_width, offsetY + height, color);
                GuiGraphicsExtractor.fill(offsetX + width - outline_width, offsetY, offsetX + width, offsetY + height, color);
                GuiGraphicsExtractor.fill(offsetX + outline_width, offsetY, offsetX + width - outline_width, offsetY + outline_height, color);
                GuiGraphicsExtractor.fill(offsetX + outline_width, offsetY + height - outline_height, offsetX + width - outline_width, offsetY + height, color);
            }
            return true;
        }
        
        return false;
    }
}