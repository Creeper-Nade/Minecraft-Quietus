package com.quietus.core.mana;

import com.quietus.util.PlayerClientPacketDistributor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;


public class ManaComponent implements ValueIOSerializable {
    private static final MovementBonusFunction MOVEMENT_BONUS_FUNC = ((sneaking, stationary) -> {
        int a = sneaking ? 1 : 0;
        int b = stationary ? 1 : 0;
        return (1 + b * 0.8d) * (1 + a * 0.2d);
    });

    private int mana;
    private int maxMana = 20;
    private double movementMult = 1.0;
    private double accumulatedManaCharge;
    private int regenBonus = 0;
    private int regenDelay = 0;
    private boolean dirty = false;

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("mana", this.mana);
        output.putInt("max_mana", this.maxMana);
        output.putInt("regen_delay", this.regenDelay);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.mana = input.getIntOr("mana", 0);
        this.maxMana = input.getIntOr("max_mana", 20);
        this.regenDelay = input.getIntOr("regen_delay", 0);
    }

    public void tick() {
        if (this.regenDelay > 0) {
            this.regenDelay--;
        }

        if (this.regenDelay <= 0 && !this.isFull()) {
            this.regenMana();
        }
    }

    private void regenMana() {
        if (this.maxMana <= 0) {
            return;
        }

        double fillRatio = (double) this.mana / this.maxMana;
        double fillMult = fillRatio * 0.18 + 0.2;
        this.accumulatedManaCharge += 2.5 * 1.15 * ((double) this.maxMana / 3.0 + 1.0 + this.regenBonus) * fillMult * this.movementMult;

        if (this.accumulatedManaCharge >= 40.0) {
            int addedMana = (int) Math.floor(this.accumulatedManaCharge / 40.0);
            this.accumulatedManaCharge -= 40.0 * addedMana;
            this.addMana(addedMana);
        }
    }

    public void makeDelay() {
        if (this.maxMana <= 0) {
            this.regenDelay = 0;
            return;
        }
        this.regenDelay = (int) Math.floor((1.0 / 3.0) * Math.floor(0.7 * (120.0 * (1.0 - (double) this.mana / this.maxMana) + 45.0)));
    }

    public void updateMovementBonus(boolean sneaking, boolean stationary) {
        double newMult = MOVEMENT_BONUS_FUNC.apply(sneaking, stationary);
        if (Double.compare(this.movementMult, newMult) != 0) {
            this.movementMult = newMult;
            this.dirty = true;
        }
    }

    public boolean isFull() {
        return this.mana >= this.maxMana;
    }

    public void setMana(int amount) {
        int newMana = Math.clamp(amount, 0, this.maxMana);
        if (this.mana != newMana) {
            this.mana = newMana;
            this.dirty = true;
        }
    }

    public void consumeMana(int amount) {
        if (amount <= 0) return;
        this.mana = Math.max(0, this.mana - amount);
        this.makeDelay();
        this.dirty = true;
    }

    public void addMana(int amount) {
        if (amount == 0) return;
        int newMana = Math.clamp(this.mana + amount, 0, this.maxMana);
        if (this.mana != newMana) {
            this.mana = newMana;
            this.dirty = true;
        }
    }

    public void setMaxMana(int amount) {
        if (this.maxMana != amount) {
            this.maxMana = amount;
            if (this.mana > this.maxMana) {
                this.mana = this.maxMana;
            }
            this.dirty = true;
        }
    }

    public void setRegenBonus(int bonus) {
        this.regenBonus = bonus;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clean() {
        this.dirty = false;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getMana() {
        return this.mana;
    }

    public int getMaxMana() {
        return this.maxMana;
    }

    public int getRegenBonus() {
        return this.regenBonus;
    }

    public int getRegenDelay() {
        return this.regenDelay;
    }

    public double getMovementMult() {
        return this.movementMult;
    }

    public double getAccumulatedManaCharge() {
        return this.accumulatedManaCharge;
    }

    @FunctionalInterface
    private interface MovementBonusFunction {
        double apply(boolean sneaking, boolean stationary);
    }

}

