package com.minecraftquietus.quietus.skilltree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


public class SkillCategory {
    private final Prerequisites prerequisites;

    public SkillCategory(Prerequisites prerequisites) {
        this.prerequisites = prerequisites;
    }

    public static final Codec<SkillCategory> CODEC = RecordCodecBuilder.create(
        (instance) -> instance.group(
            Prerequisites.CODEC.optionalFieldOf("prerequisites",Prerequisites.EMPTY).forGetter(SkillCategory::getPrerequisites)
        ).apply(instance, SkillCategory::new)
    );

    public Prerequisites getPrerequisites() {
        return this.prerequisites;
    }

}
