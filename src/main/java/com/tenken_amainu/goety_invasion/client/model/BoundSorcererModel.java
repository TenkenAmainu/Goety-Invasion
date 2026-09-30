package com.tenken_amainu.goety_invasion.client.model;

import com.Polarice3.Goety.client.render.model.BoundIllagerModel;
import com.Polarice3.Goety.common.entities.ally.undead.bound.AbstractBoundIllager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BoundSorcererModel<T extends AbstractBoundIllager> extends BoundIllagerModel<T> {

    public BoundSorcererModel(ModelPart root) {
        super(root);
        this.hat.visible = false;
        this.head.getChild("head_hat").visible = true;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("head_hat",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);

        head.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        head.addOrReplaceChild("lower_hat",
                CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 1.0F, 14.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        head.addOrReplaceChild("middle_hat",
                CubeListBuilder.create()
                        .texOffs(0, 80).addBox(-4.0F, -19.76F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.05F, 0.0F, 0.0F));

        head.addOrReplaceChild("upper_hat",
                CubeListBuilder.create()
                        .texOffs(0, 98).addBox(-4.0F, -19.76F, 3.9F, 8.0F, 5.0F, 5.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.05F, 0.0F, 0.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.05F)),
                PartPose.ZERO);

        root.addOrReplaceChild("clothes",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.05F)),
                PartPose.ZERO);

        root.addOrReplaceChild("arms",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, true)
                        .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }
}