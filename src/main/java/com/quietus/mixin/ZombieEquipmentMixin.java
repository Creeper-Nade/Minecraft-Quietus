package com.quietus.mixin;

import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import com.quietus.world.threat.SpawnDiagnostics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Zombie.class)
public abstract class ZombieEquipmentMixin {
    @ModifyConstant(
            method = "finalizeSpawn",
            constant = @Constant(doubleValue = 0.05D),
            expect = 2)
    private double quietus$increaseChickenJockeyChance(double vanillaChance) {
        Zombie zombie = (Zombie) (Object) this;
        if (zombie.getSpawnType() != EntitySpawnReason.NATURAL
                || !(zombie.level() instanceof ServerLevel level)) {
            return vanillaChance;
        }

        SpawnDiagnostics.recordJockeyRoll(SpawnDiagnostics.JockeyType.CHICKEN);
        return WorldThreatSystem.chickenJockeyRollChance(WorldThreatData.get(level).getThreat());
    }

    @ModifyConstant(
            method = "populateDefaultEquipmentSlots",
            constant = @Constant(floatValue = 0.01F))
    private float quietus$increaseNormalZombieWeaponChance(float vanillaChance) {
        return quietus$weaponChance(vanillaChance);
    }

    @ModifyConstant(
            method = "populateDefaultEquipmentSlots",
            constant = @Constant(floatValue = 0.05F))
    private float quietus$increaseHardZombieWeaponChance(float vanillaChance) {
        return quietus$weaponChance(vanillaChance);
    }

    private float quietus$weaponChance(float vanillaChance) {
        Zombie zombie = (Zombie) (Object) this;
        if (!(zombie.level() instanceof ServerLevel level)) {
            return vanillaChance;
        }

        return WorldThreatSystem.zombieWeaponChance(
                vanillaChance, WorldThreatData.get(level).getThreat());
    }
}
