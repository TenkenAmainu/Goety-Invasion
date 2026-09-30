package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.layer.HierarchicalArmorLayer;
import com.Polarice3.Goety.client.render.model.IllagerServantModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.client.renderer.entity.layers.NecromancerServantShieldLayer;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.NecromancerServant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class NecromancerServantRenderer extends MobRenderer<NecromancerServant, IllagerServantModel<NecromancerServant>> {
    private static final ResourceLocation TEXTURE = GINMod.location("textures/entity/servant/illager/necromancer_servant.png");

    public NecromancerServantRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerServantModel<>(context.bakeLayer(ModModelLayer.ILLAGER_SERVANT)), 0.5F);

        this.addLayer(new NecromancerServantShieldLayer(this, context.getModelSet()));
        this.addLayer(new HierarchicalArmorLayer<>(this, context));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight,
                               NecromancerServant entity, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.isCastingSpell()) {
                    super.render(matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
        this.model.getHat().visible = false;
    }

    @Override
    public ResourceLocation getTextureLocation(NecromancerServant entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(NecromancerServant entity, PoseStack matrixStack, float partialTick) {
        float scale = 1.0F + entity.getNecroLevel() * 0.15F;
        matrixStack.scale(scale, scale, scale);
    }
}