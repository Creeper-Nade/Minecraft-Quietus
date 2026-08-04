package com.quietus.loot;

import com.quietus.Config;
import com.quietus.item.QuietusItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.Map;
import java.util.function.DoubleSupplier;

/** Adds the Amethyst Upgrade template to selected vanilla structure reward tables. */
public final class AmethystUpgradeTemplateLoot {
    private static final String POOL_NAME = "quietus_amethyst_upgrade_template";

    private static final Map<Identifier, DoubleSupplier> TARGET_CHANCES = Map.of(
            minecraft("chests/trial_chambers/reward"), Config.LOOT.trialChamberVaultChance::get,
            minecraft("chests/trial_chambers/reward_ominous"), Config.LOOT.ominousTrialChamberVaultChance::get,
            minecraft("chests/abandoned_mineshaft"), Config.LOOT.mineshaftMinecartChance::get,
            minecraft("chests/simple_dungeon"), Config.LOOT.monsterRoomChestChance::get,
            minecraft("chests/stronghold_library"), Config.LOOT.strongholdLibraryChance::get,
            minecraft("chests/stronghold_corridor"), Config.LOOT.strongholdCorridorChance::get,
            minecraft("chests/ancient_city"), Config.LOOT.ancientCityChance::get
    );

    private AmethystUpgradeTemplateLoot() {
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        DoubleSupplier chanceSupplier = TARGET_CHANCES.get(event.getName());
        if (chanceSupplier == null || event.getTable().getPool(POOL_NAME) != null) {
            return;
        }

        float chance = (float) chanceSupplier.getAsDouble();
        if (chance <= 0.0F) {
            return;
        }

        LootPool pool = LootPool.lootPool()
                .name(POOL_NAME)
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(QuietusItems.AMETHYST_UPGRADE_SMITHING_TEMPLATE.get()))
                .build();
        event.getTable().addPool(pool);
    }

    private static Identifier minecraft(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
