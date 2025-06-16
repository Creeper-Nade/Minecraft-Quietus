package com.minecraftquietus.quietus.client.model.equipments;


import com.minecraftquietus.quietus.client.model.QuietusEmissiveLayer;
import com.minecraftquietus.quietus.item.equipment.AmethystArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.animatable.processing.AnimationProcessor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.RenderUtil;

public class AmethystArmorRenderer<R extends HumanoidRenderState & GeoRenderState> extends GeoArmorRenderer<AmethystArmorItem,R> {

    protected GeoBone waistBone = null;
    public AmethystArmorRenderer() {
        super(new AmethystArmorModel());
        //I was trying to add an emissive layer however it's bugged, ignore it for now
        //this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }




// Overriding stuffs from GeoArmorRenderer to add the bone of waist
    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        if (this.lastModel == bakedModel)
            return;

        AnimationProcessor<AmethystArmorItem> animationProcessor = this.model.getAnimationProcessor();
        this.lastModel = bakedModel;
        this.headBone = animationProcessor.getBone("armorHead");
        this.bodyBone = animationProcessor.getBone("armorBody");
        this.waistBone = animationProcessor.getBone("armorWaist");
        this.rightArmBone = animationProcessor.getBone("armorRightArm");
        this.leftArmBone = animationProcessor.getBone("armorLeftArm");
        this.rightLegBone = animationProcessor.getBone("armorRightLeg");
        this.leftLegBone = animationProcessor.getBone("armorLeftLeg");
        this.rightBootBone = animationProcessor.getBone("armorRightBoot");
        this.leftBootBone = animationProcessor.getBone("armorLeftBoot");
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        setAllBonesVisible(false);

        switch (currentSlot) {
            case HEAD -> setBonesVisible(this.head.visible, this.headBone);
            case CHEST -> setBonesVisible(this.body.visible, this.bodyBone, this.rightArmBone, this.leftArmBone);
            case LEGS -> setBonesVisible(this.rightLeg.visible, this.rightLegBone, this.leftLegBone,this.waistBone);
            case FEET -> setBonesVisible(this.rightLeg.visible, this.rightBootBone, this.leftBootBone);
            default -> {}
        }
    }

    @Override
    protected void setAllBonesVisible(boolean visible) {
        setBonesVisible(visible, this.headBone, this.bodyBone, this.rightArmBone, this.leftArmBone,
                this.rightLegBone, this.leftLegBone, this.rightBootBone, this.leftBootBone,this.waistBone);
    }
    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model) {
        setAllVisible(false);

        currentPart.visible = true;
        GeoBone bone = null;

        if (currentPart == model.hat || currentPart == model.head) {
            bone = this.headBone;
        }
        else if (currentPart == model.body) {
            bone = this.bodyBone;
        }
        else if (currentPart == model.leftArm) {
            bone = this.leftArmBone;
        }
        else if (currentPart == model.rightArm) {
            bone = this.rightArmBone;
        }
        else if (currentPart == model.leftLeg) {
            bone = currentSlot == EquipmentSlot.FEET ? this.leftBootBone : this.leftLegBone;
            this.waistBone.setHidden(false);
        }
        else if (currentPart == model.rightLeg) {
            bone = currentSlot == EquipmentSlot.FEET ? this.rightBootBone : this.rightLegBone;
        }

        if (bone != null)
            bone.setHidden(false);
    }
    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        if (this.headBone != null) {
            ModelPart headPart = baseModel.head;

            RenderUtil.matchModelPartRot(headPart, this.headBone);
            this.headBone.updatePosition(headPart.x, -headPart.y, headPart.z);
            this.headBone.updateScale(headPart.xScale, headPart.yScale, headPart.zScale);
        }

        if (this.bodyBone != null) {
            ModelPart bodyPart = baseModel.body;

            RenderUtil.matchModelPartRot(bodyPart, this.bodyBone);
            RenderUtil.matchModelPartRot(bodyPart, this.waistBone);
            this.bodyBone.updatePosition(bodyPart.x, -bodyPart.y, bodyPart.z);
            this.bodyBone.updateScale(bodyPart.xScale, bodyPart.yScale, bodyPart.zScale);
            this.waistBone.updatePosition(bodyPart.x, -bodyPart.y, bodyPart.z);
            this.waistBone.updateScale(bodyPart.xScale, bodyPart.yScale, bodyPart.zScale);
        }

        if (this.rightArmBone != null) {
            ModelPart rightArmPart = baseModel.rightArm;

            RenderUtil.matchModelPartRot(rightArmPart, this.rightArmBone);
            this.rightArmBone.updatePosition(rightArmPart.x + 5, 2 - rightArmPart.y, rightArmPart.z);
            this.rightArmBone.updateScale(rightArmPart.xScale, rightArmPart.yScale, rightArmPart.zScale);
        }

        if (this.leftArmBone != null) {
            ModelPart leftArmPart = baseModel.leftArm;

            RenderUtil.matchModelPartRot(leftArmPart, this.leftArmBone);
            this.leftArmBone.updatePosition(leftArmPart.x - 5f, 2f - leftArmPart.y, leftArmPart.z);
            this.leftArmBone.updateScale(leftArmPart.xScale, leftArmPart.yScale, leftArmPart.zScale);
        }

        if (this.rightLegBone != null) {
            ModelPart rightLegPart = baseModel.rightLeg;

            RenderUtil.matchModelPartRot(rightLegPart, this.rightLegBone);
            this.rightLegBone.updatePosition(rightLegPart.x + 2, 12 - rightLegPart.y, rightLegPart.z);
            this.rightLegBone.updateScale(rightLegPart.xScale, rightLegPart.yScale, rightLegPart.zScale);

            if (this.rightBootBone != null) {
                RenderUtil.matchModelPartRot(rightLegPart, this.rightBootBone);
                this.rightBootBone.updatePosition(rightLegPart.x + 2, 12 - rightLegPart.y, rightLegPart.z);
                this.rightBootBone.updateScale(rightLegPart.xScale, rightLegPart.yScale, rightLegPart.zScale);
            }
        }

        if (this.leftLegBone != null) {
            ModelPart leftLegPart = baseModel.leftLeg;

            RenderUtil.matchModelPartRot(leftLegPart, this.leftLegBone);
            this.leftLegBone.updatePosition(leftLegPart.x - 2, 12 - leftLegPart.y, leftLegPart.z);
            this.leftLegBone.updateScale(leftLegPart.xScale, leftLegPart.yScale, leftLegPart.zScale);

            if (this.leftBootBone != null) {
                RenderUtil.matchModelPartRot(leftLegPart, this.leftBootBone);
                this.leftBootBone.updatePosition(leftLegPart.x - 2, 12 - leftLegPart.y, leftLegPart.z);
                this.leftBootBone.updateScale(leftLegPart.xScale, leftLegPart.yScale, leftLegPart.zScale);
            }
        }
    }
}
