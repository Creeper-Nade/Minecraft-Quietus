package com.minecraftquietus.quietus.effects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS= DeferredRegister.create(BuiltInRegistries.MOB_EFFECT,MODID);

    public static final Holder<MobEffect> SPELUNKING_EFFECT = MOB_EFFECTS.register("spelunking", () -> new Spelunking_Effect(
            //Can be either BENEFICIAL, NEUTRAL or HARMFUL. Used to determine the potion tooltip color of this effect.
            MobEffectCategory.BENEFICIAL,
            //The color of the effect particles in RGB format.
            0xFAD540));

    public static void register (IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
