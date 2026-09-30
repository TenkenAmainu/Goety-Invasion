package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.projectiles.AllyNecroSkullBolt;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class AllyNecroSkullBoltRenderer extends EntityRenderer<AllyNecroSkullBolt> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(GINMod.MODID, "textures/entity/projectiles/ally_necroskullbolt.png");
    private static final ResourceLocation TRAIL_TEXTURE =
            new ResourceLocation(GINMod.MODID, "textures/particle/trail.png");

    private final SkullModel model;

    public AllyNecroSkullBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKULL));
    }

    @Override
    protected int getBlockLightLevel(AllyNecroSkullBolt entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(AllyNecroSkullBolt entity, float entityYaw, float partialTicks, PoseStack stack,
                       MultiBufferSource buffer, int light) {
        stack.pushPose();
        stack.scale(-1.0F, -1.0F, 1.0F);
        float rotY = Mth.rotLerp(entity.yRotO, entity.getYRot(), partialTicks);
        float rotX = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        VertexConsumer modelConsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.setupAnim(0.0F, rotY, rotX);
        this.model.renderToBuffer(stack, modelConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        stack.popPose();

        if (entity.hasTrail()) {
            double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            float glow = entity.getGlow;
            float alpha = 0.6F + (glow - 1.0F) * 0.4F;

            stack.pushPose();
            stack.translate(-x, -y, -z);
            renderTrail(entity, partialTicks, stack, buffer, 0.0F, 1.0F, 0.2F, alpha, light);
            stack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, stack, buffer, light);
    }

    private void renderTrail(AllyNecroSkullBolt entity, float partialTicks, PoseStack poseStack,
                             MultiBufferSource buffer, float r, float g, float b, float a, int packedLight) {
        int sampleSize = 10;
        float trailWidth = 0.2F;

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TRAIL_TEXTURE));

        Vec3 drawFrom = entity.getTrailPosition(0, partialTicks);

        for (int i = 0; i < sampleSize; i++) {
            Vec3 sample = entity.getTrailPosition(i + 1, partialTicks);
            float u1 = i / (float) sampleSize;
            float u2 = u1 + (1.0F / sampleSize);

            Vec3 forward = sample.subtract(drawFrom);
            if (forward.lengthSqr() == 0) continue;

            Vec3 toCamera = this.entityRenderDispatcher.camera.getPosition().subtract(drawFrom);
            Vec3 side = forward.cross(toCamera).normalize();
            Vec3 offset = side.scale(trailWidth / 2.0F);

            vertexConsumer.vertex(matrix4f, (float)(drawFrom.x + offset.x), (float)(drawFrom.y + offset.y), (float)(drawFrom.z + offset.z))
                    .color(r, g, b, a).uv(u1, 0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(matrix3f, 0, 1, 0).endVertex();
            vertexConsumer.vertex(matrix4f, (float)(drawFrom.x - offset.x), (float)(drawFrom.y - offset.y), (float)(drawFrom.z - offset.z))
                    .color(r, g, b, a).uv(u1, 1F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(matrix3f, 0, 1, 0).endVertex();
            vertexConsumer.vertex(matrix4f, (float)(sample.x - offset.x), (float)(sample.y - offset.y), (float)(sample.z - offset.z))
                    .color(r, g, b, a).uv(u2, 1F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(matrix3f, 0, 1, 0).endVertex();
            vertexConsumer.vertex(matrix4f, (float)(sample.x + offset.x), (float)(sample.y + offset.y), (float)(sample.z + offset.z))
                    .color(r, g, b, a).uv(u2, 0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                    .normal(matrix3f, 0, 1, 0).endVertex();

            drawFrom = sample;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AllyNecroSkullBolt entity) {
        return TEXTURE;
    }
}