package com.quietus.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** A non-rendering tooltip row with an exact pixel height. */
public record TooltipSpacer(int height) implements TooltipComponent {
}
