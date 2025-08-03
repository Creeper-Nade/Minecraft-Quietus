package com.minecraftquietus.quietus.skilltree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minecraftquietus.quietus.core.QuietusRegistries;
import com.minecraftquietus.quietus.util.SkillUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record SkillPoint(
    Prerequisites prerequisites,
    List<Reward> rewards
) {
    public static final Codec<SkillPoint> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Prerequisites.CODEC.optionalFieldOf("prerequisites", Prerequisites.EMPTY).forGetter(SkillPoint::prerequisites),
            Reward.CODEC.listOf().fieldOf("rewards").forGetter(SkillPoint::rewards)
        ).apply(instance, SkillPoint::new)
    );

    public void apply(Player player) {
        for (Reward action : this.rewards) {
            action.apply(player);
        }
    }


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


    @Override
    public String toString() {
        return CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this)
            .result()
            .map(com.google.gson.JsonElement::toString)
            .orElse("{}");
    } 
}
