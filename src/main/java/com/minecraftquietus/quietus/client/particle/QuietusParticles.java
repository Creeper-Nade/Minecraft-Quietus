package com.minecraftquietus.quietus.client.particle;

import com.minecraftquietus.quietus.client.particle.particle_options.DustExplosionParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class QuietusParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, "quietus");

    public static final Supplier<ParticleType<DustExplosionParticleOptions>> DUST_EXPLOSION =
            registerParticle("dust_explosion", false, (p_337461_) -> DustExplosionParticleOptions.CODEC, (p_319435_) -> DustExplosionParticleOptions.STREAM_CODEC);

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> registerParticle(String name, boolean overrideLimitter, final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter) {
        return PARTICLE_TYPES.register(name, () ->new ParticleType<T>(overrideLimitter) {
            public MapCodec<T> codec() {
                return (MapCodec)codecGetter.apply(this);
            }

            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return (StreamCodec)streamCodecGetter.apply(this);
            }
        });
    }

    public static void register(IEventBus eventBus)
    {
        PARTICLE_TYPES.register(eventBus);
    }
}
