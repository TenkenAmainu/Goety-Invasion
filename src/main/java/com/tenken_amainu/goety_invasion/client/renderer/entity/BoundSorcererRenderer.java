package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.BoundIllagerRenderer;
import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.model.VillagerArmorModel;
import com.Polarice3.Goety.common.entities.ally.undead.bound.AbstractBoundIllager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.client.model.BoundSorcererModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundSorcerer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BoundSorcererRenderer extends BoundIllagerRenderer<BoundSorcerer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "goety_invasion", "textures/entity/servant/bound/bound_sorcerer.png");
    private static final ResourceLocation CASTING = new ResourceLocation(
            "goety_invasion", "textures/entity/servant/bound/bound_sorcerer_casting.png");

    public BoundSorcererRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
                new BoundSorcererModel<>(ctx.bakeLayer(GIN_CMod.BOUND_SORCERER)),
                0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new VillagerArmorModel<>(ctx.bakeLayer(ModModelLayer.VILLAGER_ARMOR_INNER)),
                new VillagerArmorModel<>(ctx.bakeLayer(ModModelLayer.VILLAGER_ARMOR_OUTER)),
                ctx.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack stack, MultiBufferSource buffer, int light,
                               BoundSorcerer entity,
                               float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                if (entity.getArmPose() != AbstractBoundIllager.BoundArmPose.CROSSED) {
                    super.render(stack, buffer, light, entity,
                            limbSwing, limbSwingAmount, partialTicks,
                            ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
    }

    @Override
    protected void scale(BoundSorcerer entity, PoseStack poseStack, float partial) {
        float f = 0.9375F;
        poseStack.scale(f, f, f);
    }

    @Override
    public ResourceLocation getTextureLocation(BoundSorcerer entity) {
        return entity.isCastingSpell() ? CASTING : TEXTURE;
    }
}