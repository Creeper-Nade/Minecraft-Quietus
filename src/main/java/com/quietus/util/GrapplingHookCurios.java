package com.quietus.util;

import com.quietus.item.tool.GrapplingHookItem;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class GrapplingHookCurios {
    public static final String SLOT_ID = "grappling_hook";

    private GrapplingHookCurios() {
    }

    public static Optional<SlotResult> findEquippedHook(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findCurio(SLOT_ID, 0))
                .filter(result -> result.stack().getItem() instanceof GrapplingHookItem);
    }
}
