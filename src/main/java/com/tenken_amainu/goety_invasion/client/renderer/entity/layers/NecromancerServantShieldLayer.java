package com.tenken_amainu.goety_invasion.client.renderer.entity.layers;

import com.Polarice3.Goety.client.render.model.IllagerServantModel;
import com.tenken_amainu.goety_invasion.GIN_CMod;
import com.tenken_amainu.goety_invasion.common.entities.ally.illager.NecromancerServant;
import fuzs.illagerinvasion.IllagerInvasion;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class NecromancerServantShieldLayer extends EnergySwirlLayer<NecromancerServant, IllagerServantModel<NecromancerServant>> {
    private static final ResourceLocation SHIELD_TEXTURE = IllagerInvasion.id("textures/entity/necromancer_armor1.png");
    private final IllagerServantModel<NecromancerServant> shieldModel;

    public NecromancerServantShieldLayer(RenderLayerParent<NecromancerServant, IllagerServantModel<NecromancerServant>> parent, EntityModelSet modelSet) {
        super(parent);
        this.shieldModel = new IllagerServantModel<>(modelSet.bakeLayer(GIN_CMod.NECROMANCER_SERVANT_SHIELD));
    }

    @Override
    protected float xOffset(float partialAge) {
        return Mth.cos(partialAge * 0.2f) * 0.2f;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return SHIELD_TEXTURE;
    }

    @Override
    protected IllagerServantModel<NecromancerServant> model() {
        return this.shieldModel;
    }


    protected boolean isPowered(NecromancerServant entity) {
        return entity.getShieldedState();
    }
}