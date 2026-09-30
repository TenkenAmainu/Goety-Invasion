package com.tenken_amainu.goety_invasion.client.model;

import com.Polarice3.Goety.client.render.model.BoundIllagerModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundInvoker;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BoundInvokerModel extends BoundIllagerModel<BoundInvoker> {

    public BoundInvokerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild(PartNames.HAT, CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = partDefinition.addOrReplaceChild(PartNames.HEAD,
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild(PartNames.HAT,
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);

        head.addOrReplaceChild(PartNames.NOSE,
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        partDefinition.addOrReplaceChild(PartNames.BODY,
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 23.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partDefinition.addOrReplaceChild("clothes",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 23.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition arms = partDefinition.addOrReplaceChild(PartNames.ARMS,
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));

        arms.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create().texOffs(44, 22).mirror()
                        .addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.ZERO);

        partDefinition.addOrReplaceChild(PartNames.RIGHT_ARM,
                CubeListBuilder.create().texOffs(40, 46)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        partDefinition.addOrReplaceChild(PartNames.LEFT_ARM,
                CubeListBuilder.create().texOffs(40, 46).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        partDefinition.addOrReplaceChild(PartNames.RIGHT_LEG,
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        partDefinition.addOrReplaceChild(PartNames.LEFT_LEG,
                CubeListBuilder.create().texOffs(0, 22).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}