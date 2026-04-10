package com.minecraftquietus.quietus.server.packet;

import com.minecraftquietus.quietus.item.tool.GrapplingHookItem;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.minecraftquietus.quietus.Quietus.MODID;

public record GrapplingJumpReleasePacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GrapplingJumpReleasePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"grappling_hook_jump_reset"));

    public static final StreamCodec<FriendlyByteBuf, GrapplingJumpReleasePacket> STREAM_CODEC = StreamCodec.unit(new GrapplingJumpReleasePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


}