package com.tenken_amainu.goety_invasion.common.ai.goal;

import com.tenken_amainu.goety_invasion.common.entities.ally.illager.MarauderServant;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ServantHatchetAttackGoal extends Goal {
    public static final UniformInt COOLDOWN_RANGE = TimeUtil.rangeOfSeconds(1, 2);

    private final MarauderServant mob;
    private final double speed;
    private final float minSafeDistSq;
    private final float maxSafeDistSq;
    private final float minAttackDistSq;
    private final float moveDistance;
    private int seeingTargetTicker;
    private int cooldown = -1;
    private int chargeTime = 0;

    public ServantHatchetAttackGoal(MarauderServant mob, double speed, int attackInterval, float maxRange) {
        this(mob, speed, attackInterval, maxRange, maxRange * 0.8F, maxRange * 1.2F, 0.0F);
    }

    public ServantHatchetAttackGoal(MarauderServant mob, double speed, int attackInterval,
                                    float maxRange, float minSafeDistance, float maxSafeDistance,
                                    float minAttackDistance) {
        this.mob = mob;
        this.speed = speed;
        this.minSafeDistSq = minSafeDistance * minSafeDistance;
        this.maxSafeDistSq = maxSafeDistance * maxSafeDistance;
        this.minAttackDistSq = minAttackDistance * minAttackDistance;
        this.moveDistance = maxSafeDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()
                || !this.mob.getMainHandItem().is(ModRegistry.PLATINUM_INFUSED_HATCHET_ITEM.get())
                || !this.mob.canAttack(target)) {
            return false;
        }
        if (this.mob.isGuardingArea() && this.mob.distanceToSqr(target) < this.minAttackDistSq) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        this.chargeTime = 0;
    }

    @Override
    public void stop() {
        this.mob.stopUsingItem();
        this.mob.setAggressive(false);
        this.seeingTargetTicker = 0;
        this.mob.setCharging(false);
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            this.chargeTime = 0;
            this.mob.setCharging(false);
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        boolean canSee = this.mob.getSensing().hasLineOfSight(target);
        if (canSee != (this.seeingTargetTicker > 0)) {
            this.seeingTargetTicker = 0;
        }
        this.seeingTargetTicker += canSee ? 1 : -1;

        double distSq = this.mob.distanceToSqr(target);

        if (!canSee) {
            if (--this.cooldown <= 0) {
                this.mob.getNavigation().moveTo(target, this.speed);
                this.cooldown = COOLDOWN_RANGE.sample(this.mob.getRandom());
            }
        } else if (distSq < this.minSafeDistSq) {
            if (--this.cooldown <= 0) {
                double dx = this.mob.getX() - target.getX();
                double dz = this.mob.getZ() - target.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0.01) {
                    double moveX = this.mob.getX() + dx / len * this.moveDistance;
                    double moveZ = this.mob.getZ() + dz / len * this.moveDistance;
                    this.mob.getNavigation().moveTo(moveX, target.getY(), moveZ, this.speed);
                } else {
                    this.mob.getNavigation().moveTo(this.mob.getX() + 1, target.getY(), this.mob.getZ(), this.speed);
                }
                this.cooldown = COOLDOWN_RANGE.sample(this.mob.getRandom());
            }
        } else if (distSq > this.maxSafeDistSq) {
            if (--this.cooldown <= 0) {
                this.mob.getNavigation().moveTo(target, this.speed);
                this.cooldown = COOLDOWN_RANGE.sample(this.mob.getRandom());
            }
        } else {
            this.mob.getNavigation().stop();
            this.cooldown = 0;
        }

        --this.chargeTime;
        if (this.chargeTime == -40) {
            this.mob.setCharging(true);
        }
        if (this.chargeTime == -80) {
            if (distSq >= this.minAttackDistSq) {
                this.mob.performRangedAttack(target, 1.0F);
            }
            this.mob.setCharging(false);
            this.chargeTime = 0;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}