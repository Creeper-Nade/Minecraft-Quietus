package com.minecraftquietus.quietus.potion;

import com.minecraftquietus.quietus.effects.QuietusEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, MODID);

    /*public static final Holder<Potion> SPELUNKER_POTION = POTIONS.register("spelunking", new Potion(
            // The suffix applied to the potion
            MODID.getPath(),
            // The effects used by the potion
            new MobEffectInstance(ModEffects.SPELUNKING_EFFECT, 3600)
    ));*/

    public static final DeferredHolder<Potion,Potion> SPELUNKING = POTIONS.register("spelunking", () -> new Potion("spelunking",
            new MobEffectInstance[]{
                    new MobEffectInstance(QuietusEffects.SPELUNKING_EFFECT,3600)
            }));
    public static final DeferredHolder<Potion,Potion> LONG_SPELUNKING = POTIONS.register("long_spelunking", () -> new Potion("spelunking",
            new MobEffectInstance[]{
                    new MobEffectInstance(QuietusEffects.SPELUNKING_EFFECT,6000)
            }));
    public static final DeferredHolder<Potion,Potion> STRONG_SPELUNKING = POTIONS.register("strong_spelunking", () -> new Potion("spelunking",
            new MobEffectInstance[]{
                    new MobEffectInstance(QuietusEffects.SPELUNKING_EFFECT,2400,1)
            }));

    public static final DeferredHolder<Potion,Potion> LESSER_INSTANT_MANA = POTIONS.register("lesser_instant_mana", () -> new Potion("lesser_instant_mana",
            new MobEffectInstance[]{
                    new MobEffectInstance(QuietusEffects.INSTANT_MANA,0)
            }));
    

    public static void register(IEventBus eventBus)
    {
        POTIONS.register(eventBus);
    }
}
