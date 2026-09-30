package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.BoundIllagerRenderer;
import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.model.VillagerArmorModel;
import com.Polarice3.Goety.common.entities.ally.undead.bound.AbstractBoundIllager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.client.model.BoundFireCallerModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundFireCaller;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BoundFireCallerRenderer extends BoundIllagerRenderer<BoundFireCaller> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "goety_invasion", "textures/entity/servant/bound/bound_firecaller.png");
    private static final ResourceLocation CASTING = new ResourceLocation(
            "goety_invasion", "textures/entity/servant/bound/bound_firecaller_casting.png");

    public BoundFireCallerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
                new BoundFireCallerModel<>(ctx.bakeLayer(GIN_CMod.BOUND_FIRE_CALLER)),
                0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new VillagerArmorModel<>(ctx.bakeLayer(ModModelLayer.VILLAGER_ARMOR_INNER)),
                new VillagerArmorModel<>(ctx.bakeLayer(ModModelLayer.VILLAGER_ARMOR_OUTER)),
                ctx.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack stack, MultiBufferSource buffer, int light,
                               BoundFireCaller entity,
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
    protected void scale(BoundFireCaller entity, PoseStack poseStack, float partial) {
        float f = 0.9375F;
        poseStack.scale(f, f, f);
    }

    @Override
    public ResourceLocation getTextureLocation(BoundFireCaller entity) {
        return entity.isCastingSpell() ? CASTING : TEXTURE;
    }
}