package com.quietus.item.tool;

import com.quietus.item.tooltip.MagicWeaponControlsTooltip;
import com.quietus.magic.MagicChantingPattern;
import com.quietus.magic.MagicChantingServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class MagicChantingWeaponItem extends QuietusProjectileWeaponItem {
    private final MagicChantingPattern chantingPattern;

    public MagicChantingWeaponItem(Item.Properties properties, MagicChantingPattern chantingPattern) {
        super(properties);
        this.chantingPattern = chantingPattern;
    }

    public MagicChantingPattern getChantingPattern() {
        return chantingPattern;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return Optional.of(MagicWeaponControlsTooltip.INSTANCE);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            MagicChantingServer.requestStart(serverPlayer, hand);
        }
        // Consume the interaction without requesting vanilla's swing animation;
        // Chanting supplies its own per-check item-use animation and release swing.
        return InteractionResult.CONSUME;
    }
}
