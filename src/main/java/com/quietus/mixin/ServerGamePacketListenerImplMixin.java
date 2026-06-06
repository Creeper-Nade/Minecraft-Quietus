package com.quietus.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplMixin {
    @Accessor("waitingForRespawn")
    void setWaitingForRespawn(boolean waitingForRespawn);

    @Accessor("clientLoadedTimeoutTimer")
    void setClientLoadedTimeoutTimer(int timer);
}
