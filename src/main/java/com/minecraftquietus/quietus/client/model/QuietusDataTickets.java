package com.minecraftquietus.quietus.client.model;

import com.minecraftquietus.quietus.entity.monster.PlayerFragment;
import software.bernie.geckolib.constant.dataticket.DataTicket;

public class QuietusDataTickets {
    public static final DataTicket<PlayerFragment> PLAYER_FRAGMENT_ENTITY =
            DataTicket.create("player_fragment_entity", PlayerFragment.class);
}
