package com.quietus.item;

import com.quietus.item.tooltip.WeaponStatTooltips;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;

import java.util.function.Consumer;

/** Custom item legends rendered after vanilla component tooltips. */
public final class QuietusTooltipAppenders {
    private QuietusTooltipAppenders() {
    }

    public static final TooltipAppender ITEM_LEGENDS = (stack, context, display, player, flag, builder) -> {
        if (!stack.has(QuietusComponents.ITEM_LEGEND.get())
                || !display.shows(QuietusComponents.ITEM_LEGEND.get())) {
            return;
        }
        if (stack.is(QuietusItems.AMETHYST_STAFF.get())) {
            WeaponStatTooltips.appendProjectileStats(stack, player, flag, builder);
            appendLegend(builder, "tooltip.quietus.amethyst_staff.", 6, 8);
        } else if (stack.is(QuietusItems.AMETHYST_HELMET.get())
                || stack.is(QuietusItems.AMETHYST_CHESTPLATE.get())
                || stack.is(QuietusItems.AMETHYST_LEGGINGS.get())
                || stack.is(QuietusItems.AMETHYST_BOOTS.get())) {
            appendLegend(builder, "tooltip.quietus.amethyst_armor.", 4);
        }
    };

    private static void appendLegend(Consumer<Component> builder, String translationPrefix, int lineCount) {
        appendLegend(builder, translationPrefix, 1, lineCount);
    }

    private static void appendLegend(Consumer<Component> builder, String translationPrefix, int firstLine, int lastLine) {
        builder.accept(CommonComponents.EMPTY);
        for (int i = firstLine; i <= lastLine; i++) {
            builder.accept(Component.translatable(translationPrefix + i));
        }
    }
}
