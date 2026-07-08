package com.quietus.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quietus.item.QuietusComponents;
import com.quietus.item.component.CanDecay;
import com.quietus.util.client.ContainerScreenUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
    
    @Inject(
        method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
        at = @At("HEAD")
    )
    private void onBeforeItemRendered(@Nullable LivingEntity owner, @Nullable Level level, ItemStack itemstack, int x, int y, int seed, CallbackInfo ci) {
        if (!itemstack.has(QuietusComponents.CAN_DECAY.get())) // skip immediately if this item does not have component
            return;

        Minecraft minecraft = Minecraft.getInstance();
        CanDecay decayComponent = itemstack.get(QuietusComponents.CAN_DECAY.get());
        int decay = itemstack.getOrDefault(QuietusComponents.DECAY.get(), 0).intValue();
        float fraction = decayComponent.getDecayFraction(decay); // CanDecay Component existance is already checked 5 lines above

        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;

        int width = 16; // 16 px for items
        int height = 16; // 16 px for items
        int outline_width = 1; // must not be negative
        int outline_height = 1; // must not be negative
        int height_offset = Math.round((height-1-outline_height*2) * fraction); // the height depending on how decayed is this item
        int color = decayComponent.getDisplayColor(decay, 0x88);
        int color_transparent = decayComponent.getDisplayColor(decay, 0x48);


        graphics.fill( 
            RenderPipelines.GUI, 
            x + outline_width, 
            y + height - height_offset - outline_height*2, 
            x + width - outline_width, 
            y + height - outline_height,
            color
        ); 
        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) { // only in container GUI
            if (ContainerScreenUtil.isMouseDraggingItem(containerScreen, itemstack)) {
                graphics.fill(RenderPipelines.GUI, x + outline_width, y + outline_height, x + width - outline_width, y + height - height_offset - outline_height*2, color_transparent);
            } else {
                graphics.fill(RenderPipelines.GUI, x, y, x + outline_width, y + height, color);
                graphics.fill(RenderPipelines.GUI, x + width - outline_width, y, x + width, y + height, color);
                graphics.fill(RenderPipelines.GUI, x + outline_width, y, x + width - outline_width, y + outline_height, color);
                graphics.fill(RenderPipelines.GUI, x + outline_width, y + height - outline_height, x + width - outline_width, y + height, color);
            }
        }
        
        graphics.nextStratum();
    }
}
