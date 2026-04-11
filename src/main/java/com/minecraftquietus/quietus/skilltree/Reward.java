package com.minecraftquietus.quietus.skilltree;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.util.SkillUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public record Reward(
    Identifier skillLocation,
    int amount,
    String source
) {
    public static final Codec<Reward> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Identifier.CODEC.fieldOf("skill").forGetter(Reward::skillLocation),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(Reward::amount),
            Codec.STRING.optionalFieldOf("source", "none").forGetter(Reward::source)
        ).apply(instance, Reward::new)
    );

    public static final StreamCodec<FriendlyByteBuf, Reward> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, Reward::skillLocation,    
        ByteBufCodecs.INT, Reward::amount,
        ByteBufCodecs.STRING_UTF8, Reward::source,
        Reward::new
    );

    public void apply(Player player) {
        if (QuietusRegistries.SKILL_REGISTRY.containsKey(this.skillLocation)) {
            SkillUtil.addSkillLevel(player, QuietusRegistries.SKILL_REGISTRY.getValue(this.skillLocation), this.amount, this.source);
        }
    }

    public static Reward make(Identifier skillLocation, int amount, String source) {
        return new Reward(skillLocation, amount, source);
    }

    
    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    }
}
