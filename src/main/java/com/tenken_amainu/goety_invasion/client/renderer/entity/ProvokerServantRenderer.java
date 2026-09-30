package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.layer.HierarchicalArmorLayer;
import com.Polarice3.Goety.client.render.model.IllagerServantModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.ProvokerServant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class ProvokerServantRenderer extends MobRenderer<ProvokerServant, IllagerServantModel<ProvokerServant>> {

    private static final ResourceLocation TEXTURE = GINMod.location("textures/entity/servant/illager/provoker_servant.png");

    public ProvokerServantRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerServantModel<>(context.bakeLayer(ModModelLayer.ILLAGER_SERVANT)), 0.5F);

        this.addLayer(new HierarchicalArmorLayer<>(this, context));
        this.addLayer(new ItemInHandLayer<ProvokerServant, IllagerServantModel<ProvokerServant>>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                               ProvokerServant entity, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.getTarget() != null || entity.isAggressive()) {
                    super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
    }

    @Override
    protected void scale(ProvokerServant entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(ProvokerServant entity) {
        return TEXTURE;
    }
}