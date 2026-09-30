package com.tenken_amainu.goety_invasion.client.model;

import com.tenken_amainu.goety_invasion.common.entities.ally.undead.AllySurrendered;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jetbrains.annotations.NotNull;

public class AllySurrenderedModel<T extends AllySurrendered & RangedAttackMob> extends SkeletonModel<T> {

    public AllySurrenderedModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isCharging()) {
            rightArm.xRot = 3.7699115f;
            leftArm.xRot = 3.7699115f;
        }
    }
}