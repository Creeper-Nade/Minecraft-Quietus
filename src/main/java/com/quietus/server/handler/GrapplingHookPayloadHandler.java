package com.quietus.server.handler;

import com.quietus.item.tool.GrapplingHookItem;
import com.quietus.server.packet.GrapplingHookActionPacket;
import com.quietus.util.GrapplingHookCurios;
import com.quietus.util.QuietusAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class GrapplingHookPayloadHandler {
    private GrapplingHookPayloadHandler() {
    }

    public static void handleAction(GrapplingHookActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT).hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
                return;
            }

            GrapplingHookCurios.findEquippedHook(player).ifPresent(result -> {
                if (result.stack().getItem() instanceof GrapplingHookItem hook) {
                    hook.useFromCurio(player, result.stack());
                }
            });
        });
    }
}
