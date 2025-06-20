package com.minecraftquietus.quietus.enchantment;

import com.minecraftquietus.quietus.Quietus;
import com.minecraftquietus.quietus.enchantment.effect_components.crit_chance;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static net.minecraft.core.component.DataComponentType.builder;

public interface QuietusEnchantmentComponent {

    public static final DeferredRegister.DataComponents ENCHANTMENT_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Quietus.MODID);

    //Codec<DataComponentType<?>> COMPONENT_CODEC = Codec.lazyInitialized(() -> BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.byNameCodec());
    //Codec<DataComponentMap> CODEC = DataComponentMap.makeCodec(COMPONENT_CODEC);

   //DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> CRIT_CHANCE = register("crit_chance", (p_380869_) -> p_380869_.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));
  // public static final DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> CRIT_CHANCE = ENCHANTMENT_COMPONENT_TYPES.registerComponentType("crit_chance",(p_380869_) -> p_380869_.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));
/*
    public static final Supplier<DataComponentType<crit_chance>> CRIT_CHANCE =
            ENCHANTMENT_COMPONENT_TYPES.registerComponentType(
                    "crit_chance",
                    builder -> builder.persistent(crit_chance.CODEC)
            );*/
/*
public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConditionalEffect<crit_chance>>> CONDITIONAL_INCREMENT =
        ENCHANTMENT_COMPONENT_TYPES.register("conditional_increment",
                () -> DataComponentType.ConditionalEffect<crit_chance>builder()
                        // The ContextKeySet needed depends on what the enchantment is supposed to do.
                        // This might be one of ENCHANTED_DAMAGE, ENCHANTED_ITEM, ENCHANTED_LOCATION, ENCHANTED_ENTITY, or HIT_BLOCK
                        // since all of these bring the enchantment level into context (along with whatever other information is indicated).
                        .persistent(ConditionalEffect.codec(crit_chance.CODEC, LootContextParamSets.ENCHANTED_DAMAGE))
                        .build());*/

    public static final Supplier<DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> CRIT_CHANCE =  ENCHANTMENT_COMPONENT_TYPES.registerComponentType(
            "crit_chance", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
    );

    public static final Supplier<DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> MANA_COST_REDUCTION =  ENCHANTMENT_COMPONENT_TYPES.registerComponentType(
            "mana_cost_reduction", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
    );

    public static void register(IEventBus eventBus)
    {
        ENCHANTMENT_COMPONENT_TYPES.register(eventBus);
    }

}
