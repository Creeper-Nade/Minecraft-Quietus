package com.quietus.mixin;

import net.minecraft.world.entity.monster.skeleton.Skeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Skeleton.class)
public interface SkeletonConversionAccessor {
    @Accessor("inPowderSnowTime")
    void quietus$setInPowderSnowTime(int time);
}
