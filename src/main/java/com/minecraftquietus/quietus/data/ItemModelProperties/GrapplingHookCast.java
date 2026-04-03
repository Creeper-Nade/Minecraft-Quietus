package com.minecraftquietus.quietus.data.ItemModelProperties;

import com.minecraftquietus.quietus.client.handler.ClientPayloadHandler;
import com.minecraftquietus.quietus.core.GrapplingHookAttachment;
import com.minecraftquietus.quietus.item.QuietusComponents;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public record GrapplingHookCast() implements ConditionalItemModelProperty {
    public static final MapCodec<GrapplingHookCast> MAP_CODEC = MapCodec.unit(new GrapplingHookCast());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level,
                       @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if(entity==null) return false;
        //GrapplingHookAttachment attachment = entity.getData(QuietusAttachments.GRAPPLE_ATTACHMENT);
        return stack.get(QuietusComponents.GRAPPLING_HOOK_CAST.get()) != null && ClientPayloadHandler.getInstance().GetHookActivity();
    }

    @Override
    public MapCodec<GrapplingHookCast> type() {
        return MAP_CODEC;
    }
}