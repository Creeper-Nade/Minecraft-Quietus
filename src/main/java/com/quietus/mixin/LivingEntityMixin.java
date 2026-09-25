package com.quietus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.quietus.skill.QuietusSkills;
import com.quietus.util.SkillUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyReturnValue(method = "getAttributeValue", at = @At("RETURN"))
    private double quietus$modifyAttackSpeed(double original, Holder<Attribute> attribute) {
        if (Attributes.ATTACK_SPEED.equals(attribute) && (Object) this instanceof Player player) {
            double bonus = SkillUtil.getDoubleTotalSkillLevel(player, QuietusSkills.ATTACK_SPEED_MULT_BONUS.get());
            if (bonus != 0.0 && original > 0.0) {
                return Math.max(0.0, original * (1.0 + bonus));
            }
        }
        return original;
    }
}
