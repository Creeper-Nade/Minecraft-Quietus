package com.quietus.enchantment;

import com.quietus.item.property.GrapplingHookProperty;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableFloat;

public class QuietusEnchantmentHelper extends EnchantmentHelper {
    private static final float ELONGATION_DISTANCE_PER_LEVEL = 2.0F;
    private static final float RESILIENCE_STAT_INCREASE_PER_LEVEL = 0.1F;
    private static final float ACUPUNCTURE_CRIT_MULTIPLIER_PER_LEVEL = 0.25F / 3.0F;
    private static final float ATTUNEMENT_RADIUS_PER_LEVEL = 0.025F;

    /** Adds 1/12 per level, taking a normal x1.5 critical hit to x1.75 at level III. */
    public static float getAcupunctureCritMultiplierBonus(Level level, ItemStack tool) {
        int enchantmentLevel = getLevel(level, tool, QuietusEnchantments.ACUPUNCTURE);
        return enchantmentLevel * ACUPUNCTURE_CRIT_MULTIPLIER_PER_LEVEL;
    }

    /** Expands each chanting checkpoint by 2.5% of the full timing track per level. */
    public static float getAttunedCheckpointRadius(Level level, ItemStack tool, float baseRadius) {
        int enchantmentLevel = getLevel(level, tool, QuietusEnchantments.ATTUNEMENT);
        return Mth.clamp(baseRadius + enchantmentLevel * ATTUNEMENT_RADIUS_PER_LEVEL, 0.0F, 0.499F);
    }
    public static float modifyManaCost(ServerLevel level, ItemStack tool, float reduction) {
        MutableFloat mutablefloat = new MutableFloat(reduction);
        runIterationOnItem(tool, (p_344525_, p_344526_) ->Enchantment_modifyManaCost(((Enchantment)p_344525_.value()),level, p_344526_, tool, mutablefloat));
        return mutablefloat.floatValue();
    }

    /**
     * Applies the enchantments stored on a hook stack to its base stats. Keeping
     * this calculation here lets every current and future GrapplingHookItem use
     * the same behavior.
     */
    public static GrapplingHookProperty modifyGrapplingHookProperties(
            ServerLevel level, ItemStack hook, GrapplingHookProperty baseProperties
    ) {
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int elongationLevel = getItemEnchantmentLevel(enchantments.getOrThrow(QuietusEnchantments.ELONGATION), hook);
        int resilienceLevel = getItemEnchantmentLevel(enchantments.getOrThrow(QuietusEnchantments.RESILIENCE), hook);
        float resilienceMultiplier = 1.0F + resilienceLevel * RESILIENCE_STAT_INCREASE_PER_LEVEL;

        return new GrapplingHookProperty(
                baseProperties.maxRange(),
                baseProperties.pullStrength() * resilienceMultiplier,
                baseProperties.frictionMultiplier(),
                baseProperties.maxPullSpeed() * resilienceMultiplier,
                baseProperties.maxTravelDistance() + elongationLevel * ELONGATION_DISTANCE_PER_LEVEL
        );
    }

    private static int getLevel(Level level, ItemStack tool, net.minecraft.resources.ResourceKey<Enchantment> enchantment) {
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return getItemEnchantmentLevel(enchantments.getOrThrow(enchantment), tool);
    }

    public static void Enchantment_modifyManaCost(Enchantment enchantment, ServerLevel level, int enchantmentLevel, ItemStack tool, MutableFloat cost) {
        enchantment.modifyItemFilteredCount(QuietusEnchantmentComponent.MANA_COST_REDUCTION.get(), level, enchantmentLevel, tool, cost);
    }
}
