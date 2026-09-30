package com.tenken_amainu.goety_invasion.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.client.model.AllySurrenderedModel;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.AllySurrendered;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AllySurrenderedRenderer extends HumanoidMobRenderer<AllySurrendered, AllySurrenderedModel<AllySurrendered>> {

    private static final ResourceLocation SURRENDERED_LOCATION = GINMod.location("textures/entity/servant/ally_surrendered.png");
    private static final ResourceLocation SURRENDERED_CHARGE_LOCATION = GINMod.location("textures/entity/servant/ally_surrendered_charge.png");

    public AllySurrenderedRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public AllySurrenderedRenderer(EntityRendererProvider.Context context,
                                   ModelLayerLocation modelLayerLocation,
                                   ModelLayerLocation modelLayerLocation2,
                                   ModelLayerLocation modelLayerLocation3) {
        super(context, new AllySurrenderedModel<>(context.bakeLayer(modelLayerLocation)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new AllySurrenderedModel<>(context.bakeLayer(modelLayerLocation2)),
                new AllySurrenderedModel<>(context.bakeLayer(modelLayerLocation3)),
                context.getModelManager()
        ));
    }

    @Override
    protected void scale(@NotNull AllySurrendered allySurrendered, PoseStack matrixStack, float f) {
        matrixStack.scale(0.85f, 0.85f, 0.85f);
    }

    @Override
    public Vec3 getRenderOffset(AllySurrendered allySurrendered, float partialTicks) {
        return new Vec3(0.0, -0.35, 0.0);
    }

    @Override
    public ResourceLocation getTextureLocation(AllySurrendered allySurrendered) {
        return allySurrendered.isCharging() ? SURRENDERED_CHARGE_LOCATION : SURRENDERED_LOCATION;
    }
}