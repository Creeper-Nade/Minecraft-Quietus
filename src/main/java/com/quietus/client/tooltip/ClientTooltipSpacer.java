package com.quietus.client.tooltip;

import com.quietus.item.tooltip.TooltipSpacer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public record ClientTooltipSpacer(TooltipSpacer spacer) implements ClientTooltipComponent {
    @Override
    public int getHeight(Font font) {
        return spacer.height();
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }
}
