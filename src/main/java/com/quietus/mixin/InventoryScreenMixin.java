package com.quietus.mixin;

import com.quietus.client.hud.VoidOrreryInventoryDisplay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void quietus$renderVoidOrrery(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                         float partialTick, CallbackInfo callbackInfo) {
        int inventoryWidth = 176;
        int inventoryHeight = 166;
        AbstractContainerScreenAccessor screen = (AbstractContainerScreenAccessor) this;
        int inventoryLeft = screen.quietus$getLeftPos();
        int inventoryTop = screen.quietus$getTopPos();
        VoidOrreryInventoryDisplay.render(graphics, inventoryLeft, inventoryTop, inventoryWidth, inventoryHeight);
    }
}
