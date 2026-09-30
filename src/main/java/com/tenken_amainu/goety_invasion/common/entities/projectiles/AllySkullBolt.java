package com.tenken_amainu.goety_invasion.common.entities.projectiles;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AllySkullBolt extends AbstractHurtingProjectile {

    float damage = 7.0f;
    float healAmount = 5.0f;

    public AllySkullBolt(EntityType<? extends AllySkullBolt> entityType, Level world) {
        super(entityType, world);
    }

    public AllySkullBolt(Level world, LivingEntity owner, double directionX, double directionY, double directionZ) {
        super(ModRegistry.SKULL_BOLT_ENTITY_TYPE.get(), owner, directionX, directionY, directionZ, world);
    }

    public void setDamage(float damage) { this.damage = damage; }
    public void setHealAmount(float healAmount) { this.healAmount = healAmount; }

    @Override public boolean isOnFire() { return super.isOnFire(); }
    @Override public boolean isPickable() { return false; }
    @Override public boolean hurt(DamageSource source, float amount) { return false; }
    @Override protected boolean shouldBurn() { return false; }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        if (!(result.getEntity() instanceof LivingEntity target)) return;

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        boolean isAlly = owner != null && (target.isAlliedTo(owner)
                || (target instanceof Summoned summoned && summoned.getTrueOwner() == owner));

        if (isAlly) {
            if (target.getMobType() == MobType.UNDEAD) {
                target.heal(healAmount);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
            }
            return;
        }

        target.hurt(this.damageSources().magic(), damage);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    25, 0.25D, 0.25D, 0.25D, 0.05D);
        }
        this.discard();
    }
}