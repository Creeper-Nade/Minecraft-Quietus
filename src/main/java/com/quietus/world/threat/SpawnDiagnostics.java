package com.quietus.world.threat;

import com.mojang.logging.LogUtils;
import com.quietus.Quietus;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/** Aggregates natural-spawning diagnostics without logging every spawn attempt. */
@EventBusSubscriber(modid = Quietus.MODID)
public final class SpawnDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int REPORT_INTERVAL_TICKS = 400;

    private static long hostileChecks;
    private static long extraHostileAttempts;
    private static long zombiePackSelections;
    private static long vanillaPackMinimumTotal;
    private static long vanillaPackMaximumTotal;
    private static long adjustedPackMinimumTotal;
    private static long adjustedPackMaximumTotal;
    private static long naturalZombieFinalizations;
    private static long zombieClusterCapChecks;
    private static long vanillaClusterCapTotal;
    private static long adjustedClusterCapTotal;
    private static long zombieRetryPacks;
    private static long zombieRetryTargetTotal;
    private static long zombiePacksReachingTarget;
    private static final long[] jockeyRolls = new long[JockeyType.values().length];
    private static final long[] jockeyRollsPassed = new long[JockeyType.values().length];
    private static final long[] jockeysCreated = new long[JockeyType.values().length];
    private static final long[] cumulativeJockeyRolls = new long[JockeyType.values().length];
    private static final long[] cumulativeJockeyRollsPassed = new long[JockeyType.values().length];
    private static final long[] cumulativeJockeysCreated = new long[JockeyType.values().length];

    private SpawnDiagnostics() {
    }

    public static synchronized void recordHostileAttempt(boolean extraAttemptGranted) {
        hostileChecks++;
        if (extraAttemptGranted) {
            extraHostileAttempts++;
        }
    }

    public static synchronized void recordZombiePackSelection(
            int vanillaMin, int vanillaMax, int adjustedMin, int adjustedMax) {
        zombiePackSelections++;
        vanillaPackMinimumTotal += vanillaMin;
        vanillaPackMaximumTotal += vanillaMax;
        adjustedPackMinimumTotal += adjustedMin;
        adjustedPackMaximumTotal += adjustedMax;
    }

    public static synchronized void recordNaturalZombieFinalization() {
        naturalZombieFinalizations++;
    }

    public static synchronized void recordZombieClusterCap(int vanillaCap, int adjustedCap) {
        zombieClusterCapChecks++;
        vanillaClusterCapTotal += vanillaCap;
        adjustedClusterCapTotal += adjustedCap;
    }

    public static synchronized void recordZombieRetryPack(int target) {
        zombieRetryPacks++;
        zombieRetryTargetTotal += target;
    }

    public static synchronized void recordZombiePackReachedTarget() {
        zombiePacksReachingTarget++;
    }

    public static synchronized void recordJockeyRoll(JockeyType type) {
        jockeyRolls[type.ordinal()]++;
        cumulativeJockeyRolls[type.ordinal()]++;
    }

    public static synchronized void recordJockeyRollPassed(JockeyType type) {
        jockeyRollsPassed[type.ordinal()]++;
        cumulativeJockeyRollsPassed[type.ordinal()]++;
    }

    public static synchronized void recordJockeyCreated(JockeyType type) {
        jockeysCreated[type.ordinal()]++;
        cumulativeJockeysCreated[type.ordinal()]++;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % REPORT_INTERVAL_TICKS != 0) {
            return;
        }

        logAndReset(WorldThreatData.get(server).getThreat());
    }

    private static synchronized void logAndReset(double threat) {
        if (hostileChecks == 0 && zombiePackSelections == 0 && naturalZombieFinalizations == 0) {
            return;
        }

        double extraRate = percentage(extraHostileAttempts, hostileChecks);
        double expectedExtraRate = WorldThreatSystem.extraHostilePackAttemptChance(threat) * 100.0D;
        double finalizedPerSelection = ratio(naturalZombieFinalizations, zombiePackSelections);

        LOGGER.info(
                "[Quietus spawn diagnostics] threat={} hostile calls={} extra attempts={} ({}%, expected {}%); "
                        + "zombie pack selections={} average vanilla range={}-{} average adjusted range={}-{}; "
                        + "natural zombie finalizations={} ({} per selected pack); "
                        + "average zombie cluster cap={}->{} ({} checks); "
                        + "retry packs={} average target={} targets reached={}",
                format(threat), hostileChecks, extraHostileAttempts, format(extraRate), format(expectedExtraRate),
                zombiePackSelections,
                format(average(vanillaPackMinimumTotal, zombiePackSelections)),
                format(average(vanillaPackMaximumTotal, zombiePackSelections)),
                format(average(adjustedPackMinimumTotal, zombiePackSelections)),
                format(average(adjustedPackMaximumTotal, zombiePackSelections)),
                naturalZombieFinalizations, format(finalizedPerSelection),
                format(average(vanillaClusterCapTotal, zombieClusterCapChecks)),
                format(average(adjustedClusterCapTotal, zombieClusterCapChecks)),
                zombieClusterCapChecks, zombieRetryPacks,
                format(average(zombieRetryTargetTotal, zombieRetryPacks)), zombiePacksReachingTarget);

        LOGGER.info(
                "[Quietus jockey diagnostics] threat={} chances chicken-roll={}%, spider={}%, camel-husk={}%, "
                        + "zombie-nautilus={}%, zombie-horseman={}%; "
                        + "rolls/results chicken={}/{}, spider={}/{}, camel-husk={}/{}, "
                        + "zombie-nautilus={}/{}, zombie-horseman={}/{}/{} (rolls/passed/created); "
                        + "cumulative horseman={}/{}/{}; "
                        + "cumulative results chicken={}, spider={}, camel-husk={}, zombie-nautilus={}",
                format(threat),
                format(WorldThreatSystem.chickenJockeyRollChance(threat) * 100.0D),
                format(WorldThreatSystem.spiderJockeyChance(threat) * 100.0D),
                format(WorldThreatSystem.camelHuskJockeyChance(threat) * 100.0D),
                format(WorldThreatSystem.zombieNautilusJockeyChance(threat) * 100.0D),
                format(WorldThreatSystem.zombieHorsemanChance(threat) * 100.0D),
                jockeyRolls[JockeyType.CHICKEN.ordinal()], jockeysCreated[JockeyType.CHICKEN.ordinal()],
                jockeyRolls[JockeyType.SPIDER.ordinal()], jockeysCreated[JockeyType.SPIDER.ordinal()],
                jockeyRolls[JockeyType.CAMEL_HUSK.ordinal()], jockeysCreated[JockeyType.CAMEL_HUSK.ordinal()],
                jockeyRolls[JockeyType.ZOMBIE_NAUTILUS.ordinal()], jockeysCreated[JockeyType.ZOMBIE_NAUTILUS.ordinal()],
                jockeyRolls[JockeyType.ZOMBIE_HORSEMAN.ordinal()],
                jockeyRollsPassed[JockeyType.ZOMBIE_HORSEMAN.ordinal()],
                jockeysCreated[JockeyType.ZOMBIE_HORSEMAN.ordinal()],
                cumulativeJockeyRolls[JockeyType.ZOMBIE_HORSEMAN.ordinal()],
                cumulativeJockeyRollsPassed[JockeyType.ZOMBIE_HORSEMAN.ordinal()],
                cumulativeJockeysCreated[JockeyType.ZOMBIE_HORSEMAN.ordinal()],
                cumulativeJockeysCreated[JockeyType.CHICKEN.ordinal()],
                cumulativeJockeysCreated[JockeyType.SPIDER.ordinal()],
                cumulativeJockeysCreated[JockeyType.CAMEL_HUSK.ordinal()],
                cumulativeJockeysCreated[JockeyType.ZOMBIE_NAUTILUS.ordinal()]);

        hostileChecks = 0;
        extraHostileAttempts = 0;
        zombiePackSelections = 0;
        vanillaPackMinimumTotal = 0;
        vanillaPackMaximumTotal = 0;
        adjustedPackMinimumTotal = 0;
        adjustedPackMaximumTotal = 0;
        naturalZombieFinalizations = 0;
        zombieClusterCapChecks = 0;
        vanillaClusterCapTotal = 0;
        adjustedClusterCapTotal = 0;
        zombieRetryPacks = 0;
        zombieRetryTargetTotal = 0;
        zombiePacksReachingTarget = 0;
        java.util.Arrays.fill(jockeyRolls, 0L);
        java.util.Arrays.fill(jockeyRollsPassed, 0L);
        java.util.Arrays.fill(jockeysCreated, 0L);
    }

    private static double average(long total, long count) {
        return count == 0 ? 0.0D : (double) total / count;
    }

    private static double ratio(long numerator, long denominator) {
        return denominator == 0 ? 0.0D : (double) numerator / denominator;
    }

    private static double percentage(long numerator, long denominator) {
        return ratio(numerator, denominator) * 100.0D;
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    public enum JockeyType {
        CHICKEN,
        SPIDER,
        CAMEL_HUSK,
        ZOMBIE_NAUTILUS,
        ZOMBIE_HORSEMAN
    }
}
