package com.minecraftquietus.quietus.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.minecraftquietus.quietus.item.QuietusComponents;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public final class ItemStackUtil {
    
    public static boolean isSameItemSameComponentsExceptDecay(ItemStack stack, ItemStack other) {
        if (!ItemStack.isSameItem(stack, other)) {
            return false;
        } else {
            DataComponentMap stack_components = stack.getComponents();
            DataComponentMap other_components = other.getComponents();
            Set<DataComponentType<?>> all_component_types = new HashSet<>(stack_components.keySet());
            all_component_types.addAll(other_components.keySet());
            for (DataComponentType<?> type : all_component_types) {
                if (type == QuietusComponents.DECAY.get()) { // skip decay value comparison
                    continue;
                } else if (!Objects.equals(stack.get(type), other.get(type))) {
                    return false;
                }
            }
            return true;
        }
    }
}
