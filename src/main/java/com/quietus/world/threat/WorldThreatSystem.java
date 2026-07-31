package com.quietus.world.threat;

import com.quietus.Quietus;
import com.quietus.entity.QuietusEntityTypes;
import com.quietus.tags.QuietusTags;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;

/** Formulae and selection rules from the World Threat design specification. */
public final class WorldThreatSystem {
    public static final double STAGE_BASE_MULTIPLIER = 2.25D;
    /** Adds 1% damage per Threat point, reaching x2.0 at Threat 100. */
    public static final double THREAT_DAMAGE_PER_LEVEL = 0.01D;
    public static final int MAX_ZOMBIE_PACK_SIZE_BONUS = 4;
    public static final double MAX_EXTRA_HOSTILE_PACK_ATTEMPT_CHANCE = 0.25D;
    public static final float MAX_ARMOR_CHANCE_BONUS = 0.15F;
    public static final float MAX_ZOMBIE_WEAPON_CHANCE_BONUS = 0.10F;
    public static final float MAX_CHICKEN_JOCKEY_ROLL_CHANCE = 0.15F;
    public static final float MAX_SPIDER_JOCKEY_CHANCE = 0.15F;
    public static final float MAX_CAMEL_HUSK_JOCKEY_CHANCE = 0.25F;
    public static final float MAX_ZOMBIE_NAUTILUS_JOCKEY_CHANCE = 0.75F;
    public static final float MAX_ZOMBIE_HORSEMAN_CHANCE = 0.15F;
    private static final double[] STARTING_VOLATILITY = {2.0D, 5.0D, 8.0D, 12.0D};
    private static final double[] MAXIMUM_VOLATILITY = {15.0D, 25.0D, 35.0D, 50.0D};
    private static final Identifier STAGE_HEALTH_MODIFIER =
            Identifier.fromNamespaceAndPath(Quietus.MODID, "world_threat_stage_health");

    private WorldThreatSystem() {
    }

    public static double stageMultiplier(int stage) {
        return Math.pow(STAGE_BASE_MULTIPLIER,
                Mth.clamp(stage, WorldThreatData.MIN_STAGE, WorldThreatData.MAX_STAGE));
    }

    public static double threatDamageMultiplier(double threat) {
        return 1.0D + THREAT_DAMAGE_PER_LEVEL
                * Mth.clamp(threat, WorldThreatData.MIN_THREAT, WorldThreatData.MAX_THREAT);
    }

    public static double damageMultiplier(int stage, double threat) {
        return stageMultiplier(stage) * threatDamageMultiplier(threat);
    }

    /** Adds 0-4 members while preserving the spawn entry's original min/max spread. */
    public static int zombiePackSizeBonus(double threat) {
        return Mth.floor(threatProgress(threat) * MAX_ZOMBIE_PACK_SIZE_BONUS);
    }

    /** At maximum Threat, hostile spawning receives one extra pack attempt 25% of the time. */
    public static double extraHostilePackAttemptChance(double threat) {
        return threatProgress(threat) * MAX_EXTRA_HOSTILE_PACK_ATTEMPT_CHANCE;
    }

    public static float armorChanceCoefficient(float vanillaCoefficient, double threat) {
        return vanillaCoefficient + (float) threatProgress(threat) * MAX_ARMOR_CHANCE_BONUS;
    }

    public static float zombieWeaponChance(float vanillaChance, double threat) {
        return vanillaChance + (float) threatProgress(threat) * MAX_ZOMBIE_WEAPON_CHANCE_BONUS;
    }

    public static float chickenJockeyRollChance(double threat) {
        return threatScaledChance(0.05F, MAX_CHICKEN_JOCKEY_ROLL_CHANCE, threat);
    }

    public static int spiderJockeyRollBound(double threat) {
        float chance = spiderJockeyChance(threat);
        return Math.max(1, Math.round(1.0F / chance));
    }

    public static float spiderJockeyChance(double threat) {
        return threatScaledChance(0.01F, MAX_SPIDER_JOCKEY_CHANCE, threat);
    }

    public static float camelHuskJockeyChance(double threat) {
        return threatScaledChance(0.10F, MAX_CAMEL_HUSK_JOCKEY_CHANCE, threat);
    }

    public static float zombieNautilusJockeyChance(double threat) {
        return threatScaledChance(0.50F, MAX_ZOMBIE_NAUTILUS_JOCKEY_CHANCE, threat);
    }

    public static float zombieHorsemanChance(double threat) {
        return threatScaledChance(0.1F, MAX_ZOMBIE_HORSEMAN_CHANCE, threat);
    }

    private static float threatScaledChance(float vanillaChance, float maximumChance, double threat) {
        return Mth.lerp((float) threatProgress(threat), vanillaChance, maximumChance);
    }

    private static double threatProgress(double threat) {
        return Mth.clamp(threat, WorldThreatData.MIN_THREAT, WorldThreatData.MAX_THREAT)
                / WorldThreatData.MAX_THREAT;
    }

    public static double riseChance(double threat) {
        double clamped = Mth.clamp(threat, WorldThreatData.MIN_THREAT, WorldThreatData.MAX_THREAT);
        return 1.0D - clamped / 140.0D;
    }

    public static double startingVolatility(int stage) {
        return STARTING_VOLATILITY[Mth.clamp(stage, WorldThreatData.MIN_STAGE, WorldThreatData.MAX_STAGE)];
    }

    public static double maximumVolatility(int stage) {
        return MAXIMUM_VOLATILITY[Mth.clamp(stage, WorldThreatData.MIN_STAGE, WorldThreatData.MAX_STAGE)];
    }

    public static boolean isScalableEnemy(Mob mob) {
        return mob instanceof Enemy
                && mob.getType().getTags().noneMatch(QuietusTags.Entity.BOSS_MONSTER::equals);
    }

    /** Applies stage health while optionally preserving the mob's current health percentage. */
    public static void applyStageHealth(Mob mob, int stage, boolean refillHealth) {
        if (!isScalableEnemy(mob)) {
            return;
        }

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        float healthFraction = mob.getMaxHealth() <= 0.0F ? 1.0F : mob.getHealth() / mob.getMaxHealth();
        double bonus = stageMultiplier(stage) - 1.0D;
        if (bonus > 0.0D) {
            maxHealth.addOrReplacePermanentModifier(new AttributeModifier(
                    STAGE_HEALTH_MODIFIER,
                    bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            maxHealth.removeModifier(STAGE_HEALTH_MODIFIER);
        }

        mob.setHealth(refillHealth ? mob.getMaxHealth() : mob.getMaxHealth() * healthFraction);
    }

    /**
     * Selects among the skeleton variants currently implemented by the mod.
     * Stage 0 interpolates from 50/30/20 at Threat 0 to 25/45/30 at Threat 100.
     * Later stages retain that stronger distribution until another variant exists.
     */
    public static EntityType<?> selectSkeletonVariant(int stage, double threat, RandomSource random) {
        double progression = Mth.clamp(stage + threat / 100.0D, 0.0D, 1.0D);
        double normalChance = 0.50D - 0.25D * progression;
        double bowslingerChance = 0.30D + 0.15D * progression;
        double roll = random.nextDouble();

        if (roll < normalChance) {
            return EntityType.SKELETON;
        }
        if (roll < normalChance + bowslingerChance) {
            return QuietusEntityTypes.BOWSLINGER.get();
        }
        return QuietusEntityTypes.PARABOLER.get();
    }
}
