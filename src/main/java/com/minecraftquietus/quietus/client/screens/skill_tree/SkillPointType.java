package com.minecraftquietus.quietus.client.screens.skill_tree;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import static com.minecraftquietus.quietus.Quietus.MODID;

import com.mojang.serialization.Codec;

public enum SkillPointType implements StringRepresentable {
    SQUARE(0, "square_node");

    private final int index;
    private final String shape;

    private SkillPointType(int index, String shape) {
        this.index = index;
        this.shape = shape;
    }

    public static final Codec<SkillPointType> CODEC = StringRepresentable.fromEnum(SkillPointType::values);

    

    protected ResourceLocation getLocation(boolean obtained) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/sprites/skill_tree/" + String.join("_", this.shape, obtained ? "obtained" : "unobtained") + ".png");
    }

    public int index() {
        return this.index;
    }
    public String shape() {
        return this.shape;
    }

    @Override
    public String getSerializedName() {
        return this.shape;
    }
}
