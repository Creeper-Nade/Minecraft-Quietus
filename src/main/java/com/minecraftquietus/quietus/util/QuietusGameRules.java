package com.minecraftquietus.quietus.util;

import com.mojang.serialization.DynamicLike;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuietusGameRules {
    public static GameRules.Key<GameRules.BooleanValue> GHOST_MODE_ENABLED;
    public static GameRules.Key<GameRules.IntegerValue> TICKS_PER_DECAY;

    public static void Init()
    {
        GHOST_MODE_ENABLED = GameRules.register("enableGhostMode", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        TICKS_PER_DECAY = GameRules.register("ticksPerDecay", GameRules.Category.UPDATES, GameRules.IntegerValue.create(100));
    }


}
