package com.quietus.data;

import com.quietus.tags.QuietusTags;
import com.quietus.util.damage.QuietusDamageType;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.concurrent.CompletableFuture;

public class QuietusDamageTypeTagProvider extends DamageTypeTagsProvider {
    public QuietusDamageTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(DamageTypeTags.BYPASSES_COOLDOWN)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME);
        this.tag(DamageTypeTags.PANIC_CAUSES)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME);
        this.tag(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME);
        this.tag(QuietusTags.DamageTypes.BUFFABLE_MAGIC)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE)
                .add(QuietusDamageType.MAGIC_PROJECTILE_DAMAGE_BYPASS_INVINCIBLE_FRAME);
    }
}
