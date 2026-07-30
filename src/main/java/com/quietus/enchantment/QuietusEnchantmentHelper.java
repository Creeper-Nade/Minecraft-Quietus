package com.quietus.enchantment;

import com.quietus.item.property.GrapplingHookProperty;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import org.apache.commons.lang3.mutable.MutableFloat;

public class QuietusEnchantmentHelper extends EnchantmentHelper {
    private static final float ELONGATION_DISTANCE_PER_LEVEL = 2.0F;
    private static final float RESILIENCE_STAT_INCREASE_PER_LEVEL = 0.1F;

    public static float modifyCritChance(ServerLevel level, ItemStack tool, Entity entity, DamageSource damageSource, double chance) {
        MutableFloat mutablefloat = new MutableFloat(chance);
        runIterationOnItem(tool, (p_344525_, p_344526_) ->Enchantment_modifyCritChance(((Enchantment)p_344525_.value()),level, p_344526_, tool, entity, damageSource, mutablefloat));
        return mutablefloat.floatValue();
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

    public static void Enchantment_modifyCritChance(Enchantment enchantment, ServerLevel level, int enchantmentLevel, ItemStack tool, Entity entity, DamageSource damageSource, MutableFloat damage) {
        enchantment.modifyDamageFilteredValue(QuietusEnchantmentComponent.CRIT_CHANCE.get(), level, enchantmentLevel, tool, entity, damageSource, damage);
    }

    public static void Enchantment_modifyManaCost(Enchantment enchantment, ServerLevel level, int enchantmentLevel, ItemStack tool, MutableFloat cost) {
        enchantment.modifyItemFilteredCount(QuietusEnchantmentComponent.MANA_COST_REDUCTION.get(), level, enchantmentLevel, tool, cost);
    }
}
