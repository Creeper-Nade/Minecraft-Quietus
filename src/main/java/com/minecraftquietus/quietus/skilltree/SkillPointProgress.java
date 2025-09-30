package com.minecraftquietus.quietus.skilltree;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.advancements.CriterionProgress;

public class SkillPointProgress {
    private final Map<String, CriterionProgress> criteria;
    private int level;
    private Prerequisites.Requirements requirements;

    public SkillPointProgress() {
        this.criteria = new HashMap<>();
        this.level = 1;
    }
}
