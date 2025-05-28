package com.minecraftquietus.quietus.client.renderer;

import com.minecraftquietus.quietus.entity.monster.Bowslinger;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;

public class BowslingerRenderer extends AbstractSkeletonRenderer<Bowslinger, SkeletonRenderState> {
    private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public BowslingerRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public ResourceLocation getTextureLocation(SkeletonRenderState renderState) {
        return SKELETON_LOCATION;
    }

    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(AbstractSkeleton entity, HumanoidArm arm) {
        /*return entity.getMainArm() == arm && entity.isAggressive() && entity.getMainHandItem().is(Items.BOW)
            ? HumanoidModel.ArmPose.BOW_AND_ARROW
            : HumanoidModel.ArmPose.EMPTY;*/
        return entity.isUsingItem() && entity.isAggressive() && entity.getItemHeldByArm(arm).getItem() instanceof BowItem
            ? HumanoidModel.ArmPose.BOW_AND_ARROW
            : HumanoidModel.ArmPose.EMPTY;
    }
}