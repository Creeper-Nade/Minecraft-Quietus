package com.quietus.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.quietus.Quietus.MODID;

public class QuietusGameRules {
    public static GameRule<Boolean> GHOST_MODE_ENABLED;
    public static GameRule<Boolean> FRAGMENT_SPAWNING;
    public static GameRule<Integer> TICKS_PER_DECAY;
    
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        // Safe check to verify we are executing on the Game Rules registry lifecycle
        if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
            
            // Appending the namespace directly inside the identifier string bypasses default lowercase rules
            GHOST_MODE_ENABLED = GameRules.registerBoolean(MODID + ":do_death_spectating", GameRuleCategory.PLAYER, true);
            FRAGMENT_SPAWNING = GameRules.registerBoolean(MODID + ":keep_inventory_partially", GameRuleCategory.PLAYER, true);
            TICKS_PER_DECAY = GameRules.registerInteger(MODID + ":ticks_per_decay", GameRuleCategory.UPDATES, 100, 0);
            
        }
    }


}
