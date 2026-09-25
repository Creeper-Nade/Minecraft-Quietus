package com.quietus.data;

import com.quietus.tags.QuietusTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.concurrent.CompletableFuture;

public class QuietusMobEffectTagProvider extends KeyTagProvider<MobEffect> {

    public QuietusMobEffectTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.MOB_EFFECT, lookupProvider);
    }

    protected TagAppender<Holder<MobEffect>, MobEffect> effectTag(TagKey<MobEffect> tagKey) {
        return this.tag(tagKey).map(holder -> holder.unwrapKey().orElseThrow());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.effectTag(QuietusTags.MobEffects.DEBUFFS)
                .add(MobEffects.SLOWNESS)
                .add(MobEffects.MINING_FATIGUE)
                .add(MobEffects.INSTANT_DAMAGE)
                .add(MobEffects.NAUSEA)
                .add(MobEffects.BLINDNESS)
                .add(MobEffects.HUNGER)
                .add(MobEffects.WEAKNESS)
                .add(MobEffects.POISON)
                .add(MobEffects.WITHER)
                .add(MobEffects.LEVITATION)
                .add(MobEffects.UNLUCK)
                .add(MobEffects.DARKNESS)
                .add(MobEffects.WIND_CHARGED)
                .add(MobEffects.WEAVING)
                .add(MobEffects.OOZING)
                .add(MobEffects.INFESTED);
    }
}
