package com.quietus.mixin;

import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import com.quietus.world.threat.SpawnDiagnostics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.zombie.Husk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Husk.class)
public abstract class HuskJockeyMixin {
    @ModifyConstant(
            method = "finalizeSpawn",
            constant = @Constant(floatValue = 0.10F),
            expect = 1)
    private float quietus$increaseCamelHuskJockeyChance(float vanillaChance) {
        Husk husk = (Husk) (Object) this;
        if (husk.getSpawnType() != EntitySpawnReason.NATURAL
                || !(husk.level() instanceof ServerLevel level)) {
            return vanillaChance;
        }

        SpawnDiagnostics.recordJockeyRoll(SpawnDiagnostics.JockeyType.CAMEL_HUSK);
        return WorldThreatSystem.camelHuskJockeyChance(WorldThreatData.get(level).getThreat());
    }
}
