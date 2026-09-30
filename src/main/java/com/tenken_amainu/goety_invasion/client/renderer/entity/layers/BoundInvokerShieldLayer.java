package com.tenken_amainu.goety_invasion.client.renderer.entity.layers;

import com.Polarice3.Goety.client.render.model.BoundIllagerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.client.model.BoundInvokerModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundInvoker;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BoundInvokerShieldLayer extends EnergySwirlLayer<BoundInvoker, BoundIllagerModel<BoundInvoker>> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final BoundInvokerModel model;

    public BoundInvokerShieldLayer(RenderLayerParent<BoundInvoker, BoundIllagerModel<BoundInvoker>> context, EntityModelSet loader) {
        super(context);
        this.model = new BoundInvokerModel(loader.bakeLayer(GIN_CMod.BOUND_INVOKER_LAYER));
    }

    @Override
    protected float xOffset(float partialAge) {
        return Mth.cos(partialAge * 0.02f) * 0.02F;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return TEXTURE_LOCATION;
    }

    @Override
    protected EntityModel<BoundInvoker> model() {
        return this.model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BoundInvoker livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (livingEntity.isShielded()) {
            super.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount,
                    partialTick, ageInTicks, netHeadYaw, headPitch);
        }
    }
}