package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.layer.HierarchicalArmorLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.client.model.HatIllagerServantModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.SorcererServant;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SorcererServantRenderer extends MobRenderer<SorcererServant, HatIllagerServantModel<SorcererServant>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("goety_invasion", "textures/entity/servant/illager/sorcerer_servant.png");

    public SorcererServantRenderer(EntityRendererProvider.Context context) {
        super(context, new HatIllagerServantModel<>(context.bakeLayer(GIN_CMod.SORCERER_SERVANT_LAYER)), 0.5F);
        this.model.getHat().visible = true;
        this.addLayer(new HierarchicalArmorLayer<>(this, context));
    }

    @Override
    protected void scale(SorcererServant entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.95F, 0.95F, 0.95F);
    }

    @Override
    public ResourceLocation getTextureLocation(SorcererServant entity) {
        return TEXTURE;
    }
}