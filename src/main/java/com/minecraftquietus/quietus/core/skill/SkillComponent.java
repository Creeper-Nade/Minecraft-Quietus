package com.minecraftquietus.quietus.core.skill;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<Skill,Integer> skillMap = new HashMap<>();


    public boolean hasSkill(Skill skill) {
        return this.getLevel(skill) > 0;
    }

    public int getLevel(Skill skill) {
        return this.skillMap.getOrDefault(skill, 0);
    }

    public void addLevel(Skill skill, int amount) {
        int level = this.skillMap.getOrDefault(skill, 0);
        level += amount;
        this.skillMap.put(skill, Math.clamp(level, 0, skill.maxLevel()));
    }

    public void setLevel(Skill skill, int value) {
        this.skillMap.put(skill, Math.clamp(value, 0, skill.maxLevel()));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();

        this.skillMap.forEach((skill, level) -> {
            CompoundTag skill_tag = new CompoundTag();
            skill_tag.putString("id", skill.id.location().toString());
            skill_tag.putInt("level", level);
            list.add(skill_tag);
        });

        nbt.put(TAG_LIST_SKILLS, list);
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        this.skillMap.clear();
        ListTag list = nbt.getListOrEmpty(TAG_LIST_SKILLS);

        for (Tag tag : list) {
            if (tag instanceof CompoundTag tag2) {
                int level = tag2.getIntOr("level", 1);
                Skill skill = QuietusRegistries.SKILL_REGISTRY.getValue(ResourceLocation.parse(tag2.getStringOr("id", "none")));
                this.skillMap.put(skill, level);
            }
        }
    }

}
