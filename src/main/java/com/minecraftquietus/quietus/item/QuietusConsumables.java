package com.minecraftquietus.quietus.item;

import java.util.List;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class QuietusConsumables {

    public static final Consumable MOLD = Consumables.defaultFood()
        .onConsume(
            new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.HUNGER, 400, 0),
                    new MobEffectInstance(MobEffects.NAUSEA, 200, 0)
                )
            )
        )
        .build();

    public static final Consumable MOLD_BUCKET = Consumables.defaultDrink()
        .onConsume(
            new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.HUNGER, 500, 0),
                    new MobEffectInstance(MobEffects.NAUSEA, 160, 0),
                    new MobEffectInstance(MobEffects.POISON, 100, 0)
                )
            )
        )
        .build();

    public static final Consumable MOLD_BOWL = Consumables.defaultDrink()
        .onConsume(
            new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.HUNGER, 400, 0),
                    new MobEffectInstance(MobEffects.NAUSEA, 160, 0),
                    new MobEffectInstance(MobEffects.POISON, 100, 0)
                )
            )
        )
        .build();

    public static final Consumable YOGHURT_BUCKET = Consumables.defaultDrink().build();
    public static final Consumable CHEESE_BUCKET = Consumables.defaultDrink().build();


    public static Consumable init() {
        return MOLD;
    }
}
