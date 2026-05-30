package com.quietus.entity;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static com.quietus.Quietus.MODID;

public class QuietusEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> REGISTRAR = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, MODID);

    public static final Supplier<EntityDataSerializer<Identifier>> RESOURCE_LOCATION = REGISTRAR.register("resource_location",
            () -> EntityDataSerializer.forValueType(Identifier.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }

}
