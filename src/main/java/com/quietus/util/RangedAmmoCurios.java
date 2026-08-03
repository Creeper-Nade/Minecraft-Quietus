package com.quietus.util;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class RangedAmmoCurios {
    public static final String SLOT_ID = "ammo";

    private RangedAmmoCurios() {
    }

    public static Optional<SlotResult> findAmmo(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findCurio(SLOT_ID, 0))
                .filter(result -> !result.stack().isEmpty());
    }
}
