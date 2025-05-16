package com.minecraftquietus.quietus.mixin;

import com.minecraftquietus.quietus.event.QuietusCommonEvents;
import com.minecraftquietus.quietus.util.mana.ManaComponent;
import com.minecraftquietus.quietus.util.mana.ManaHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
        Player player = QuietusCommonEvents.QuietusServerPlayer;
        if (player != null && player.getData(ManaComponent.ATTACHMENT) != null) {
            //System.out.println(1);
            ManaComponent mana = player.getData(ManaComponent.ATTACHMENT);
            int rows = mana.getRowCount();
            //System.out.println(rows);
            return originalY - (rows * 10 ); // 10px per row + 5px buffer
        }
        return originalY;
    }
}
