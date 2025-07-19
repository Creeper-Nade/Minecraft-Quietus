package com.minecraftquietus.quietus.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.minecraftquietus.quietus.item.WeatheringItem;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ContainerUtil {
    public static ItemStack[] getSurroundingItems(int originIndex, int rowOffset, int horizontalOffset, Container container, int perRow, boolean includeOrigin) throws IndexOutOfBoundsException, ArithmeticException {
        if (rowOffset < 0 || horizontalOffset < 0) throw new IndexOutOfBoundsException("Row and horizontal offsets cannot be negative!");
        if (perRow == 0) throw new ArithmeticException("Items perRow cannot be 0");
        if (perRow < 0) return new ItemStack[0]; // has no valid rows, hence no surrounding items
        int containerSize = container.getContainerSize();
        List<ItemStack> surroundingItems = new ArrayList<>();
        for (int a = -(rowOffset); a <= rowOffset; a++) { // a for row offset
            for (int b = -(horizontalOffset); b <= horizontalOffset; b++) { // b for horizontal offset
                int index = originIndex + a*perRow + b;
                if (index >= 0 
                    && index < containerSize 
                    && Math.floorMod(originIndex, perRow) + b >= 0
                    && Math.floorMod(originIndex, perRow) + b < perRow) {
                        if (!includeOrigin && index != originIndex)
                            surroundingItems.add(container.getItem(index));
                    }
            }
        }
        return surroundingItems.toArray(new ItemStack[0]);
    }

    public static ItemStack[] getSurroundingItems(int originIndex, int rowOffset, int horizontalOffset, ItemContainerContents container, int perRow, boolean includeOrigin) throws IndexOutOfBoundsException, ArithmeticException {
        if (rowOffset < 0 || horizontalOffset < 0) throw new IndexOutOfBoundsException("Row and horizontal offsets cannot be negative!");
        if (perRow == 0) throw new ArithmeticException("Items perRow cannot be 0");
        if (perRow < 0) return new ItemStack[0]; // has no valid rows, hence no surrounding items
        int containerSize = container.getSlots();
        List<ItemStack> surroundingItems = new ArrayList<>();
        for (int a = -(rowOffset); a <= rowOffset; a++) { // a for row offset
            for (int b = -(horizontalOffset); b <= horizontalOffset; b++) { // b for horizontal offset
                int index = originIndex + a*perRow + b;
                if (index >= 0 
                    && index < containerSize 
                    && Math.floorMod(originIndex, perRow) + b >= 0
                    && Math.floorMod(originIndex, perRow) + b < perRow) {
                        if (!includeOrigin && index != originIndex)
                            surroundingItems.add(container.getStackInSlot(index));
                    }
            }
        }
        return surroundingItems.toArray(new ItemStack[0]);
    }
}
