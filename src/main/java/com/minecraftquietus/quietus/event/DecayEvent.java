package com.minecraftquietus.quietus.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * DecayEvent fires when an item is decaying to a new item, fired in {@link com.minecraftquietus.quietus.event_listener.WeatheringHandler}
 * Is {@link ICancellableEvent}. When canceled, the item will not be decaying this tick. 
 * However if its decay component remains unchanged, it will likely still decay the next tick.
 * Fires only on logical server side.
 */
public class DecayEvent extends Event implements ICancellableEvent {
    private final ItemStack original;
    private final ItemStack convertInto;
    private final Level level;
    private ItemStack finalItem;

    public DecayEvent(ItemStack original, ItemStack convertInto, Level level) {
        this.original = original;
        this.convertInto = convertInto;
        this.level = level;
        this.finalItem = convertInto;
    }
    
    public ItemStack getOriginalItem() {
        return this.original;
    }

    public ItemStack getItemConvertInto() {
        return this.convertInto;
    }

    public Level getLevel() {
        return this.level;
    }

    public void setItemConvertInto(ItemStack itemstack) {
        this.finalItem = itemstack;
    }

    public ItemStack getFinalItem() {
        return this.finalItem;
    }


    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
