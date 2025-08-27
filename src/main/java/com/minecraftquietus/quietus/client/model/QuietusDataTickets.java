package com.minecraftquietus.quietus.client.model;

import com.minecraftquietus.quietus.entity.monster.PlayerGhost;
import software.bernie.geckolib.constant.dataticket.DataTicket;

public class QuietusDataTickets {
    public static final DataTicket<PlayerGhost> PLAYER_GHOST_ENTITY =
            DataTicket.create("player_ghost_entity", PlayerGhost.class);
}
