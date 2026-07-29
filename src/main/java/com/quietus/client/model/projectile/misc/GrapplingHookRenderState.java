package com.quietus.client.model.projectile.misc;

import com.quietus.client.model.projectile.magic.ProjectileRenderState;
import net.minecraft.world.phys.Vec3;

public class GrapplingHookRenderState extends ProjectileRenderState {
    public Vec3 lineStartOffset;
    public Vec3 lineOriginOffset;

    public GrapplingHookRenderState() {
        this.lineStartOffset = Vec3.ZERO;
        this.lineOriginOffset = Vec3.ZERO;
    }

}
