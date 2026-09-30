package com.tenken_amainu.goety_invasion.common.entities.projectiles;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.ServantUtil;
import com.tenken_amainu.goety_invasion.GINMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AllyNecroSkullBolt extends AbstractHurtingProjectile {

    private float damage = 7.0F;
    private float healAmount = 5.0F;
    private float extraDamage = 0.0F;

    private final float rotSpeed = 0.05F;
    public float roll;
    public float oRoll;
    public float getGlow;
    private float glowAmount = 0.05F;

    private final Vec3[] trailPositions = new Vec3[64];
    private int trailPointer = -1;

    public AllyNecroSkullBolt(EntityType<? extends AllyNecroSkullBolt> type, Level world) {
        super(type, world);
        this.roll = (float) (Math.random() * Math.PI * 2);
    }

    public AllyNecroSkullBolt(Level world, LivingEntity owner, double dirX, double dirY, double dirZ) {
        super(GINMod.ALLY_NECRO_SKULL_BOLT_ENTITY_TYPE.get(), owner, dirX, dirY, dirZ, world);
        this.roll = (float) (Math.random() * Math.PI * 2);
    }

    public void setDamage(float damage) { this.damage = damage; }
    public void setHealAmount(float healAmount) { this.healAmount = healAmount; }
    public void setExtraDamage(float extraDamage) { this.extraDamage = extraDamage; }
    public float getExtraDamage() { return extraDamage; }

    @Override
    public void tick() {
        super.tick();

        this.oRoll = this.roll;
        this.roll += (float) Math.PI * this.rotSpeed * 2.0F;

        this.getGlow = Mth.clamp(this.getGlow + this.glowAmount, 1.0F, 1.5F);
        if (this.getGlow == 1.0F || this.getGlow == 1.5F) this.glowAmount *= -1;

        if (this.tickCount >= MathHelper.secondsToTicks(20)) {
            this.discard();
            return;
        }

        Vec3 trailAt = this.position().add(0, this.getBbHeight() / 2F, 0);
        if (trailPointer == -1) {
            for (int i = 0; i < trailPositions.length; i++) {
                trailPositions[i] = trailAt;
            }
        }
        if (++this.trailPointer == this.trailPositions.length) {
            this.trailPointer = 0;
        }
        this.trailPositions[this.trailPointer] = trailAt;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult entityHit) {
            this.onHitEntity(entityHit);
        } else if (result instanceof BlockHitResult blockHit) {
            this.onHitBlock(blockHit);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    25, 0.25D, 0.25D, 0.25D, 0.05D);
        }
        this.discard();
    }

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
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
                target.addEffect(new MobEffectInstance(GoetyEffects.RALLIED.get(), 200, 0));
            }
            return;
        }

        float finalDamage = this.damage;
        if (owner instanceof Mob mob && mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double dmg = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (dmg > 0) finalDamage = (float) dmg;
        }
        finalDamage += extraDamage;

        target.invulnerableTime = 0;
        target.hurtTime = 0;

        if (!target.hurt(this.damageSources().magic(), finalDamage)) return;

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
        target.addEffect(new MobEffectInstance(GoetyEffects.SUN_ALLERGY.get(), 200, 0));

        if (!target.isAlive()) {
            ServantUtil.convertZombies(target, owner, true);
            boolean wither = owner instanceof Player player && SEHelper.hasResearch(player, ResearchList.BYGONE);
            ServantUtil.convertSkeletons(target, owner, wither, true);
            if (target instanceof Mob mob) ServantUtil.infect(mob, owner, true, true);
        }
    }

    public Vec3 getTrailPosition(int pointer, float partialTick) {
        if (trailPointer == -1) {
            return this.position().add(0, this.getBbHeight() / 2F, 0);
        }
        if (this.isRemoved()) partialTick = 1.0F;
        int i = this.trailPointer - pointer & 63;
        int j = this.trailPointer - pointer - 1 & 63;
        Vec3 d0 = this.trailPositions[j];
        Vec3 d1 = this.trailPositions[i];
        if (d0 == null || d1 == null) {
            return this.position().add(0, this.getBbHeight() / 2F, 0);
        }
        return d0.add(d1.subtract(d0).scale(partialTick));
    }

    public boolean hasTrail() {
        return trailPointer != -1 && trailPositions[0] != null;
    }

    @Override public boolean isOnFire() { return super.isOnFire(); }
    @Override public boolean isPickable() { return false; }
    @Override public boolean hurt(DamageSource source, float amount) { return false; }
    @Override protected boolean shouldBurn() { return false; }
}