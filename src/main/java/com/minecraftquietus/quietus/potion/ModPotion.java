package com.minecraftquietus.quietus.potion;

import com.minecraftquietus.quietus.effects.ModEffects;
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

public class ModPotion {
    public static final DeferredRegister<Potion> POTIONS=DeferredRegister.create(BuiltInRegistries.POTION, MODID);

    /*public static final Holder<Potion> SPELUNKER_POTION = POTIONS.register("spelunker_potion", new Potion(
            // The suffix applied to the potion
            MODID.getPath(),
            // The effects used by the potion
            new MobEffectInstance(ModEffects.SPELUNKING_EFFECT, 3600)
    ));*/

    public static final DeferredHolder<Potion,Potion> SPELUNKER_POTION=POTIONS.register("spelunker_potion", () -> new Potion("spelunker_potion",
            new MobEffectInstance[]{
                    new MobEffectInstance(ModEffects.SPELUNKING_EFFECT,3600)
            }));

    public static void register(IEventBus eventBus)
    {
        POTIONS.register(eventBus);
    }
}
