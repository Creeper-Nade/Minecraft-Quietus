package com.minecraftquietus.quietus.util.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeModifierValue (
    int value,
    AttributeModifier.Operation operation
) {
    public static final AttributeModifierValue NONE = new AttributeModifierValue(0, AttributeModifier.Operation.ADD_VALUE);
}
