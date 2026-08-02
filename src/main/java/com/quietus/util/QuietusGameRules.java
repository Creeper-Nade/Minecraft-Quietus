package com.quietus.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.quietus.Quietus.MODID;

public class QuietusGameRules {
    public static GameRule<Boolean> GHOST_MODE_ON_DEATH;
    public static GameRule<Boolean> PLAYER_FRAGMENT_ON_DEATH;
    public static GameRule<Boolean> REVAMPED_INVULNERABILITY_FRAMES;
    public static GameRule<Integer> TICKS_PER_DECAY;

    public static boolean useRevampedInvulnerabilityFrames(Level level) {
        return REVAMPED_INVULNERABILITY_FRAMES == null
                || level.getServer() == null
                || level.getServer().getGameRules().get(REVAMPED_INVULNERABILITY_FRAMES);
    }
    
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        // Safe check to verify we are executing on the Game Rules registry lifecycle
        if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
            
            // Appending the namespace directly inside the identifier string bypasses default lowercase rules
            GHOST_MODE_ON_DEATH = GameRules.registerBoolean(MODID + ":ghost_mode_on_death", GameRuleCategory.PLAYER, true);
            PLAYER_FRAGMENT_ON_DEATH = GameRules.registerBoolean(MODID + ":player_fragment_on_death", GameRuleCategory.PLAYER, true);
            REVAMPED_INVULNERABILITY_FRAMES = GameRules.registerBoolean(
                    MODID + ":revamped_invulnerability_frames", GameRuleCategory.MOBS, true);
            TICKS_PER_DECAY = GameRules.registerInteger(MODID + ":ticks_per_decay", GameRuleCategory.UPDATES, 100, 0);
            
        }
    }


}
