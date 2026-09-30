package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.Polarice3.Goety.client.render.BoundIllagerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.client.model.BoundInvokerModel;
import com.tenken_amainu.goety_invasion.client.renderer.entity.layers.BoundInvokerGoldLayer;
import com.tenken_amainu.goety_invasion.client.renderer.entity.layers.BoundInvokerShieldLayer;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundInvoker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BoundInvokerRenderer extends BoundIllagerRenderer<BoundInvoker> {
    private static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation(GINMod.MODID, "textures/entity/servant/bound/bound_invoker.png");
    private static final ResourceLocation TEXTURE_CASTING = new ResourceLocation(GINMod.MODID, "textures/entity/servant/bound/bound_invoker_casting.png");

    public BoundInvokerRenderer(EntityRendererProvider.Context context) {
        super(context, new BoundInvokerModel(context.bakeLayer(GIN_CMod.BOUND_INVOKER_LAYER)), 0.5F);
        this.addLayer(new BoundInvokerGoldLayer<>(this));
        this.addLayer(new BoundInvokerShieldLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(BoundInvoker entity) {
        return entity.isCastingSpell() ? TEXTURE_CASTING : TEXTURE_NORMAL;
    }

    @Override
    protected void scale(BoundInvoker entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.95F, 0.95F, 0.95F);
    }

    @Override
    public void render(BoundInvoker entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float q = Mth.sin(entity.tickCount * 0.076f) * 1.5f * Mth.DEG_TO_RAD;
        poseStack.translate(0.0f, 0.3 + q, 0.0f);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}