package com.quietus.event_listener;

import static com.quietus.Quietus.MODID;

import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.SpawnDiagnostics;
import com.quietus.world.threat.WorldThreatSystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.SpawnClusterSizeEvent;

@EventBusSubscriber(modid = MODID)
public final class SpawnEvent {
    /** Prevents mobs created here from recursively triggering variant generation. */
    private static final ThreadLocal<Boolean> SPAWNING_INTERNAL_MOB = ThreadLocal.withInitial(() -> false);

    private SpawnEvent() {
    }

    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        applyStageHealth(event);

        if (SPAWNING_INTERNAL_MOB.get() || event.getSpawnType() != EntitySpawnReason.NATURAL) {
            return;
        }

        EntityType<?> entityType = event.getEntity().getType();
        if (isZombieFamily(entityType)) {
            SpawnDiagnostics.recordNaturalZombieFinalization();
        }
        if (entityType == EntityType.SKELETON && replaceSkeletonWithSelectedVariant(event)) {
            return;
        }
    }

    /** Raises the natural-spawner cluster cap so the enlarged zombie pack can actually be completed. */
    @SubscribeEvent
    public static void onSpawnClusterSize(SpawnClusterSizeEvent event) {
        EntityType<?> type = event.getEntity().getType();
        if (type != EntityType.ZOMBIE
                && type != EntityType.HUSK
                && type != EntityType.DROWNED
                && type != EntityType.ZOMBIE_VILLAGER) {
            return;
        }

        Level level = event.getEntity().level();
        WorldThreatData threat = WorldThreatData.get(level.getServer());
        int vanillaCap = event.getSize();
        int adjustedCap = vanillaCap + WorldThreatSystem.zombiePackSizeBonus(threat.getThreat());
        event.setSize(adjustedCap);
        SpawnDiagnostics.recordZombieClusterCap(vanillaCap, adjustedCap);
    }

    private static boolean isZombieFamily(EntityType<?> type) {
        return type == EntityType.ZOMBIE
                || type == EntityType.HUSK
                || type == EntityType.DROWNED
                || type == EntityType.ZOMBIE_VILLAGER;
    }

    private static void applyStageHealth(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        WorldThreatData threat = WorldThreatData.get(event.getLevel().getLevel().getServer());
        WorldThreatSystem.applyStageHealth(mob, threat.getStage(), true);
    }

    /** Replaces a natural vanilla skeleton only after its vanilla spawn checks have passed. */
    private static boolean replaceSkeletonWithSelectedVariant(FinalizeSpawnEvent event) {
        Level level = event.getLevel().getLevel();
        WorldThreatData threat = WorldThreatData.get(level.getServer());
        EntityType<?> selectedType = WorldThreatSystem.selectSkeletonVariant(
                threat.getStage(), threat.getThreat(), level.getRandom());
        if (selectedType == EntityType.SKELETON) {
            return false;
        }

        Entity createdEntity = selectedType.create(level, EntitySpawnReason.NATURAL);
        if (!(createdEntity instanceof Mob replacement)) {
            return false;
        }

        replacement.snapTo(
                event.getX(), event.getY(), event.getZ(),
                event.getEntity().getYRot(), event.getEntity().getXRot());

        SPAWNING_INTERNAL_MOB.set(true);
        try {
            EventHooks.finalizeMobSpawn(
                    replacement,
                    event.getLevel(),
                    event.getDifficulty(),
                    EntitySpawnReason.NATURAL,
                    event.getSpawnData());
            if (replacement.isSpawnCancelled()) {
                replacement.discard();
                return false;
            }

            boolean added = level.addFreshEntity(replacement);
            if (added) {
                event.setSpawnCancelled(true);
            } else {
                replacement.discard();
            }
            return added;
        } finally {
            SPAWNING_INTERNAL_MOB.set(false);
        }
    }

}
