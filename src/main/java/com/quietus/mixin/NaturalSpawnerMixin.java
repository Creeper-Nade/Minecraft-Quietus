package com.quietus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.SpawnDiagnostics;
import com.quietus.world.threat.WorldThreatSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public final class NaturalSpawnerMixin {
    private static final int ZOMBIE_PACK_PLACEMENT_RETRIES = 12;
    private static final ThreadLocal<Boolean> QUIETUS_ZOMBIE_PACK = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> QUIETUS_ZOMBIE_PACK_TARGET = ThreadLocal.withInitial(() -> 0);

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;minCount()I"))
    private static int quietus$increaseZombiePackMinimum(
            MobSpawnSettings.SpawnerData spawnData,
            Operation<Integer> original,
            MobCategory category,
            ServerLevel level,
            ChunkAccess chunk,
            BlockPos start,
            NaturalSpawner.SpawnPredicate extraTest,
            NaturalSpawner.AfterSpawnCallback spawnCallback) {
        return original.call(spawnData) + quietus$zombiePackBonus(spawnData, level);
    }

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;maxCount()I"))
    private static int quietus$increaseZombiePackMaximum(
            MobSpawnSettings.SpawnerData spawnData,
            Operation<Integer> original,
            MobCategory category,
            ServerLevel level,
            ChunkAccess chunk,
            BlockPos start,
            NaturalSpawner.SpawnPredicate extraTest,
            NaturalSpawner.AfterSpawnCallback spawnCallback) {
        int vanillaMaximum = original.call(spawnData);
        int bonus = quietus$zombiePackBonus(spawnData, level);
        boolean zombiePack = quietus$isZombieFamily(spawnData.type());
        QUIETUS_ZOMBIE_PACK.set(zombiePack);
        QUIETUS_ZOMBIE_PACK_TARGET.set(0);
        if (zombiePack) {
            int vanillaMinimum = spawnData.minCount();
            SpawnDiagnostics.recordZombiePackSelection(
                    vanillaMinimum,
                    vanillaMaximum,
                    vanillaMinimum + bonus,
                    vanillaMaximum + bonus);
        }
        return vanillaMaximum + bonus;
    }

    /**
     * Vanilla uses the requested pack size as its number of placement attempts, so every rejected
     * position permanently removes one member. Preserve the requested size as the success target,
     * then give zombie-family packs a small retry budget for rejected positions.
     */
    @Inject(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;isValidSpawnPostitionForType(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;Lnet/minecraft/core/BlockPos$MutableBlockPos;D)Z"))
    private static void quietus$addZombiePackPlacementRetries(
            MobCategory category,
            ServerLevel level,
            ChunkAccess chunk,
            BlockPos start,
            NaturalSpawner.SpawnPredicate extraTest,
            NaturalSpawner.AfterSpawnCallback spawnCallback,
            CallbackInfo callback,
            @Local(ordinal = 6) LocalIntRef attempts) {
        if (!QUIETUS_ZOMBIE_PACK.get() || QUIETUS_ZOMBIE_PACK_TARGET.get() != 0) {
            return;
        }

        int target = attempts.get();
        QUIETUS_ZOMBIE_PACK_TARGET.set(target);
        attempts.set(target + ZOMBIE_PACK_PLACEMENT_RETRIES);
        SpawnDiagnostics.recordZombieRetryPack(target);
    }

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;getMaxSpawnClusterSize(Lnet/minecraft/world/entity/Mob;)I"))
    private static int quietus$recordZombiePackTargetBeforeClusterReturn(
            Mob mob, Operation<Integer> original, @Local(ordinal = 7) int groupSize) {
        int cap = original.call(mob);
        int target = QUIETUS_ZOMBIE_PACK_TARGET.get();
        if (QUIETUS_ZOMBIE_PACK.get() && target > 0 && groupSize >= target) {
            SpawnDiagnostics.recordZombiePackReachedTarget();
        }
        return cap;
    }

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;isMaxGroupSizeReached(I)Z"))
    private static boolean quietus$stopZombiePackAtRequestedSize(
            Mob mob, int groupSize, Operation<Boolean> original) {
        if (original.call(mob, groupSize)) {
            return true;
        }

        int target = QUIETUS_ZOMBIE_PACK_TARGET.get();
        return QUIETUS_ZOMBIE_PACK.get() && target > 0 && groupSize >= target;
    }

    @ModifyConstant(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            constant = @Constant(intValue = 3),
            expect = 1)
    private static int quietus$occasionallyAddHostilePackAttempt(
            int vanillaAttempts,
            MobCategory category,
            ServerLevel level) {
        if (category != MobCategory.MONSTER) {
            return vanillaAttempts;
        }

        double threat = WorldThreatData.get(level).getThreat();
        boolean granted = level.getRandom().nextDouble()
                < WorldThreatSystem.extraHostilePackAttemptChance(threat);
        SpawnDiagnostics.recordHostileAttempt(granted);
        return granted ? vanillaAttempts + 1 : vanillaAttempts;
    }

    private static int quietus$zombiePackBonus(MobSpawnSettings.SpawnerData spawnData, ServerLevel level) {
        if (!quietus$isZombieFamily(spawnData.type())) {
            return 0;
        }

        return WorldThreatSystem.zombiePackSizeBonus(WorldThreatData.get(level).getThreat());
    }

    private static boolean quietus$isZombieFamily(EntityType<?> type) {
        return type == EntityType.ZOMBIE
                || type == EntityType.HUSK
                || type == EntityType.DROWNED
                || type == EntityType.ZOMBIE_VILLAGER;
    }
}
