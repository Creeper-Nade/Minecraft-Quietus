package com.quietus.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.*;

public class GameruleConflictManager {
    //The Gamerule conflict manager only supports detection of boolean gamerules currrently
    private static final Map<GameRule<Boolean>, Set<GameRule<Boolean>>> CONFLICT_MAP = new HashMap<>();

    public static void registerConflict(GameRule<Boolean> rule1, GameRule<Boolean> rule2) {
        CONFLICT_MAP.computeIfAbsent(rule1, k -> new HashSet<>()).add(rule2);
    }

    public static List<ConflictPair> getActiveConflicts(ServerLevel level) {
        List<ConflictPair> conflicts = new ArrayList<>();
        GameRules rules = level.getGameRules();

        for (Map.Entry<GameRule<Boolean>, Set<GameRule<Boolean>>> entry : CONFLICT_MAP.entrySet()) {
            GameRule<Boolean> primaryRule = entry.getKey();

            if (rules.get(primaryRule)) {
                for (GameRule<Boolean> conflictingRule : entry.getValue()) {
                    if (rules.get(conflictingRule)) {
                        conflicts.add(new ConflictPair(primaryRule, conflictingRule));
                    }
                }
            }
        }
        return conflicts;
    }

    public record ConflictPair(GameRule<?> firstRule, GameRule<?> secondRule) {}
}
