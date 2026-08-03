package com.quietus.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Marker for the client-rendered, expandable magic weapon control guide. */
public record MagicWeaponControlsTooltip() implements TooltipComponent {
    public static final MagicWeaponControlsTooltip INSTANCE = new MagicWeaponControlsTooltip();
}
