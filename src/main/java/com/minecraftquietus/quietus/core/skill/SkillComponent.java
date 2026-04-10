package com.minecraftquietus.quietus.core.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.UnknownNullability;

import com.minecraftquietus.quietus.core.QuietusRegistries;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class SkillComponent implements INBTSerializable<CompoundTag> {
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
    public @UnknownNullability CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();

        ListTag list = new ListTag();
        this.skillMap.forEach((skill, levelsMap) -> {
            CompoundTag skill_tag = new CompoundTag();
            skill_tag.putString("id", skill.id.location().toString());
            ListTag list2 = new ListTag();
            this.skillMap.get(skill).forEach((source, level) -> {
                CompoundTag levels_tag = new CompoundTag();
                levels_tag.putString("source", source);
                levels_tag.putInt("level", level);
                list2.add(levels_tag);
            });
            skill_tag.put("levels", list2);
            list.add(skill_tag);
        });

        nbt.put(TAG_LIST_SKILLS, list);
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        this.skillMap.clear();

        ListTag list = nbt.getListOrEmpty(TAG_LIST_SKILLS);
        for (Tag tag : list) { if (tag instanceof CompoundTag tag2) {
            Skill skill = QuietusRegistries.SKILL_REGISTRY.getValue(ResourceLocation.parse(tag2.getStringOr("id", "quietus:none")));
            Map<String,Integer> levelsMap = new HashMap<>();
            ListTag list2 = tag2.getListOrEmpty("levels");
            for (Tag tag3 : list2) { if (tag3 instanceof CompoundTag tag4) {
                String source = tag4.getStringOr("source", "none");
                int level = tag4.getIntOr("level", 1);
                levelsMap.put(source, level);
            }}
            this.skillMap.put(skill, levelsMap);
        }}
    }

}
