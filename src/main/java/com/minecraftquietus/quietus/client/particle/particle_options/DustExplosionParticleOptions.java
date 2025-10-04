package com.minecraftquietus.quietus.client.particle.particle_options;

import com.minecraftquietus.quietus.client.particle.DustExplosion;
import com.minecraftquietus.quietus.client.particle.QuietusParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustExplosionParticleOptions  extends ScalableParticleOptionsBase {
    public static final MapCodec<DustExplosionParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(DustExplosionParticleOptions::getColorAsInt),
                    SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)
            ).apply(instance, DustExplosionParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DustExplosionParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, DustExplosionParticleOptions::getColorAsInt,
                    ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale,
                    DustExplosionParticleOptions::new
            );

    // Remove the static initializer block and use the above declaration

    private final int color;

    public DustExplosionParticleOptions(int color, float scale) {
        super(scale);
        this.color = color;
    }

    // Alternative constructor for Vector3f color
    public DustExplosionParticleOptions(Vec3 color, float scale) {

        this(ARGB.color(color), scale);
    }

    @Override
    public ParticleType<?> getType() {
        return QuietusParticles.DUST_EXPLOSION.get(); // Make sure this is properly registered
    }

    public Vector3f getColor() {
        return ARGB.vector3fFromRGB24(this.color);
    }

    public int getColorAsInt() {
        return this.color;
    }
}
