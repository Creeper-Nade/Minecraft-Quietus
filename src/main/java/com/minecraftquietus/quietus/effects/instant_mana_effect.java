package com.minecraftquietus.quietus.effects;

import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class instant_mana_effect extends InstantenousMobEffect {
    protected instant_mana_effect(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier)
    {
        livingEntity.getData(QuietusAttachments.MANA_ATTACHMENT).addMana(12*(1+amplifier),livingEntity);
        return super.applyEffectTick(level,livingEntity,amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
    {
        return true;
    }
}
