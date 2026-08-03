package com.quietus.magic;

import com.quietus.item.QuietusComponents;
import com.quietus.client.packet.MagicCastStartPacket;
import com.quietus.item.component.UsesMana;
import com.quietus.item.tool.MagicChantingWeaponItem;
import com.quietus.server.packet.MagicCastInputPacket;
import com.quietus.util.ManaUtil;
import com.quietus.enchantment.QuietusEnchantmentHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.quietus.Quietus.MODID;

@EventBusSubscriber(modid = MODID)
public final class MagicChantingServer {
    private static final float NETWORK_PROGRESS_TOLERANCE = 0.20F;
    private static final Map<UUID, CastSession> SESSIONS = new HashMap<>();

    private MagicChantingServer() {
    }

    public static void handle(ServerPlayer player, MagicCastInputPacket packet) {
        CastSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }

        if (packet.action() == MagicCastInputPacket.CANCEL) {
            release(player, session);
            return;
        }

        float progress = packet.progress();
        float serverProgress = (player.level().getGameTime() - session.startGameTime())
                / (float) session.pattern().durationTicks();
        boolean progressIsPlausible = Float.isFinite(progress)
                && progress >= session.lastProgress()
                && progress >= 0.0F
                && progress <= 1.05F
                && Math.abs(progress - serverProgress) <= NETWORK_PROGRESS_TOLERANCE;

        if (!progressIsPlausible) {
            release(player, session);
            return;
        }

        int processed = session.processedCheckpoints();
        float checkpointRadius = checkpointRadius(player, session);
        int skipped = 0;
        while (processed < session.generatedPattern().size()) {
            MagicChantingPattern.Checkpoint checkpoint = session.generatedPattern().checkpoints().get(processed);
            if (progress <= checkpoint.center() + checkpointRadius) {
                break;
            }
            processed++;
            skipped++;
        }

        CastSession progressed = new CastSession(
                session.startGameTime(), processed, session.successfulCheckpoints(), progress,
                session.hand(), session.pattern(), session.generatedPattern());
        if (processed == session.generatedPattern().size()) {
            release(player, progressed);
            return;
        }

        MagicChantingPattern.Checkpoint checkpoint = session.generatedPattern().checkpoints().get(processed);
        boolean correctButton =
                (packet.action() == MagicCastInputPacket.LEFT
                    && checkpoint.input() == MagicChantingPattern.Input.LEFT)
                || (packet.action() == MagicCastInputPacket.RIGHT
                    && checkpoint.input() == MagicChantingPattern.Input.RIGHT);
        boolean inWindow = Math.abs(progress - checkpoint.center()) <= checkpointRadius;

        if (inWindow && correctButton) {
            CastSession advanced = new CastSession(
                    session.startGameTime(), processed + 1, session.successfulCheckpoints() + 1, progress,
                    session.hand(), session.pattern(), session.generatedPattern());
            SESSIONS.put(player.getUUID(), advanced);
            if (advanced.processedCheckpoints() == advanced.generatedPattern().size()) {
                release(player, advanced);
            }
        } else if (inWindow) {
            release(player, progressed);
        } else if (skipped > 0) {
            SESSIONS.put(player.getUUID(), progressed);
        } else {
            release(player, progressed);
        }
    }

    public static void requestStart(ServerPlayer player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (SESSIONS.containsKey(player.getUUID())
                || !(weapon.getItem() instanceof MagicChantingWeaponItem)
                || player.getCooldowns().isOnCooldown(weapon)) {
            sendStartResult(player, false, 0L, hand);
            return;
        }
        StartResult result = start(player, hand);
        sendStartResult(player, result.accepted(), result.seed(), hand);
    }

    private static StartResult start(ServerPlayer player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (!(weapon.getItem() instanceof MagicChantingWeaponItem magicWeapon)) {
            return StartResult.REJECTED;
        }

        UsesMana usesMana = weapon.get(QuietusComponents.USES_MANA.get());
        if (usesMana != null) {
            int cost = usesMana.calculateConsumption(
                    ManaUtil.getMana(player), ManaUtil.getMaxMana(player), weapon, player.level());
            if (ManaUtil.getMana(player) < cost) {
                return StartResult.REJECTED;
            }
            ManaUtil.get(player).consumeMana(cost, player);
        }

        long seed = player.getRandom().nextLong();
        MagicChantingPattern pattern = magicWeapon.getChantingPattern();
        MagicChantingPattern.Generated generatedPattern = pattern.generate(seed);
        SESSIONS.put(player.getUUID(), new CastSession(
                player.level().getGameTime(), 0, 0, 0.0F, hand, pattern, generatedPattern));
        return new StartResult(true, seed);
    }

    private static void sendStartResult(ServerPlayer player, boolean accepted, long seed,
                                        InteractionHand hand) {
        PacketDistributor.sendToPlayer(player, new MagicCastStartPacket(
                accepted, seed, hand == InteractionHand.OFF_HAND ? 1 : 0));
    }

    private static void release(ServerPlayer player, CastSession session) {
        SESSIONS.remove(player.getUUID());
        ItemStack weapon = player.getItemInHand(session.hand());
        if (!(weapon.getItem() instanceof MagicChantingWeaponItem staff)) {
            return;
        }

        staff.fireChantingResult(player, session.hand(), weapon,
                session.successfulCheckpoints(), session.generatedPattern().size());
    }

    private static float checkpointRadius(ServerPlayer player, CastSession session) {
        return QuietusEnchantmentHelper.getAttunedCheckpointRadius(
                player.level(), player.getItemInHand(session.hand()), session.pattern().windowRadius());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CastSession session = SESSIONS.get(player.getUUID());
        if (session != null && player.level().getGameTime() - session.startGameTime()
                >= session.pattern().durationTicks()) {
            release(player, session);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID());
    }

    private record CastSession(long startGameTime, int processedCheckpoints,
                               int successfulCheckpoints, float lastProgress,
                               InteractionHand hand, MagicChantingPattern pattern,
                               MagicChantingPattern.Generated generatedPattern) {
    }

    private record StartResult(boolean accepted, long seed) {
        private static final StartResult REJECTED = new StartResult(false, 0L);
    }
}
