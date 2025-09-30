package com.minecraftquietus.quietus.core.skilltree;

import java.util.Map;

import org.jetbrains.annotations.UnknownNullability;

import com.minecraftquietus.quietus.skilltree.SkillCategory;
import com.minecraftquietus.quietus.skilltree.SkillCategoryProgress;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class SkillTreeProgressComponent implements INBTSerializable<CompoundTag> {
    private Map<SkillCategory, SkillCategoryProgress> progress; 

    @Override
    public void deserializeNBT(Provider provider, CompoundTag tag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deserializeNBT'");
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(Provider provider) {
        
    }

}
