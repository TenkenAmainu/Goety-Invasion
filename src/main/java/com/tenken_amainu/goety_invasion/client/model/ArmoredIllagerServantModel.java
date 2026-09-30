package com.tenken_amainu.goety_invasion.client.model;

import com.Polarice3.Goety.client.render.layer.HierarchicalArmor;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.InquisitorServant;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpyglassItem;

public class ArmoredIllagerServantModel<T extends AbstractIllagerServant> extends HierarchicalModel<T> implements ArmedModel, HeadedModel, HierarchicalArmor {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart collar;
    private final ModelPart body;
    private final ModelPart clothes;
    private final ModelPart arms;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart leftShoulderpad;
    private final ModelPart rightShoulderpad;

    public ArmoredIllagerServantModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = this.head.getChild("hat");
        this.collar = this.head.getChild("collar");
        this.body = root.getChild("body");
        this.clothes = this.body.getChild("clothes");
        this.arms = root.getChild("arms");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.leftShoulderpad = root.getChild("left_shoulderpad");
        this.rightShoulderpad = root.getChild("right_shoulderpad");

        this.hat.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);
        head.addOrReplaceChild("collar",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothes",
                CubeListBuilder.create().texOffs(0, 38).addBox(-4.0F, -24.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition arms = root.addOrReplaceChild("arms",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
        arms.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 46).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_shoulderpad",
                CubeListBuilder.create().texOffs(0, 64).mirror().addBox(-0.7F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.1F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_shoulderpad",
                CubeListBuilder.create().texOffs(0, 64).addBox(-3.3F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(1.1F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        if (entity.cantDo > 0) {
            head.zRot = 0.3F * Mth.sin(0.45F * ageInTicks);
            head.xRot = 0.4F;
        }

        if (riding) {
            rightArm.xRot = -1.2566371F;
            rightArm.yRot = 0.0F;
            rightArm.zRot = 0.0F;
            leftArm.xRot = -1.2566371F;
            leftArm.yRot = 0.0F;
            leftArm.zRot = 0.0F;
            rightLeg.xRot = -1.4137167F;
            rightLeg.yRot = 0.31415927F;
            rightLeg.zRot = 0.07853982F;
            leftLeg.xRot = -1.4137167F;
            leftLeg.yRot = -0.31415927F;
            leftLeg.zRot = -0.07853982F;
        } else {
            rightArm.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F;
            rightArm.yRot = 0.0F;
            rightArm.zRot = 0.0F;
            leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            leftArm.yRot = 0.0F;
            leftArm.zRot = 0.0F;
            rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
            rightLeg.yRot = 0.0F;
            rightLeg.zRot = 0.0F;
            leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount * 0.5F;
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = 0.0F;
        }
        rightShoulderpad.xRot = rightArm.xRot;
        rightShoulderpad.yRot = rightArm.yRot;
        rightShoulderpad.zRot = rightArm.zRot;
        leftShoulderpad.xRot = leftArm.xRot;
        leftShoulderpad.yRot = leftArm.yRot;
        leftShoulderpad.zRot = leftArm.zRot;

        AbstractIllagerServant.IllagerServantArmPose armPose = entity.getArmPose();

        if (armPose == AbstractIllagerServant.IllagerServantArmPose.ATTACKING) {
            if (entity.getMainHandItem().isEmpty()) {
                AnimationUtils.animateZombieArms(leftArm, rightArm, true, attackTime, ageInTicks);
                AnimationUtils.animateZombieArms(leftShoulderpad, rightShoulderpad, true, attackTime, ageInTicks);
            } else {
                AnimationUtils.swingWeaponDown(rightArm, leftArm, entity, attackTime, ageInTicks);
                AnimationUtils.swingWeaponDown(rightShoulderpad, leftShoulderpad, entity, attackTime, ageInTicks);
            }

            HumanoidArm shieldArm = null;
            if (entity.getOffhandItem().is(Items.SHIELD)) {
                shieldArm = entity.getMainArm().getOpposite();
            } else if (entity.getMainHandItem().is(Items.SHIELD)) {
                shieldArm = entity.getMainArm();
            }
            if (shieldArm != null) {
                boolean left = shieldArm == HumanoidArm.LEFT;
                ModelPart arm = left ? leftArm : rightArm;
                ModelPart pad = left ? leftShoulderpad : rightShoulderpad;
                arm.xRot = -1.05F;
                arm.yRot = left ? 0.5235988F : -0.5235988F;
                arm.zRot = 0.0F;
                pad.xRot = arm.xRot;
                pad.yRot = arm.yRot;
                pad.zRot = arm.zRot;
            }
        } else if (armPose == AbstractIllagerServant.IllagerServantArmPose.SPELLCASTING) {
            rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
            rightArm.zRot = 2.3561945F;
            leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
            leftArm.zRot = -2.3561945F;
            rightShoulderpad.xRot = rightArm.xRot;
            rightShoulderpad.zRot = rightArm.zRot;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.zRot = leftArm.zRot;
        } else if (armPose == AbstractIllagerServant.IllagerServantArmPose.BOW_AND_ARROW) {
            rightArm.yRot = -0.1F + head.yRot;
            rightArm.xRot = -1.5707964F + head.xRot;
            leftArm.xRot = -0.9424779F + head.xRot;
            leftArm.yRot = head.yRot - 0.4F;
            leftArm.zRot = 1.5707964F;
            rightShoulderpad.yRot = rightArm.yRot;
            rightShoulderpad.xRot = rightArm.xRot;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.yRot = leftArm.yRot;
            leftShoulderpad.zRot = leftArm.zRot;
        } else if (armPose == AbstractIllagerServant.IllagerServantArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, true);
            rightShoulderpad.xRot = rightArm.xRot;
            rightShoulderpad.yRot = rightArm.yRot;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.yRot = leftArm.yRot;
        } else if (armPose == AbstractIllagerServant.IllagerServantArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(rightArm, leftArm, entity, true);
            rightShoulderpad.xRot = rightArm.xRot;
            rightShoulderpad.yRot = rightArm.yRot;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.yRot = leftArm.yRot;
        } else if (armPose == AbstractIllagerServant.IllagerServantArmPose.CELEBRATING) {
            rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
            rightArm.zRot = 2.670354F;
            leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
            leftArm.zRot = -2.3561945F;
            rightShoulderpad.xRot = rightArm.xRot;
            rightShoulderpad.zRot = rightArm.zRot;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.zRot = leftArm.zRot;
        } else if (armPose == AbstractIllagerServant.IllagerServantArmPose.CROSSED && arms.visible) {
            rightArm.xRot = -0.75F;
            leftArm.xRot = -0.75F;
            rightShoulderpad.xRot = -0.75F;
            leftShoulderpad.xRot = -0.75F;
        }

        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            useItemRight(InteractionHand.MAIN_HAND, entity);
            useItemLeft(InteractionHand.OFF_HAND, entity);
        } else {
            useItemLeft(InteractionHand.MAIN_HAND, entity);
            useItemRight(InteractionHand.OFF_HAND, entity);
        }

        if (entity instanceof InquisitorServant inquisitor) {
            if (inquisitor.getStunnedState()) {
                head.xRot = 18.5F * Mth.DEG_TO_RAD;
                head.yRot = Mth.cos(ageInTicks * 0.8F) * 0.3F;
                rightArm.xRot = 200.0F * Mth.DEG_TO_RAD;
                rightArm.yRot = 0.5235988F;
                rightShoulderpad.xRot = rightArm.xRot;
                rightShoulderpad.yRot = rightArm.yRot;
                leftArm.xRot = 200.0F * Mth.DEG_TO_RAD;
                leftArm.yRot = -0.5235988F;
                leftShoulderpad.xRot = leftArm.xRot;
                leftShoulderpad.yRot = leftArm.yRot;
            }

            if (inquisitor.getRoarTicks() > 0) {
                float progress = 1.0F - (inquisitor.getRoarTicks() / 40.0F);
                float armFactor = Mth.sin(progress * Mth.PI);
                head.xRot = -0.8F - armFactor * 0.3F;
                head.yRot = Mth.sin((entity.tickCount + ageInTicks) * 0.5F) * 0.2F;
                rightArm.xRot = -1.4F - armFactor * 0.6F;
                leftArm.xRot = -1.4F - armFactor * 0.6F;
                rightArm.zRot = 0.5F + armFactor * 0.5F;
                leftArm.zRot = -0.5F - armFactor * 0.5F;
                rightShoulderpad.xRot = rightArm.xRot;
                rightShoulderpad.zRot = rightArm.zRot;
                leftShoulderpad.xRot = leftArm.xRot;
                leftShoulderpad.zRot = leftArm.zRot;
            }
        }

        boolean crossed = armPose == AbstractIllagerServant.IllagerServantArmPose.CROSSED;
        arms.visible = crossed;
        leftArm.visible = !crossed;
        rightArm.visible = !crossed;
        leftShoulderpad.visible = !crossed;
        rightShoulderpad.visible = !crossed;

        boolean hasArmor = entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem
                || entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorItem;
        clothes.visible = !hasArmor;
    }

    private void useItemRight(InteractionHand hand, T entity) {
        if (entity.getUsedItemHand() != hand) return;
        ItemStack useItem = entity.getUseItem();
        if (useItem.getItem() instanceof SpyglassItem) {
            rightArm.xRot = Mth.clamp(head.xRot - 1.9198622F - (entity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            rightArm.yRot = head.yRot - 0.2617994F;
            rightShoulderpad.xRot = rightArm.xRot;
            rightShoulderpad.yRot = rightArm.yRot;
        } else if (useItem.getItem() instanceof InstrumentItem) {
            rightArm.xRot = Mth.clamp(head.xRot, -1.2F, 1.2F) - 1.4835298F;
            rightArm.yRot = head.yRot - Mth.PI / 6F;
            rightShoulderpad.xRot = rightArm.xRot;
            rightShoulderpad.yRot = rightArm.yRot;
        }
    }

    private void useItemLeft(InteractionHand hand, T entity) {
        if (entity.getUsedItemHand() != hand) return;
        ItemStack useItem = entity.getUseItem();
        if (useItem.getItem() instanceof SpyglassItem) {
            leftArm.xRot = Mth.clamp(head.xRot - 1.9198622F - (entity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            leftArm.yRot = head.yRot + 0.2617994F;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.yRot = leftArm.yRot;
        } else if (useItem.getItem() instanceof InstrumentItem) {
            leftArm.xRot = Mth.clamp(head.xRot, -1.2F, 1.2F) - 1.4835298F;
            leftArm.yRot = head.yRot + Mth.PI / 6F;
            leftShoulderpad.xRot = leftArm.xRot;
            leftShoulderpad.yRot = leftArm.yRot;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.young) {
            poseStack.pushPose();
            float f = 1.5F / 2.0F;
            poseStack.scale(f, f, f);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            this.headParts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha));
            poseStack.popPose();
            poseStack.pushPose();
            float f1 = 1.0F / 2.0F;
            poseStack.scale(f1, f1, f1);
            poseStack.translate(0.0F, 24.0F / 16.0F, 0.0F);
            this.bodyParts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha));
            poseStack.popPose();
        } else {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.arms, this.rightLeg, this.leftLeg, this.rightShoulderpad, this.leftShoulderpad);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.getArm(arm).translateAndRotate(poseStack);
    }

    private ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
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

    public ModelPart getHat() {
        return this.hat;
    }

    public ModelPart getCollar() {
        return this.collar;
    }
}