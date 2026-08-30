package com.quietus.event_listener;

import java.util.Objects;

import com.quietus.Quietus;
import com.quietus.skill.QuietusSkills;
import com.quietus.tags.QuietusTags;
import com.quietus.util.ManaUtil;
import com.quietus.util.SkillUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Quietus.MODID)
public class SkillEffectsEvents {

    @SubscribeEvent
    public static void applyDamageBonuses(LivingIncomingDamageEvent event) {
        float damage = event.getAmount();
        float bonus = 0.0f;
        if (Objects.isNull(event.getSource().getEntity())) {
            return;
        }
        if (event.getSource().getEntity() instanceof Player sourcePlayer) {
            if (event.getSource().is(QuietusTags.DamageTypes.BUFFABLE_MAGIC)) {
                double magicMultBonus = (double)SkillUtil.getTotalSkillLevel(sourcePlayer, QuietusSkills.MAGIC_DAMAGE_MULT_BONUS.get());
                bonus += damage * magicMultBonus;
            }
        }

        if (bonus != 0.0f) {
            event.setAmount(damage + bonus);
        }
    }
    
}
