package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.layer.HierarchicalArmorLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.client.model.FireCallerServantModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.FireCallerServant;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class FireCallerServantRenderer extends MobRenderer<FireCallerServant, FireCallerServantModel<FireCallerServant>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "goety_invasion", "textures/entity/servant/illager/firecaller_servant.png");

    public FireCallerServantRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
                new FireCallerServantModel<>(ctx.bakeLayer(GIN_CMod.FIRE_CALLER_SERVANT)),
                0.5F);
        this.model.getHat().visible = true;
        this.addLayer(new CustomHeadLayer<>(this, ctx.getModelSet(), ctx.getItemInHandRenderer()));
        this.addLayer(new HierarchicalArmorLayer<>(this, ctx));
        this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    protected void scale(FireCallerServant entity, PoseStack poseStack, float partial) {
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(FireCallerServant entity) {
        return TEXTURE;
    }
}