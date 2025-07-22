package com.minecraftquietus.quietus.mixin;

import com.minecraftquietus.quietus.client.hud.ManaHudOverlay;
import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.event_listener.QuietusCommonEvents;
import com.minecraftquietus.quietus.util.QuietusAttachments;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// mixin/GuiMixin.java
@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyArg(
            method = "renderAirLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V"
            ),
            index = 3 // Y position parameter index
    )
    private int adjustOxygenBarY(int originalY) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            //System.out.println(1);

            int rows = ManaHudOverlay.getRowCount();
            //System.out.println(rows);
            return originalY - ((rows-1) * (10- ManaHudOverlay.row_space))-10; // 10px per row + 5px buffer
        }
        return originalY;
    }
}
