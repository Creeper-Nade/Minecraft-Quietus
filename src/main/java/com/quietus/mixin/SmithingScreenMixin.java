package com.quietus.mixin;

import com.quietus.item.equipment.AmethystArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin {
    @Shadow
    @Final
    @Mutable
    private ArmorStandRenderState armorStandPreview;

    /**
     * Vanilla builds the smithing preview by modifying a bare render state. GeckoLib
     * captures armor render data while extracting a state from a living entity, so
     * use a temporary armor stand to run that normal extraction path for Geo armor.
     */
    @Inject(method = "updateArmorStandPreview", at = @At("TAIL"))
    private void quietus$extractGeoArmorPreview(ItemStack itemStack, CallbackInfo callbackInfo) {
        if (!(itemStack.getItem() instanceof AmethystArmorItem)) {
            return;
        }

        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        EquipmentSlot slot = equippable != null ? equippable.slot() : null;
        ClientLevel level = Minecraft.getInstance().level;
        if (slot == null || !slot.isArmor() || level == null) {
            return;
        }

        ArmorStand previewEntity = new ArmorStand(EntityType.ARMOR_STAND, level);
        previewEntity.setShowArms(true);
        previewEntity.setNoBasePlate(true);
        previewEntity.setItemSlot(slot, itemStack.copy());

        ArmorStandRenderer renderer = (ArmorStandRenderer) Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .getRenderer(previewEntity);
        ArmorStandRenderState renderState = renderer.createRenderState(previewEntity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        renderState.showBasePlate = false;
        renderState.showArms = true;
        renderState.xRot = 25.0F;
        renderState.bodyRot = 210.0F;
        this.armorStandPreview = renderState;
    }
}
