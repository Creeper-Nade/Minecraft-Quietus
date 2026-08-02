package com.quietus.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The Void Orrery always renders with the enchanted-item glint.
 */
public class VoidOrreryItem extends Item {
    public VoidOrreryItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
