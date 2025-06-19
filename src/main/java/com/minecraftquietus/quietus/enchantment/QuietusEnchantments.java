package com.minecraftquietus.quietus.enchantment;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.tags.QuietusTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class QuietusEnchantments {
    public static final ResourceKey<Enchantment> IMPACT = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Quietus.MODID, "impact"));
    public static final ResourceKey<Enchantment> HEX = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Quietus.MODID, "hex"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> holdergetter4 = context.lookup(Registries.ENTITY_TYPE);

        register(context, HEX, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(QuietusTags.Items.MAGIC_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(20, 10),
                        1,
                EquipmentSlotGroup.MAINHAND)).withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(1.0F)), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, net.minecraft.advancements.critereon.EntityPredicate.Builder.entity().of(holdergetter4, QuietusTags.Entity.MAGIC_PROJECTILE).build())));

        register(context, IMPACT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(QuietusTags.Items.MAGIC_ENCHANTABLE),
                2,
                2,
                Enchantment.dynamicCost(10, 20),
                Enchantment.dynamicCost(35, 20),
                4,
                EquipmentSlotGroup.MAINHAND)).withEffect(EnchantmentEffectComponents.KNOCKBACK, new AddValue(LevelBasedValue.perLevel(0.9F)), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, net.minecraft.advancements.critereon.EntityPredicate.Builder.entity().of(holdergetter4, QuietusTags.Entity.MAGIC_PROJECTILE).build())));
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
                                 Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
