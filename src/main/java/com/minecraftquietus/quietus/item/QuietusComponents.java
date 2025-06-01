package com.minecraftquietus.quietus.item;

import java.util.function.Supplier;

import com.minecraftquietus.quietus.item.component.UsesMana;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecraftquietus.quietus.Quietus.MODID;


public class QuietusComponents {
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    /**
     * Mana consumed 
     */
    public static final Supplier<DataComponentType<UsesMana>> USES_MANA = REGISTRAR.registerComponentType("uses_mana", builder -> builder.persistent(UsesMana.CODEC).networkSynchronized(UsesMana.STREAM_CODEC));




    public static void register(IEventBus eventBus)
    {
        REGISTRAR.register(eventBus);
    }
}
