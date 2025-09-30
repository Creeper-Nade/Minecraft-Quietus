package com.minecraftquietus.quietus.skilltree;

import java.util.HashMap;
import java.util.Map;

public class SkillCategoryProgress {
    private final Map<SkillPoint, SkillPointProgress> progress;

    public SkillCategoryProgress(){
        this.progress = new HashMap<>();
    }
}
