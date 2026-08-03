package com.quietus.server.handler;

import com.quietus.magic.MagicChantingServer;
import com.quietus.server.packet.MagicCastInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MagicCastPayloadHandler {
    private MagicCastPayloadHandler() {
    }

    public static void handle(MagicCastInputPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                MagicChantingServer.handle(player, packet);
            }
        });
    }
}
