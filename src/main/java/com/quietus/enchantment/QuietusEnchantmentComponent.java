package com.quietus.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

import static com.quietus.Quietus.MODID;

public interface QuietusEnchantmentComponent {

    DeferredRegister.DataComponents ENCHANTMENT_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, MODID);

    Supplier<DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> MANA_COST_REDUCTION =  ENCHANTMENT_COMPONENT_TYPES.registerComponentType(
            "mana_cost_reduction", builder -> builder.persistent(validatedListCodec(ConditionalEffect.codec(EnchantmentValueEffect.CODEC),LootContextParamSets.ENCHANTED_ITEM))
    );

    private static <T extends Validatable> Codec<List<T>> validatedListCodec(Codec<T> elementCodec, ContextKeySet paramSet) {
        return elementCodec.listOf().validate(Validatable.listValidatorForContext(paramSet));
}
    static void register(IEventBus eventBus)
    {
        ENCHANTMENT_COMPONENT_TYPES.register(eventBus);
    }

}
