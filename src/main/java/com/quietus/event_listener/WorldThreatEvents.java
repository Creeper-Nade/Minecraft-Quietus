package com.quietus.event_listener;

import com.quietus.Quietus;
import com.quietus.tags.QuietusTags;
import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Quietus.MODID)
public final class WorldThreatEvents {
    private static final long TICKS_PER_DAY = 24_000L;

    private WorldThreatEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level != level.getServer().overworld()) {
            return;
        }

        Holder<WorldClock> overworldClock = level.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        long currentDay = Math.floorDiv(level.getServer().clockManager().getTotalTicks(overworldClock), TICKS_PER_DAY);
        WorldThreatData.get(level).updateThroughDay(currentDay, level.getRandom());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Enemy)
                || attacker.getType().getTags().anyMatch(QuietusTags.Entity.BOSS_MONSTER::equals)) {
            return;
        }

        WorldThreatData threat = WorldThreatData.get(level);
        event.setNewDamage((float) (event.getNewDamage()
                * WorldThreatSystem.damageMultiplier(threat.getStage(), threat.getThreat())));
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        WorldThreatData threat = WorldThreatData.get(level);
        WorldThreatSystem.applyStageHealth(mob, threat.getStage(), false);
    }
}
