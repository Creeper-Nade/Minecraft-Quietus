package com.minecraftquietus.quietus.enchantment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.List;

public class QuietusEnchantmentHelper extends EnchantmentHelper {
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

    public static void Enchantment_modifyCritChance(Enchantment enchantment, ServerLevel level, int enchantmentLevel, ItemStack tool, Entity entity, DamageSource damageSource, MutableFloat damage) {
        enchantment.modifyDamageFilteredValue(QuietusEnchantmentComponent.CRIT_CHANCE.get(), level, enchantmentLevel, tool, entity, damageSource, damage);
    }

    public static void Enchantment_modifyManaCost(Enchantment enchantment, ServerLevel level, int enchantmentLevel, ItemStack tool, MutableFloat cost) {
        enchantment.modifyItemFilteredCount(QuietusEnchantmentComponent.MANA_COST_REDUCTION.get(), level, enchantmentLevel, tool, cost);
    }
}
