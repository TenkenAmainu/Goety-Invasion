package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.projectiles.AllySkullBolt;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AllySkullBoltRender extends EntityRenderer<AllySkullBolt> {
    private static final ResourceLocation TEXTURE_LOCATION =
            new ResourceLocation(GINMod.MODID, "textures/entity/projectiles/ally_skullbolt.png");

    private final SkullModel model;

    public AllySkullBoltRender(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKULL));
    }

    @Override
    protected int getBlockLightLevel(AllySkullBolt allySkullBolt, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(AllySkullBolt entity, float entityYaw, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        float h = Mth.rotLerp(entity.yRotO, entity.getYRot(), partialTicks);
        float j = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.setupAnim(0.0f, h, j);
        this.model.renderToBuffer(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AllySkullBolt entity) {
        return TEXTURE_LOCATION;
    }
}