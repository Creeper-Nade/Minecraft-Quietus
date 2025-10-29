package com.minecraftquietus.quietus.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

import java.util.*;

public class GameruleConflictManager {
    //The Gamerule conflict manager only supports detection of boolean gamerules currrently
    private static final Map<GameRules.Key<GameRules.BooleanValue>, Set<GameRules.Key<GameRules.BooleanValue>>> CONFLICT_MAP = new HashMap<>();

    public static void registerConflict(GameRules.Key<GameRules.BooleanValue> rule1, GameRules.Key<GameRules.BooleanValue> rule2) {
        CONFLICT_MAP.computeIfAbsent(rule1, k -> new HashSet<>()).add(rule2);
    }

    public static List<ConflictPair> getActiveConflicts(ServerLevel level) {
        List<ConflictPair> conflicts = new ArrayList<>();
        GameRules rules = level.getGameRules();

        for (Map.Entry<GameRules.Key<GameRules.BooleanValue>, Set<GameRules.Key<GameRules.BooleanValue>>> entry : CONFLICT_MAP.entrySet()) {
            GameRules.Key<GameRules.BooleanValue> primaryRule = entry.getKey();

            if (rules.getBoolean(primaryRule)) {
                for (GameRules.Key<GameRules.BooleanValue> conflictingRule : entry.getValue()) {
                    if (rules.getBoolean(conflictingRule)) {
                        conflicts.add(new ConflictPair(primaryRule, conflictingRule));
                    }
                }
            }
        }
        return conflicts;
    }

    public record ConflictPair(GameRules.Key<?> firstRule, GameRules.Key<?> secondRule) {}
}
