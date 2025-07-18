package com.minecraftquietus.quietus.util.handler;

import com.minecraftquietus.quietus.core.DeathRevamp.GhostDeath;
import com.minecraftquietus.quietus.core.DeathRevamp.GhostMovementHandler;
import com.minecraftquietus.quietus.packet.GhostStatePacket;
import com.minecraftquietus.quietus.packet.ManaPacket;
import com.minecraftquietus.quietus.packet.PlayerRevivalCooldownPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandler {
    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }

    private static int MaxMana;
    private static int Mana;
    private static boolean ManaFastCharging;

    private static boolean PlayerIsGhost;
    private static boolean IsHardCore;
    private static int MaxReviveCD;
    private static int ReviveCD;


    public static void handleMana(final ManaPacket Mpack, final IPayloadContext context) {
        context.enqueueWork(() -> {
                    MaxMana = Mpack.MaxMana();
                    Mana = Mpack.Mana();
                    ManaFastCharging = Mpack.FastCharging();
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("quietus.networking.failed", e.getMessage()));
                    return null;
                });
    }
    public int GetMaxManaFromPack() {return MaxMana;}
    public boolean GetManaChargeStatus(){return ManaFastCharging;}
    public int GetManaFromPack() {return Mana;}

    public static void handleGhostState(final GhostStatePacket payload,final IPayloadContext context) {
        context.enqueueWork(() -> {
                    PlayerIsGhost = payload.isGhost();
                    MaxReviveCD = payload.Max_CD();
                    IsHardCore = payload.hardcore();
                    if(payload.isGhost())
                    {
                        GhostDeath.showScreen(payload.message());
                        GhostMovementHandler.Init();
                    }


                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("quietus.networking.failed", e.getMessage()));
                    return null;
                });

    }
    public boolean getGhostState(){return PlayerIsGhost;}
    public boolean getHardcore(){return IsHardCore;}
    public int getMaxReviveCD(){return MaxReviveCD;}

    public static void handleReviveCD(final PlayerRevivalCooldownPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
                    ReviveCD = payload.cooldown();
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("quietus.networking.failed", e.getMessage()));
                    return null;
                });

    }
    public int GetReviveCD(){return ReviveCD;}
}
