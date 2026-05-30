package com.quietus.client.model;

import com.quietus.entity.monster.PlayerFragment;
import com.geckolib.constant.dataticket.DataTicket;

public class QuietusDataTickets {
    public static final DataTicket<PlayerFragment> PLAYER_FRAGMENT_ENTITY =
            DataTicket.create("player_fragment_entity", PlayerFragment.class);
}
