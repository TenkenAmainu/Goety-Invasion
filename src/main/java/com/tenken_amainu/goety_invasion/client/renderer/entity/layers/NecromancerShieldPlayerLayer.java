package com.tenken_amainu.goety_invasion.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tenken_amainu.goety_invasion.GINMod;
import fuzs.illagerinvasion.IllagerInvasion;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class NecromancerShieldPlayerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation SHIELD_TEXTURE = IllagerInvasion.id("textures/entity/necromancer_armor1.png");
    private final PlayerModel<AbstractClientPlayer> shieldModel;

    public NecromancerShieldPlayerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                                        PlayerModel<AbstractClientPlayer> model) {
        super(parent);
        this.shieldModel = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.hasEffect(GINMod.NECROMANCER_SHIELD.get())) return;

        float time = entity.tickCount + partialTicks;
        PlayerModel<AbstractClientPlayer> model = this.shieldModel;
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.getParentModel().copyPropertiesTo(model);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.energySwirl(
                SHIELD_TEXTURE,
                Mth.cos(time * 0.2f) * 0.2f % 1.0F,
                time * 0.01F % 1.0F
        ));

        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
    }
}