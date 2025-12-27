package com.minecraftquietus.quietus.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class GrapplingHookAttachment implements INBTSerializable<CompoundTag> {
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
        return hookEntityId != -1;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("HookEntityId", hookEntityId);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        hookEntityId = compoundTag.getIntOr("HookEntityId",-1);
    }
}
