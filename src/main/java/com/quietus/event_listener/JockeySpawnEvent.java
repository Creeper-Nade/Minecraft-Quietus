package com.quietus.event_listener;

import com.quietus.Quietus;
import com.quietus.world.threat.SpawnDiagnostics;
import com.quietus.world.threat.WorldThreatData;
import com.quietus.world.threat.WorldThreatSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Quietus.MODID)
public final class JockeySpawnEvent {
    private static final List<Zombie> PENDING_HORSEMEN = new ArrayList<>();

    private JockeySpawnEvent() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        recordExistingJockey(event.getEntity());

        if (!(event.getEntity() instanceof Zombie zombie)
                || zombie.getType() != EntityType.ZOMBIE
                || zombie.getSpawnType() != EntitySpawnReason.NATURAL
                || zombie.isBaby()
                || zombie.isPassenger()) {
            return;
        }

        SpawnDiagnostics.recordJockeyRoll(SpawnDiagnostics.JockeyType.ZOMBIE_HORSEMAN);
        double threat = WorldThreatData.get(level).getThreat();
        if (level.getRandom().nextFloat() < WorldThreatSystem.zombieHorsemanChance(threat)) {
            SpawnDiagnostics.recordJockeyRollPassed(SpawnDiagnostics.JockeyType.ZOMBIE_HORSEMAN);
            PENDING_HORSEMEN.add(zombie);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_HORSEMEN.isEmpty()) {
            return;
        }

        List<Zombie> pending = List.copyOf(PENDING_HORSEMEN);
        PENDING_HORSEMEN.clear();
        for (Zombie zombie : pending) {
            createHorseman(zombie);
        }
    }

    private static void createHorseman(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level)
                || !zombie.isAlive()
                || !zombie.isAddedToLevel()
                || zombie.isPassenger()) {
            return;
        }

        ZombieHorse horse = EntityType.ZOMBIE_HORSE.create(level, EntitySpawnReason.JOCKEY);
        if (horse == null) {
            return;
        }

        horse.snapTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), 0.0F);
        EventHooks.finalizeMobSpawn(
                horse,
                level,
                level.getCurrentDifficultyAt(zombie.blockPosition()),
                EntitySpawnReason.JOCKEY,
                null);
        if (horse.isSpawnCancelled() || !level.addFreshEntity(horse)) {
            horse.discard();
            return;
        }

        if (!zombie.startRiding(horse, true, false)) {
            horse.discard();
            return;
        }

        zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
        SpawnDiagnostics.recordJockeyCreated(SpawnDiagnostics.JockeyType.ZOMBIE_HORSEMAN);
    }

    private static void recordExistingJockey(Entity rider) {
        Entity vehicle = rider.getVehicle();
        if (vehicle == null) {
            return;
        }

        if (rider instanceof Zombie && vehicle.getType() == EntityType.CHICKEN) {
            SpawnDiagnostics.recordJockeyCreated(SpawnDiagnostics.JockeyType.CHICKEN);
        } else if (rider instanceof AbstractSkeleton && vehicle.getType() == EntityType.SPIDER) {
            SpawnDiagnostics.recordJockeyCreated(SpawnDiagnostics.JockeyType.SPIDER);
        } else if (rider.getType() == EntityType.HUSK && vehicle.getType() == EntityType.CAMEL_HUSK) {
            SpawnDiagnostics.recordJockeyCreated(SpawnDiagnostics.JockeyType.CAMEL_HUSK);
        } else if (rider.getType() == EntityType.DROWNED && vehicle.getType() == EntityType.ZOMBIE_NAUTILUS) {
            SpawnDiagnostics.recordJockeyCreated(SpawnDiagnostics.JockeyType.ZOMBIE_NAUTILUS);
        } else if (rider.getType() == EntityType.ZOMBIE && vehicle.getType() == EntityType.ZOMBIE_HORSE) {
            SpawnDiagnostics.recordJockeyCreated(SpawnDiagnostics.JockeyType.ZOMBIE_HORSEMAN);
        }
    }
}
