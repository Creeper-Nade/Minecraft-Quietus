package com.minecraftquietus.quietus.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;

import com.minecraftquietus.quietus.client.QuietusKeyBindings;
import com.minecraftquietus.quietus.client.handler.ClientSkillTreePayloadHandler;
import com.minecraftquietus.quietus.client.screens.skill_tree.SkillTreeScreen;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    
    @Inject(
        method = "keyPressed",
        at = @At(
            value = "HEAD"
        ),
        cancellable = true
    )
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (QuietusKeyBindings.SKILL_TREE_KEY.get().matches(event)) {
            Minecraft.getInstance().setScreen(new SkillTreeScreen(ClientSkillTreePayloadHandler.getSkillTree()));
            cir.setReturnValue(true);
        }
    }
}
