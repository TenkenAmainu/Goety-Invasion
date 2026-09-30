package com.tenken_amainu.goety_invasion.client.model;

import com.Polarice3.Goety.client.render.layer.HierarchicalArmor;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
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
import net.minecraft.world.item.SpyglassItem;

public class HatIllagerServantModel<T extends AbstractIllagerServant> extends HierarchicalModel<T>
        implements ArmedModel, HeadedModel, HierarchicalArmor {

    public final ModelPart root;
    public final ModelPart body;
    public final ModelPart clothes;
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart collar;
    public final ModelPart upper_hat;
    public final ModelPart middle_hat;
    public final ModelPart lower_hat;
    public final ModelPart arms;
    public final ModelPart RightArm;
    public final ModelPart LeftArm;
    public final ModelPart RightLeg;
    public final ModelPart LeftLeg;

    public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;

    public HatIllagerServantModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hat.visible = false;
        this.collar = this.head.getChild("collar");
        this.upper_hat = this.head.getChild("upper_hat");
        this.middle_hat = this.head.getChild("middle_hat");
        this.lower_hat = this.head.getChild("lower_hat");
        this.body = root.getChild("body");
        this.clothes = this.body.getChild("clothes");
        this.arms = root.getChild("arms");
        this.RightArm = root.getChild("right_arm");
        this.LeftArm = root.getChild("left_arm");
        this.RightLeg = root.getChild("right_leg");
        this.LeftLeg = root.getChild("left_leg");
    }

    public static MeshDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);

        head.addOrReplaceChild("collar",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.45F)),
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

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothes",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-4.0F, -24.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition arms = partdefinition.addOrReplaceChild("arms",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
        arms.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create()
                        .texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        return meshdefinition;
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(createMesh(), 64, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.cantDo > 0) {
            this.head.zRot = 0.3F * Mth.sin(0.45F * ageInTicks);
            this.head.xRot = 0.4F;
        } else {
            this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            this.head.xRot = headPitch * Mth.DEG_TO_RAD;
        }

        if (this.riding) {
            this.RightArm.xRot = (-(float) Math.PI / 5F);
            this.RightArm.yRot = 0.0F;
            this.RightArm.zRot = 0.0F;
            this.LeftArm.xRot = (-(float) Math.PI / 5F);
            this.LeftArm.yRot = 0.0F;
            this.LeftArm.zRot = 0.0F;
            this.RightLeg.xRot = -1.4137167F;
            this.RightLeg.yRot = ((float) Math.PI / 10F);
            this.RightLeg.zRot = 0.07853982F;
            this.LeftLeg.xRot = -1.4137167F;
            this.LeftLeg.yRot = (-(float) Math.PI / 10F);
            this.LeftLeg.zRot = -0.07853982F;
        } else {
            this.RightArm.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F;
            this.RightArm.yRot = 0.0F;
            this.RightArm.zRot = 0.0F;
            this.LeftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            this.LeftArm.yRot = 0.0F;
            this.LeftArm.zRot = 0.0F;
            this.RightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
            this.RightLeg.yRot = 0.0F;
            this.RightLeg.zRot = 0.0F;
            this.LeftLeg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount * 0.5F;
            this.LeftLeg.yRot = 0.0F;
            this.LeftLeg.zRot = 0.0F;
        }

        AbstractIllagerServant.IllagerServantArmPose armPose = entity.getArmPose();
        switch (armPose) {
            case ATTACKING -> {
                if (entity.getMainHandItem().isEmpty()) {
                    AnimationUtils.animateZombieArms(this.LeftArm, this.RightArm, true, this.attackTime, ageInTicks);
                } else {
                    AnimationUtils.swingWeaponDown(this.RightArm, this.LeftArm, entity, this.attackTime, ageInTicks);
                }
            }
            case SPELLCASTING -> {
                this.RightArm.z = 0.0F;
                this.RightArm.x = -5.0F;
                this.LeftArm.z = 0.0F;
                this.LeftArm.x = 5.0F;
                this.RightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.LeftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.RightArm.zRot = 2.3561945F;
                this.LeftArm.zRot = -2.3561945F;
                this.RightArm.yRot = 0.0F;
                this.LeftArm.yRot = 0.0F;
            }
            case BOW_AND_ARROW -> {
                this.RightArm.yRot = -0.1F + this.head.yRot;
                this.RightArm.xRot = -1.5707964F + this.head.xRot;
                this.LeftArm.xRot = -0.9424779F + this.head.xRot;
                this.LeftArm.yRot = this.head.yRot - 0.4F;
                this.LeftArm.zRot = 1.5707964F;
            }
            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.RightArm, this.LeftArm, this.head, true);
            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.RightArm, this.LeftArm, entity, true);
            case CELEBRATING -> {
                this.RightArm.z = 0.0F;
                this.RightArm.x = -5.0F;
                this.RightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
                this.RightArm.zRot = 2.3561945F;
                this.RightArm.yRot = 0.0F;
                this.LeftArm.z = 0.0F;
                this.LeftArm.x = 5.0F;
                this.LeftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
                this.LeftArm.zRot = -2.3561945F;
                this.LeftArm.yRot = 0.0F;
            }
            case CROSSED -> {
                if (this.arms.visible) {
                    this.RightArm.xRot = -0.75F;
                    this.RightArm.zRot = 0.0F;
                    this.RightArm.yRot = 0.0F;
                    this.LeftArm.xRot = -0.75F;
                    this.LeftArm.zRot = 0.0F;
                    this.LeftArm.yRot = 0.0F;
                }
            }
        }

        boolean crossed = armPose == AbstractIllagerServant.IllagerServantArmPose.CROSSED;
        this.arms.visible = crossed;
        this.LeftArm.visible = !crossed;
        this.RightArm.visible = !crossed;

        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            this.useItemRight(InteractionHand.MAIN_HAND, entity);
            this.useItemLeft(InteractionHand.OFF_HAND, entity);
        } else {
            this.useItemLeft(InteractionHand.MAIN_HAND, entity);
            this.useItemRight(InteractionHand.OFF_HAND, entity);
        }

        boolean wearingArmor = entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem
                || entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorItem;
        this.clothes.visible = !wearingArmor;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        if (this.young) {
            poseStack.pushPose();
            float headScale = 1.5F / 2.0F;
            poseStack.scale(headScale, headScale, headScale);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            this.headParts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha));
            poseStack.popPose();

            poseStack.pushPose();
            float bodyScale = 1.0F / 2.0F;
            poseStack.scale(bodyScale, bodyScale, bodyScale);
            poseStack.translate(0.0F, 24.0F / 16.0F, 0.0F);
            this.bodyParts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha));
            poseStack.popPose();
        } else {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    public void copyPropertiesTo(HatIllagerServantModel<T> target) {
        super.copyPropertiesTo(target);
        target.leftArmPose = this.leftArmPose;
        target.rightArmPose = this.rightArmPose;
    }

    private void useItemRight(InteractionHand hand, T entity) {
        if (entity.getUsedItemHand() != hand) return;
        ItemStack useItem = entity.getUseItem();
        if (useItem.getItem() instanceof SpyglassItem) {
            this.RightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (entity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.RightArm.yRot = this.head.yRot - 0.2617994F;
        } else if (useItem.getItem() instanceof InstrumentItem) {
            this.RightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
            this.RightArm.yRot = this.head.yRot - ((float) Math.PI / 6F);
        }
    }

    private void useItemLeft(InteractionHand hand, T entity) {
        if (entity.getUsedItemHand() != hand) return;
        ItemStack useItem = entity.getUseItem();
        if (useItem.getItem() instanceof SpyglassItem) {
            this.LeftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (entity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.LeftArm.yRot = this.head.yRot + 0.2617994F;
        } else if (useItem.getItem() instanceof InstrumentItem) {
            this.LeftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
            this.LeftArm.yRot = this.head.yRot + ((float) Math.PI / 6F);
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    private ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.LeftArm : this.RightArm;
    }

    public ModelPart getHat() {
        return this.hat;
    }

    public ModelPart getCollar() {
        return this.collar;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.RightArm, this.LeftArm, this.arms, this.RightLeg, this.LeftLeg);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.getArm(arm).translateAndRotate(poseStack);
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
        return ImmutableList.of(this.RightArm);
    }

    @Override
    public Iterable<ModelPart> leftHandArmors() {
        return ImmutableList.of(this.LeftArm);
    }

    @Override
    public Iterable<ModelPart> rightLegPartArmors() {
        return ImmutableList.of(this.RightLeg);
    }

    @Override
    public Iterable<ModelPart> leftLegPartArmors() {
        return ImmutableList.of(this.LeftLeg);
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