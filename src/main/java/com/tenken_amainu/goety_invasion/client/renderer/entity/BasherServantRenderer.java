package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.layer.HierarchicalArmorLayer;
import com.Polarice3.Goety.client.render.model.IllagerServantModel;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.BasherServant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BasherServantRenderer extends MobRenderer<BasherServant, IllagerServantModel<BasherServant>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(GINMod.MODID, "textures/entity/servant/illager/basher_servant.png");

    public BasherServantRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerServantModel<BasherServant>(context.bakeLayer(ModModelLayer.ILLAGER_SERVANT)) {
            @Override
            public void setupAnim(BasherServant entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

                if (entity.isStunned()) {
                    this.head.xRot += 0.4F;
                    this.head.yRot += (float) Math.sin(ageInTicks * 0.5) * 0.3F;
                }
            }
        }, 0.5F);

        this.addLayer(new HierarchicalArmorLayer<>(this, context));

        this.addLayer(new ItemInHandLayer<BasherServant, IllagerServantModel<BasherServant>>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BasherServant entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.isAggressive()) {
                    super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
    }

    @Override
    protected void scale(BasherServant entity, PoseStack poseStack, float partialTickTime) {
        float scale = 0.9375F;
        poseStack.scale(scale, scale, scale);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(BasherServant entity) {
        return TEXTURE;
    }
}