package com.minecraftquietus.quietus.misc;

import com.google.common.base.Objects;
import net.minecraft.core.BlockPos;
import javax.annotation.concurrent.Immutable;
@Immutable
public class RBlockProperties {
    private final int color;
    private final BlockPos pos;

    public RBlockProperties(BlockPos pos, int color) {
        this.color = color;
        this.pos = pos;
    }

    public RBlockProperties(int x, int y, int z, int color) {
        this( new BlockPos(x, y, z), color );
    }



    public int getColor() {
        return color;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RBlockProperties that = (RBlockProperties) o;
        return Objects.equal(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }
}
