package com.minecraftquietus.quietus.util.client;

import static com.minecraftquietus.quietus.block.QuietusBlocks.register;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

public final class ContainerScreenUtil {
    
    public static boolean isMouseInArea(AbstractContainerScreen<?> containerScreen, int xMin, int yMin, int xMax, int yMax) {
        Minecraft minecraft = Minecraft.getInstance();
        int containerGUILeft = containerScreen.getGuiLeft();
        int containerGUITop = containerScreen.getGuiTop();
        // Get scaled mouse position
        Window window = minecraft.getWindow();
        double mouseX = minecraft.mouseHandler.xpos();
        double mouseY = minecraft.mouseHandler.ypos();
        int finalMouseX = (int)(mouseX * window.getGuiScaledWidth() / window.getScreenWidth()) - containerGUILeft;
        int finalMouseY = (int)(mouseY * window.getGuiScaledHeight() / window.getScreenHeight()) - containerGUITop;
        //LOGGER.info("calculated_x_of_mouse: {}, calculated_y_of_mouse: {} --- x_min:{}, y_min:{}", finalMouseX, finalMouseY, xMin, yMin);

        return (finalMouseX >= xMin && finalMouseX <= xMax && 
            finalMouseY >= yMin && finalMouseY <= yMax);
    }
    public static boolean isMouseDraggingItem(AbstractContainerScreen<?> containerScreen, ItemStack itemStack) {
        return containerScreen.getMenu().getCarried().equals(itemStack);
    }
}
