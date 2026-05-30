package com.quietus.util;

import com.quietus.core.skill.Skill;
import com.quietus.core.skill.SkillComponent;

import net.minecraft.world.entity.player.Player;

public class SkillUtil {

    public static SkillComponent getSkills(Player player) {
        return player.getData(QuietusAttachments.SKILL_ATTACHMENT);
    }

    public static int getSkillLevel(Player player, Skill skill, String source) {
        return getSkills(player).getLevel(skill, source);
    }
    
    public static int getTotalSkillLevel(Player player, Skill skill) {
        return getSkills(player).getTotalLevel(skill);
    }

    public static void addSkillLevel(Player player, Skill skill, int amount, String source) {
        getSkills(player).addLevel(skill, amount, source);
    }
    
    public static void setSkillLevel(Player player, Skill skill, int value, String source) {
        getSkills(player).setLevel(skill, value, source);
    }

    public static int getMaxSkillLevel(Skill skill) {
        return skill.maxLevel();
    }
}
