package com.quietus.event_listener;

import java.util.Objects;

import com.quietus.Quietus;
import com.quietus.mixin.MobEffectInstanceAccessor;
import com.quietus.skill.QuietusSkills;
import com.quietus.tags.QuietusTags;
import com.quietus.util.ManaUtil;
import com.quietus.util.SkillUtil;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = Quietus.MODID)
public class SkillImplementationEvents {

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

    @SubscribeEvent 
    public static void mitigateDebuffEffects(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance newEffect = event.getEffectInstance();
            MobEffectInstance oldEffect = event.getOldEffectInstance();
            if (newEffect == null || newEffect.isInfiniteDuration()) {
                return;
            }
            
            if (newEffect.getEffect().is(QuietusTags.MobEffects.DEBUFFS)) {
                int modifiedDuration = (int)Math.ceil(newEffect.getDuration() * (1.0d - (double)SkillUtil.getTotalSkillLevel(player, QuietusSkills.DEBUFF_MITIGATION.get())));
                
                ((MobEffectInstanceAccessor)newEffect).quietus$setDuration(modifiedDuration);
                
                if (oldEffect != null && !oldEffect.isInfiniteDuration() && oldEffect.getDuration() > modifiedDuration) {
                    ((MobEffectInstanceAccessor)oldEffect).quietus$setDuration(modifiedDuration);
                    if (player instanceof ServerPlayer serverPlayer) { // ensure sync to client
                        serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), oldEffect, false));
                    }
                }
            }
            
        }
        
        
    }
    
    @SubscribeEvent 
    public static void applyDamageSurpression(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            double damageSupressionLvl = (double)SkillUtil.getTotalSkillLevel(player, QuietusSkills.DAMAGE_SUPRESSION.get());
            if ((double)SkillUtil.getTotalSkillLevel(player, QuietusSkills.DAMAGE_SUPRESSION.get()) == 0d) {
                return;
            }
            float dmg = event.getNewDamage();
            float supressedDamage;
            /* For the graph of the function below, see damage_surpression_calculation.png.
             * When dmg = damageSupressionLvl, the ratio between damage supressed and original 
             * damage is (e^(-1/(2ln(2))) * ln(2)) ≈ 0.337. */
            double c1 = 2*Math.log(2);
            if (dmg < damageSupressionLvl) {
                supressedDamage = (float)damageSupressionLvl * (float)Math.log(dmg/(float)damageSupressionLvl + 1) * (float)Math.exp(-dmg/c1/damageSupressionLvl);
            } else {
                supressedDamage = (float) ((float)damageSupressionLvl * (float)Math.log(2) * Math.exp(
                    - Math.pow(dmg-damageSupressionLvl, 2) / (damageSupressionLvl*damageSupressionLvl) - 1/(c1)
                ));
            }
            if (supressedDamage >= 1e-4) {
                event.setNewDamage(dmg - supressedDamage);
            }
            System.out.println("[Debugg] original damage: " + dmg + ", new damage: " + (dmg - supressedDamage));
        }
    }
    
}
