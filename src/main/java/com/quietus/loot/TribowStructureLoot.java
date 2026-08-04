package com.quietus.loot;

import com.quietus.Config;
import com.quietus.item.QuietusItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.function.DoubleSupplier;

/** Adds Tribows to selected vanilla structure reward tables. */
public final class TribowStructureLoot {
    private static final Identifier TRIAL_CHAMBER_REWARD = minecraft("chests/trial_chambers/reward");
    private static final Identifier OMINOUS_TRIAL_CHAMBER_REWARD = minecraft("chests/trial_chambers/reward_ominous");
    private static final Identifier NETHER_FORTRESS = minecraft("chests/nether_bridge");

    private static final String TRIAL_POOL = "quietus_tribow_trial_chamber";
    private static final String OMINOUS_TRIAL_POOL = "quietus_tribow_ominous_trial_chamber";
    private static final String NETHER_NORMAL_POOL = "quietus_tribow_nether_fortress";
    private static final String NETHER_ENCHANTED_POOL = "quietus_tribow_nether_fortress_enchanted";

    private TribowStructureLoot() {
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        Identifier table = event.getName();

        if (table.equals(TRIAL_CHAMBER_REWARD)) {
            addPool(event, TRIAL_POOL, Config.LOOT.tribowTrialChamberVaultChance::get,
                    LootItem.lootTableItem(QuietusItems.TRIBOW.get())
                            // Mirrors the enchanted bow in the vanilla Trial Chamber rare rewards.
                            .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                    event.getRegistries(), UniformGenerator.between(5.0F, 15.0F))));
        } else if (table.equals(OMINOUS_TRIAL_CHAMBER_REWARD)) {
            addPool(event, OMINOUS_TRIAL_POOL, Config.LOOT.tribowOminousTrialChamberVaultChance::get,
                    LootItem.lootTableItem(QuietusItems.TRIBOW.get())
                            // Mirrors the enchanted crossbow in the vanilla ominous rare rewards.
                            .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                    event.getRegistries(), UniformGenerator.between(5.0F, 20.0F))));
        } else if (table.equals(NETHER_FORTRESS)) {
            addPool(event, NETHER_NORMAL_POOL, Config.LOOT.tribowNetherFortressChance::get,
                    LootItem.lootTableItem(QuietusItems.TRIBOW.get()));
            addPool(event, NETHER_ENCHANTED_POOL, Config.LOOT.enchantedTribowNetherFortressChance::get,
                    LootItem.lootTableItem(QuietusItems.TRIBOW.get())
                            // Mirrors the damaged enchanted crossbow in vanilla Bastion Bridge loot.
                            .apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.5F)))
                            .apply(EnchantRandomlyFunction.randomApplicableEnchantment(event.getRegistries())));
        }
    }

    private static void addPool(LootTableLoadEvent event, String poolName, DoubleSupplier chanceSupplier,
                                LootItem.Builder<?> entry) {
        if (event.getTable().getPool(poolName) != null) {
            return;
        }

        float chance = (float) chanceSupplier.getAsDouble();
        if (chance <= 0.0F) {
            return;
        }

        LootPool pool = LootPool.lootPool()
                .name(poolName)
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(entry)
                .build();
        event.getTable().addPool(pool);
    }

    private static Identifier minecraft(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
