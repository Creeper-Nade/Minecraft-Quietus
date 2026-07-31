package com.quietus.world.threat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.quietus.Quietus;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistent, world-wide state for the dynamic difficulty system. */
public final class WorldThreatData extends SavedData {
    public static final int MIN_STAGE = 0;
    public static final int MAX_STAGE = 3;
    public static final int MIN_THREAT = 0;
    public static final int MAX_THREAT = 100;
    public static final double DEFAULT_VOLATILITY = 2.0D;
    private static final double VOLATILITY_GROWTH_PER_DAY = 0.5D;
    private static final int MAX_CATCH_UP_DAYS = 10_000;

    private static final Codec<WorldThreatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("stage", MIN_STAGE).forGetter(WorldThreatData::getStage),
            Codec.DOUBLE.optionalFieldOf("threat", (double) MIN_THREAT).forGetter(WorldThreatData::getThreat),
            Codec.DOUBLE.optionalFieldOf("volatility", DEFAULT_VOLATILITY).forGetter(WorldThreatData::getVolatility),
            Codec.LONG.optionalFieldOf("last_updated_day", -1L).forGetter(WorldThreatData::getLastUpdatedDay)
    ).apply(instance, WorldThreatData::new));

    public static final SavedDataType<WorldThreatData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Quietus.MODID, "world_threat"),
            WorldThreatData::new,
            CODEC
    );

    private int stage;
    private double threat;
    private double volatility;
    private long lastUpdatedDay;

    public WorldThreatData() {
        this(MIN_STAGE, MIN_THREAT, DEFAULT_VOLATILITY, -1L);
    }

    private WorldThreatData(int stage, double threat, double volatility, long lastUpdatedDay) {
        this.stage = Mth.clamp(stage, MIN_STAGE, MAX_STAGE);
        this.threat = Mth.clamp(threat, MIN_THREAT, MAX_THREAT);
        this.volatility = Mth.clamp(
                volatility,
                WorldThreatSystem.startingVolatility(this.stage),
                WorldThreatSystem.maximumVolatility(this.stage));
        this.lastUpdatedDay = lastUpdatedDay;
    }

    public static WorldThreatData get(MinecraftServer server) {
        return get(server.overworld());
    }

    public static WorldThreatData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(int stage) {
        int clamped = Mth.clamp(stage, MIN_STAGE, MAX_STAGE);
        if (this.stage != clamped) {
            this.stage = clamped;
            this.volatility = WorldThreatSystem.startingVolatility(clamped);
            this.setDirty();
        }
    }

    public double getThreat() {
        return this.threat;
    }

    public void setThreat(double threat) {
        double clamped = Mth.clamp(threat, MIN_THREAT, MAX_THREAT);
        if (this.threat != clamped) {
            this.threat = clamped;
            this.setDirty();
        }
    }

    public double getVolatility() {
        return this.volatility;
    }

    public void setVolatility(double volatility) {
        double normalized = Mth.clamp(
                volatility,
                WorldThreatSystem.startingVolatility(this.stage),
                WorldThreatSystem.maximumVolatility(this.stage));
        if (this.volatility != normalized) {
            this.volatility = normalized;
            this.setDirty();
        }
    }

    public long getLastUpdatedDay() {
        return this.lastUpdatedDay;
    }

    /**
     * Applies an update for each newly crossed Minecraft day. The first
     * call only establishes a baseline so installing the mod cannot retroactively
     * roll once for every day an existing world has already existed.
     */
    public void updateThroughDay(long currentDay, RandomSource random) {
        if (this.lastUpdatedDay < 0L || currentDay < this.lastUpdatedDay) {
            this.lastUpdatedDay = currentDay;
            this.setDirty();
            return;
        }

        if (this.lastUpdatedDay < currentDay) {
            long elapsedDays = currentDay - this.lastUpdatedDay;
            int updateCount = (int) Math.min(elapsedDays, MAX_CATCH_UP_DAYS);
            for (int i = 0; i < updateCount; i++) {
                this.performDailyUpdate(random);
            }
            this.lastUpdatedDay = currentDay;
            this.setDirty();
        }
    }

    private void performDailyUpdate(RandomSource random) {
        this.volatility = Math.min(
                this.volatility + VOLATILITY_GROWTH_PER_DAY,
                WorldThreatSystem.maximumVolatility(this.stage));
        if (random.nextDouble() < WorldThreatSystem.riseChance(this.threat)) {
            this.threat = Mth.clamp(this.threat + this.volatility, MIN_THREAT, MAX_THREAT);
        } else {
            this.threat = Mth.clamp(this.threat - this.volatility, MIN_THREAT, MAX_THREAT);
        }
    }

    /** Ready for the future small-event implementation described by the revision. */
    public void applySmallEventVictory() {
        this.threat = Math.max(MIN_THREAT, this.threat - 30.0D);
        this.volatility = Math.max(
                WorldThreatSystem.startingVolatility(this.stage),
                this.volatility - 5.0D);
        this.setDirty();
    }
}
