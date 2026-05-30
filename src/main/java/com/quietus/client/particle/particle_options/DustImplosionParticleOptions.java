package com.quietus.client.particle.particle_options;

import com.quietus.client.particle.QuietusParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustImplosionParticleOptions extends ScalableParticleOptionsBase {
    public static final MapCodec<DustImplosionParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(DustImplosionParticleOptions::getColorAsInt),
                    SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)
            ).apply(instance, DustImplosionParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DustImplosionParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, DustImplosionParticleOptions::getColorAsInt,
                    ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale,
                    DustImplosionParticleOptions::new
            );

    // Remove the static initializer block and use the above declaration

    private final int color;

    public DustImplosionParticleOptions(int color, float scale) {
        super(scale);
        this.color = color;
    }

    // Alternative constructor for Vector3f color
    public DustImplosionParticleOptions(Vec3 color, float scale) {

        this(ARGB.color(color), scale);
    }

    @Override
    public ParticleType<?> getType() {
        return QuietusParticles.DUST_IMPLOSION.get(); // Make sure this is properly registered
    }

    public Vector3f getColor() {
        return ARGB.vector3fFromRGB24(this.color);
    }

    public int getColorAsInt() {
        return this.color;
    }
}
