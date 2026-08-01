package com.quietus.client.renderer;

import com.quietus.entity.monster.Bowslinger;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;

public class BowslingerRenderer extends ThemedSkeletonRenderer<Bowslinger> {
    public BowslingerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(Bowslinger entity, HumanoidArm arm) {
        return entity.isUsingItem() && entity.isAggressive() && entity.getItemHeldByArm(arm).getItem() instanceof BowItem
                ? HumanoidModel.ArmPose.BOW_AND_ARROW
                : HumanoidModel.ArmPose.EMPTY;
    }
}
