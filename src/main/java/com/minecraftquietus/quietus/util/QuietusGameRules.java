package com.minecraftquietus.quietus.util;

import com.mojang.serialization.DynamicLike;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuietusGameRules {
    public static GameRule<Boolean> GHOST_MODE_ENABLED;
    public static GameRule<Boolean> FRAGMENT_SPAWNING;
    public static GameRule<Integer> TICKS_PER_DECAY;

    public static void Init()
    {
        GHOST_MODE_ENABLED = GameRules.registerBoolean("doDeathSpectating", GameRuleCategory.PLAYER, true);
        FRAGMENT_SPAWNING = GameRules.registerBoolean("keepInventoryPartially", GameRuleCategory.PLAYER, true);
        TICKS_PER_DECAY = GameRules.registerInteger("ticksPerDecay", GameRuleCategory.UPDATES, 100,0);
    }


}
