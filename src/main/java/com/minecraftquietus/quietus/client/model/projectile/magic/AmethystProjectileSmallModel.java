package com.minecraftquietus.quietus.client.model.projectile.magic;
import com.minecraftquietus.quietus.Quietus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

import static com.minecraftquietus.quietus.Quietus.MODID;

public class AmethystProjectileSmallModel<T extends EntityRenderState> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MODID, "small_amethyst_projectile"), "main");
	private final ModelPart bb_main;

	public AmethystProjectileSmallModel(ModelPart root) {
		super(root);
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 0).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1464F, -1.3536F, 0.5F, -1.5708F, 0.0F, -0.7854F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(6, 0).addBox(-0.5F, 0.0F, 0.0F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5607F, -2.0607F, 4.5F, -1.5708F, 0.0F, 0.7854F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}


	@Override
	public void setupAnim(T state) {
		super.setupAnim(state);
		// ...
	}

/*
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,int color) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}*/
}