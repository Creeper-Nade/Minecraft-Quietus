package com.minecraftquietus.quietus.util;

import com.minecraftquietus.quietus.core.skill.Skill;
import com.minecraftquietus.quietus.core.skill.SkillComponent;

import net.minecraft.world.entity.player.Player;

public final class SkillUtil {

    public static SkillComponent getSkills(Player player) {
        return player.getData(QuietusAttachments.SKILL_ATTACHMENT);
    }

    public static int getSkillLevel(Player player, Skill skill) {
        return getSkills(player).getLevel(skill);
    }

    public static void addSkillLevel(Player player, Skill skill, int amount) {
        getSkills(player).addLevel(skill, amount);
    }
    
    public static void setSkillLevel(Player player, Skill skill, int value) {
        getSkills(player).setLevel(skill, value);
    }

    public static int getMaxSkillLevel(Skill skill) {
        return skill.maxLevel();
    }
}
