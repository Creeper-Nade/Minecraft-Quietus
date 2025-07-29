package com.minecraftquietus.quietus.client.screens.skill_tree;

import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public enum WidgetType {
    SQUARE("square_node");

    private final String shape;

    private WidgetType(String shape) {
        this.shape = shape;
    }

    protected ResourceLocation getLocation(boolean obtained) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/sprites/skill_tree/" + String.join("_", this.shape, obtained ? "obtained" : "unobtained") + ".png");
    }
}
