package com.quietus.util;

import com.quietus.client.handler.ClientPayloadHandler;
import com.quietus.core.skill.Skill;
import com.quietus.core.skill.SkillComponent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SkillUtil {

    public static SkillComponent getSkills(Player player) {
        return player.getData(QuietusAttachments.SKILL_ATTACHMENT);
    }

    public static Number getSkillLevel(Player player, Skill skill, String source) {
        if (player == null || skill == null) {
            return 0;
        }
        return getSkills(player).getLevel(skill, source);
    }

    public static int getIntSkillLevel(Player player, Skill skill, String source) {
        return getSkillLevel(player, skill, source).intValue();
    }

    public static float getFloatSkillLevel(Player player, Skill skill, String source) {
        return getSkillLevel(player, skill, source).floatValue();
    }

    public static double getDoubleSkillLevel(Player player, Skill skill, String source) {
        return getSkillLevel(player, skill, source).doubleValue();
    }
    
    public static Number getTotalSkillLevel(Player player, Skill skill) {
        if (player == null || skill == null) {
            return 0;
        }
        if (player.level().isClientSide()) {
            double lvl = ClientPayloadHandler.getSkillLevel(skill.getId());
            return switch (skill.getType()) {
                case INT -> (int) Math.round(lvl);
                case FLOAT -> (float) lvl;
                case DOUBLE -> lvl;
            };
        }
        return getSkills(player).getTotalLevel(skill);
    }

    public static int getIntTotalSkillLevel(Player player, Skill skill) {
        return getTotalSkillLevel(player, skill).intValue();
    }

    public static float getFloatTotalSkillLevel(Player player, Skill skill) {
        return getTotalSkillLevel(player, skill).floatValue();
    }

    public static double getDoubleTotalSkillLevel(Player player, Skill skill) {
        if (player == null || skill == null) {
            return 0.0d;
        }
        if (player.level().isClientSide()) {
            return ClientPayloadHandler.getSkillLevel(skill.getId());
        }
        return getSkills(player).getTotalLevel(skill).doubleValue();
    }

    public static java.util.Map<String, Number> getSkillSourceLevels(Player player, Skill skill) {
        if (player == null || skill == null) {
            return java.util.Map.of();
        }
        return getSkills(player).getSourceLevels(skill);
    }

    public static void addSkillLevel(Player player, Skill skill, Number amount, String source) {
        getSkills(player).addLevel(skill, amount, source);
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerClientPacketDistributor.sendSkillUpdatePacketToPlayer(serverPlayer, skill, getTotalSkillLevel(serverPlayer, skill));
        }
    }
    
    public static void setSkillLevel(Player player, Skill skill, Number value, String source) {
        getSkills(player).setLevel(skill, value, source);
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerClientPacketDistributor.sendSkillUpdatePacketToPlayer(serverPlayer, skill, getTotalSkillLevel(serverPlayer, skill));
        }
    }

    public static Number getMaxSkillLevel(Skill skill) {
        return skill.maxLevel();
    }
}
