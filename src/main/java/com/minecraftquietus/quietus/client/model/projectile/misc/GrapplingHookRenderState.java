package com.minecraftquietus.quietus.client.model.projectile.misc;

import com.minecraftquietus.quietus.client.model.projectile.magic.ProjectileRenderState;
import net.minecraft.world.phys.Vec3;

public class GrapplingHookRenderState extends ProjectileRenderState {
    public Vec3 lineOriginOffset;

    public GrapplingHookRenderState() {
        this.lineOriginOffset = Vec3.ZERO;
    }

}
