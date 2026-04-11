package com.minecraftquietus.quietus.core.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import com.minecraftquietus.quietus.core.QuietusRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class SkillComponent implements ValueIOSerializable {
    public static final String TAG_LIST_SKILLS = "skills";

    private final Map<Skill,Map<String,Integer>> skillMap = new HashMap<>();


    public boolean hasSkill(Skill skill) {
        return this.getTotalLevel(skill) > 0;
    }

    public int getLevel(Skill skill, String source) {
        return this.skillMap.containsKey(skill) ?
            this.skillMap.get(skill).getOrDefault(source, 0)
            : 0;
    }
    public int getTotalLevel(Skill skill) {
        if (this.skillMap.containsKey(skill)) {
            MutableInt level = new MutableInt(0);
            this.skillMap.get(skill).values().forEach((i) -> level.add(i));
            return level.toInteger();
        } else {
            return 0;
        }
    }

    public void addLevel(Skill skill, int amount, String source) {
        int level = this.getLevel(skill, source);
        level += amount;
        level = Math.clamp(level, 0, skill.maxLevel());
        if (this.hasSkill(skill)) {
            this.skillMap.get(skill).put(source, level);
        } else {
            this.skillMap.put(skill, Map.of(source, level));
        }
    }

    public void setLevel(Skill skill, int value, String source) {
        if (this.hasSkill(skill)) {
            this.skillMap.get(skill).put(source, value);
        } else {
            this.skillMap.put(skill, Map.of(source, value));
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("skills");

        this.skillMap.forEach((skill, levelsMap) -> {
            ValueOutput skill_tag = list.addChild();
            skill_tag.putString("id", skill.id.identifier().toString());
            ValueOutput.ValueOutputList list2 = skill_tag.childrenList("levels");
            this.skillMap.get(skill).forEach((source, level) -> {
                ValueOutput levels_tag = list2.addChild();
                levels_tag.putString("source", source);
                levels_tag.putInt("level", level);
            });
        });
    }

    @Override
    public void deserialize(ValueInput input) {
        this.skillMap.clear();

        ValueInput.ValueInputList list = input.childrenListOrEmpty("skills");
        for (ValueInput tag : list) {
            Skill skill = QuietusRegistries.SKILL_REGISTRY.getValue(Identifier.parse(tag.getStringOr("id", "quietus:none")));
            Map<String,Integer> levelsMap = new HashMap<>();
            ValueInput.ValueInputList list2 = tag.childrenListOrEmpty("levels");
            for (ValueInput tag2 : list2) {
                String source = tag2.getStringOr("source", "none");
                int level = tag2.getIntOr("level", 1);
                levelsMap.put(source, level);
            }
            this.skillMap.put(skill, levelsMap);
        }
    }

}
