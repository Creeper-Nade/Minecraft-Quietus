package com.minecraftquietus.quietus.client.packet;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.minecraftquietus.quietus.Quietus.MODID;

public record GrapplingHookPhysicsPacket(double x, double y, double z) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomPacketPayload.Type<GrapplingHookPhysicsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID,"grappling_hook_physics"));

    public static final StreamCodec<FriendlyByteBuf, GrapplingHookPhysicsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeDouble(packet.x);
                buf.writeDouble(packet.y);
                buf.writeDouble(packet.z);
            },
            buf -> new GrapplingHookPhysicsPacket(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    public static GrapplingHookPhysicsPacket fromVelocity(Vec3 velocity) {
        return new GrapplingHookPhysicsPacket(velocity.x, velocity.y, velocity.z);
    }

    public Vec3 toVelocity() {
        return new Vec3(x, y, z);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


}
