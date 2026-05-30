package com.quietus.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class GrapplingHookAttachment implements ValueIOSerializable {
    private int hookEntityId = -1;
    public GrapplingHookAttachment() {}

    public int getHookEntityId() {
        return hookEntityId;
    }

    public void setHookEntityId(int id) {
        this.hookEntityId = id;
    }

    public void clear() {
        this.hookEntityId = -1;
    }
    public boolean hasActiveHook() {
        //System.out.println(hookCasted);
        return hookEntityId!=-1;
    }


    @Override
    public void serialize(ValueOutput output) {
        output.putInt("HookEntityId", hookEntityId);
    }

    @Override
    public void deserialize(ValueInput input) {
        hookEntityId = input.getIntOr("HookEntityId",-1);
    }
}
