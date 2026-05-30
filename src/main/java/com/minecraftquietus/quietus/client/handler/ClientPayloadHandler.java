package com.minecraftquietus.quietus.client.handler;

import java.util.Objects;

import com.minecraftquietus.quietus.client.packet.DoDecayPacket;
import com.minecraftquietus.quietus.client.packet.GhostStatePacket;
import com.minecraftquietus.quietus.client.packet.ManaPacket;
import com.minecraftquietus.quietus.client.packet.PlayerRevivalCooldownPacket;
import com.minecraftquietus.quietus.client.packet.WeatherItemContainerPacket;
import com.minecraftquietus.quietus.core.DeathRevamp.GhostDeath;
import com.minecraftquietus.quietus.core.DeathRevamp.GhostMovementHandler;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.item.tool.GrapplingHookItem;
import com.minecraftquietus.quietus.server.packet.GrapplingJumpReleasePacket;
import com.minecraftquietus.quietus.client.packet.GrapplingActiveHookPacket;
import com.minecraftquietus.quietus.client.packet.GrapplingHookPhysicsPacket;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;


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

    private static boolean hasActiveHook=false;
    private static int activeHookID;

    private static Minecraft minecraft = Minecraft.getInstance();


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


    public static void handleGhostState(final GhostStatePacket payload, final IPayloadContext context) {
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
    public static void handleReviveCD(final PlayerRevivalCooldownPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
                    ReviveCD = payload.cooldown();
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("quietus.networking.failed", e.getMessage()));
                    return null;
                });

    }

    public static void handleDoDecay(final DoDecayPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = minecraft.level;
            if (Objects.nonNull(level)) {
                Entity entity = level.getEntity(payload.entityId());
                ItemStack itemstack = ItemStack.EMPTY;
                if (entity instanceof LivingEntity livingEntity) {
                    itemstack = livingEntity.getItemBySlot(payload.slot());
                } else if (entity instanceof ItemFrame itemFrame) {
                    itemstack = itemFrame.getItem();
                }
                if (!itemstack.isEmpty()) {
                    if (itemstack.has(QuietusComponents.CAN_DECAY.get())) {
                        itemstack.get(QuietusComponents.CAN_DECAY.get()).changeDecayAndMakeConvertedItemIfDecayed(itemstack, 1);
                    }
                }
            }
        })
        .exceptionally(e -> {
            context.disconnect(Component.translatable("quietus.networking.failed", e.getMessage()));
            return null;
        });
    }
    public static void handleWeatherItemContainer(final WeatherItemContainerPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = minecraft.level;
            if (Objects.nonNull(level)) {
                Entity entity = level.getEntity(payload.entityId());
                ItemStack itemstack = ItemStack.EMPTY;
                if (entity instanceof LivingEntity livingEntity) {
                    itemstack = livingEntity.getItemBySlot(payload.slot());
                } else if (entity instanceof ItemFrame itemFrame) {
                    itemstack = itemFrame.getItem();
                }
                if (!itemstack.isEmpty()) {
                    if (itemstack.has(DataComponents.CONTAINER)) {
                        itemstack.set(DataComponents.CONTAINER, payload.containerContents());
                    }
                }
            }
        })
        .exceptionally(e -> {
            context.disconnect(Component.translatable("quietus.networking.failed", e.getMessage()));
            return null;
        });
    }
    public static void handleGrapplePhysics(GrapplingHookPhysicsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                //LOGGER.debug("Client received velocity: {}", packet.toVelocity());
                // Apply the velocity directly on the client
                Vec3 currentMotion = player.getDeltaMovement();
                Vec3 newMotion = packet.toVelocity();
                player.setDeltaMovement(newMotion);

                // Also set a flag to indicate we're controlling movement
                player.getPersistentData().putBoolean("QuietusGrappling", true);
            }
        });
    }
    public static void handleGrappleJump(GrapplingJumpReleasePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!player.level().isClientSide() && player.getData(QuietusAttachments.GRAPPLE_ATTACHMENT).hasActiveHook()) {
                GrapplingHookItem.retrieveHookForPlayer(player);
            }
        });
    }
    public static void handleGrappleActivity(GrapplingActiveHookPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            hasActiveHook= packet.active();
            activeHookID= packet.hookEntityId();
        });
    }
    public int GetMaxManaFromPack() {return MaxMana;}
    public boolean GetManaChargeStatus(){return ManaFastCharging;}
    public int GetManaFromPack() {return Mana;}
    public boolean getGhostState(){return PlayerIsGhost;}
    public boolean getHardcore(){return IsHardCore;}
    public int getMaxReviveCD(){return MaxReviveCD;}
    public int GetReviveCD(){return ReviveCD;}
    public boolean GetHookActivity(){return hasActiveHook;}

    public int getActiveHookID() {
        return activeHookID;
    }
}
