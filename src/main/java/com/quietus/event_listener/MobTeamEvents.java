package com.quietus.event_listener;

import com.quietus.Quietus;
import com.quietus.entity.team.MobTeamManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Quietus.MODID)
public final class MobTeamEvents {
    private MobTeamEvents() {
    }

    @SubscribeEvent
    public static void registerReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(Quietus.MODID, "mob_teams"),
                MobTeamManager.INSTANCE
        );
    }

    @SubscribeEvent
    public static void preventFriendlyTargeting(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target != null && MobTeamManager.INSTANCE.areAllies(event.getEntity(), target)) {
            event.setNewAboutToBeSetTarget(null);
            if (event.getEntity() instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
        }
    }

    @SubscribeEvent
    public static void preventFriendlyDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && MobTeamManager.INSTANCE.areAllies(attacker, event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
