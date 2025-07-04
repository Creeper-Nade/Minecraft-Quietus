package com.minecraftquietus.quietus.sounds;

import com.minecraftquietus.quietus.Quietus;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuietusSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Quietus.MODID);

    public static final Holder<SoundEvent> Steve_UOH = SOUND_EVENTS.register(
            "steve_uoh",
            // Takes in the registry name
            SoundEvent::createVariableRangeEvent
    );


    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
