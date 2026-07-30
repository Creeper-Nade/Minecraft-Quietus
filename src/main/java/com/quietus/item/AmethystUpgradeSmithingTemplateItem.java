package com.quietus.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

import static com.quietus.Quietus.MODID;

public class AmethystUpgradeSmithingTemplateItem extends SmithingTemplateItem {
    private static final Component APPLIES_TO = Component.translatable(
            "item.quietus.smithing_template.amethyst_upgrade.applies_to"
    ).withStyle(ChatFormatting.BLUE);
    private static final Component INGREDIENTS = Component.translatable(
            "item.quietus.smithing_template.amethyst_upgrade.ingredients"
    ).withStyle(ChatFormatting.BLUE);
    private static final Component BASE_SLOT_DESCRIPTION = Component.translatable(
            "item.quietus.smithing_template.amethyst_upgrade.base_slot_description"
    );
    private static final Component ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
            "item.quietus.smithing_template.amethyst_upgrade.additions_slot_description"
    );

    private static final List<Identifier> BASE_SLOT_ICONS = List.of(
            Identifier.withDefaultNamespace("container/slot/helmet"),
            Identifier.withDefaultNamespace("container/slot/chestplate"),
            Identifier.withDefaultNamespace("container/slot/leggings"),
            Identifier.withDefaultNamespace("container/slot/boots")
    );
    private static final List<Identifier> ADDITION_SLOT_ICONS = List.of(
            Identifier.fromNamespaceAndPath(MODID, "container/slot/amethyst_resonator")
    );

    public AmethystUpgradeSmithingTemplateItem(Item.Properties properties) {
        super(
                APPLIES_TO,
                INGREDIENTS,
                BASE_SLOT_DESCRIPTION,
                ADDITIONS_SLOT_DESCRIPTION,
                BASE_SLOT_ICONS,
                ADDITION_SLOT_ICONS,
                properties
        );
    }
}
