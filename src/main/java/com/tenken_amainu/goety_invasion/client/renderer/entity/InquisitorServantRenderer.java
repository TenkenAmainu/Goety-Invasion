package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.layer.HierarchicalArmorLayer;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.client.model.ArmoredIllagerServantModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.InquisitorServant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class InquisitorServantRenderer extends MobRenderer<InquisitorServant, ArmoredIllagerServantModel<InquisitorServant>> {
    private static final ResourceLocation TEXTURE = GINMod.location("textures/entity/servant/illager/inquisitor_servant.png");

    public InquisitorServantRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmoredIllagerServantModel<>(context.bakeLayer(GIN_CMod.ARMORED_ILLAGER_SERVANT)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HierarchicalArmorLayer<>(this, context));
    }

    @Override
    protected void scale(InquisitorServant entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(InquisitorServant entity) {
        return TEXTURE;
    }
}