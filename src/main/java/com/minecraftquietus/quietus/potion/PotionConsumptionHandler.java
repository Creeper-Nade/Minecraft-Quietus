package com.minecraftquietus.quietus.potion;

import com.minecraftquietus.quietus.core.ManaComponent;
import com.minecraftquietus.quietus.util.QuietusAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PotionConsumptionHandler {
    /*
    // Default mana values for potions
    private static final Map<ResourceLocation, Integer> MANA_VALUES = new HashMap<>();
    private static final int DEFAULT_MANA = 15; // Default value

    static {
        // Register vanilla potions
        registerPotionMana("minecraft", "awkward", 4);
        // Add more potions as needed
    }

    public static void registerPotionMana(String Namespace,String potionId, int mana) {
        MANA_VALUES.put(ResourceLocation.fromNamespaceAndPath(Namespace,potionId), mana);
    }


    @SubscribeEvent
    public static void onPotionConsumed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getItem().getItem() instanceof PotionItem)
        {
            ItemStack stack = event.getItem();
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

            if (contents == null) return; // Not a potion

            Optional<Holder<Potion>> potionHolder = contents.potion();
            if (potionHolder.isEmpty()) return;

            // Get potion ID from holder
            ResourceLocation potionId = potionHolder.get()
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);

            if (potionId == null) return;

            // Get mana amount from registry
            int manaAmount = MANA_VALUES.getOrDefault(potionId, DEFAULT_MANA);

            // Apply mana to player
            player.getData(QuietusAttachments.MANA_ATTACHMENT).addMana(manaAmount,player);
        }

    }*/
}
