package com.minecraftquietus.quietus.skilltree;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.util.SkillUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record Reward(
    ResourceLocation skillLocation,
    int amount,
    String source
) {
    public static final Codec<Reward> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("skill").forGetter(Reward::skillLocation),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(Reward::amount),
            Codec.STRING.optionalFieldOf("source", "none").forGetter(Reward::source)
        ).apply(instance, Reward::new)
    );

    public void apply(Player player) {
        if (QuietusRegistries.SKILL_REGISTRY.containsKey(this.skillLocation)) {
            SkillUtil.addSkillLevel(player, QuietusRegistries.SKILL_REGISTRY.getValue(this.skillLocation), this.amount, this.source);
        }
    }

    public static Reward make(ResourceLocation skillLocation, int amount, String source) {
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
