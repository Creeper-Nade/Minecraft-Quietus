package com.minecraftquietus.quietus.util.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minecraftquietus.quietus.item.WeatheringItem;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ContainerUtil {
    public static ItemStack[] getSurroundingItems(int originIndex, int rowOffset, int horizontalOffset, Container container, int perRow, boolean includeOrigin) throws IndexOutOfBoundsException, ArithmeticException {
        if (rowOffset < 0 || horizontalOffset < 0) throw new IndexOutOfBoundsException("Row and horizontal offsets cannot be negative!");
        if (perRow == 0) throw new ArithmeticException("Items perRow cannot be 0");
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
}
