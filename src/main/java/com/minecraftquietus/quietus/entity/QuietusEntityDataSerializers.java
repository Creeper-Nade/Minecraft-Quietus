package com.minecraftquietus.quietus.entity;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.core.skill.Skill;
import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.rmi.registry.Registry;
import java.util.function.Supplier;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class QuietusEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> REGISTRAR = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, MODID);

    public static final Supplier<EntityDataSerializer<ResourceLocation>> RESOURCE_LOCATION = REGISTRAR.register("resource_location",
            () -> EntityDataSerializer.forValueType(ResourceLocation.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }

}
