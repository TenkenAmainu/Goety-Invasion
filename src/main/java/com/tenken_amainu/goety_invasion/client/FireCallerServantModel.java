package com.tenken_amainu.goety_invasion.client.model;

import com.Polarice3.Goety.client.render.layer.HierarchicalArmor;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class FireCallerServantModel<T extends AbstractIllagerServant> extends HierarchicalModel<T>
        implements ArmedModel, HeadedModel, HierarchicalArmor {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public FireCallerServantModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = this.head.getChild("hat");
        this.body = root.getChild("body");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();

        PartDefinition head = modelPartData.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.3F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -10.0F, -4.3F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        modelPartData.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        modelPartData.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2.0F, 0.0F, -1.35F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        modelPartData.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -1.35F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        modelPartData.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArm = modelPartData.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        leftArm.addOrReplaceChild("staff",
                CubeListBuilder.create()
                        .texOffs(112, 0).addBox(0.0F, -4.9F, -9.5F, 1.0F, 28.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.2F, 0.0F, 0.0F));

        return LayerDefinition.create(modelData, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.body.xRot = 0.2F;
        this.head.yRot = netHeadYaw * 0.017453292F;
        this.head.xRot = headPitch * 0.017453292F;

        if (this.riding) {
            this.rightArm.xRot = -0.62831855F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = -0.62831855F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = 0.31415927F;
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = -0.31415927F;
            this.leftLeg.zRot = -0.07853982F;
        } else {
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount * 0.5F - 1.2F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount * 0.5F;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
        }

        switch (entity.getArmPose()) {
            case ATTACKING -> {
                if (entity.getMainHandItem().isEmpty()) {
                    AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
                } else {
                    AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, ageInTicks);
                }
            }
            case SPELLCASTING -> {
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.rightArm.zRot = 2.3561945F;
                this.leftArm.zRot = -2.3561945F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
            }
            case BOW_AND_ARROW -> {
                this.rightArm.yRot = -0.1F + this.head.yRot;
                this.rightArm.xRot = -1.5707964F + this.head.xRot;
                this.leftArm.xRot = -0.9424779F + this.head.xRot;
                this.leftArm.yRot = this.head.yRot - 0.4F;
                this.leftArm.zRot = 1.5707964F;
            }
            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, true);
            case CELEBRATING -> {
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
                this.rightArm.zRot = 2.670354F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
                this.leftArm.zRot = -2.3561945F;
                this.leftArm.yRot = 0.0F;
            }
        }
    }

    private ModelPart getAttackingArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    public ModelPart getHat() {
        return this.hat;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack matrices) {
        this.getAttackingArm(arm).translateAndRotate(matrices);
    }

    @Override
    public void translateToHead(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
    }

    @Override
    public void translateToChest(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(1.05F, 1.05F, 1.05F);
    }

    @Override
    public void translateToLeg(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
    }

    @Override
    public void translateToArms(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(1.05F, 1.05F, 1.05F);
    }

    @Override
    public Iterable<ModelPart> rightHandArmors() {
        return ImmutableList.of(this.rightArm);
    }

    @Override
    public Iterable<ModelPart> leftHandArmors() {
        return ImmutableList.of(this.leftArm);
    }

    @Override
    public Iterable<ModelPart> rightLegPartArmors() {
        return ImmutableList.of(this.rightLeg);
    }

    @Override
    public Iterable<ModelPart> leftLegPartArmors() {
        return ImmutableList.of(this.leftLeg);
    }

    @Override
    public Iterable<ModelPart> bodyPartArmors() {
        return ImmutableList.of(this.body);
    }

    @Override
    public Iterable<ModelPart> headPartArmors() {
        return ImmutableList.of(this.head);
    }
}