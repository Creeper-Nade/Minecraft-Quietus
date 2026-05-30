package com.quietus.client.multiplayer;

public interface ClientSkillTreeListener {
    abstract void onClientSkillTreeUpdate(int amount, int maxAmount, int progressAmount);
}
