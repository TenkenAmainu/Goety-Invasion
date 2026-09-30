package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.tenken_amainu.goety_invasion.common.ai.goal.ServantHatchetAttackGoal;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class MarauderServant extends AbstractIllagerServant implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(MarauderServant.class, EntityDataSerializers.BOOLEAN);

    public MarauderServant(EntityType<? extends MarauderServant> entityType, Level world) {
        super(entityType, world);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, GINServantConfig.MarauderServantHealth.get())
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.MarauderServantMovementSpeed.get())
                .add(Attributes.ATTACK_DAMAGE, GINServantConfig.MarauderServantAttackDamage.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.MarauderServantFollowRange.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MarauderMeleeGoal(
                this, 1.0,
                GINServantConfig.MarauderMeleeAttackRange.get().floatValue()));
        this.goalSelector.addGoal(3, new ServantHatchetAttackGoal(
                this, 1.0, 100, 8.0F, 3.8F, 4.8F, 3.8F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this, Raider.class));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGING, false);
    }

    public boolean isCharging() { return this.entityData.get(DATA_CHARGING); }
    public void setCharging(boolean charging) { this.entityData.set(DATA_CHARGING, charging); }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType spawnReason, @Nullable SpawnGroupData entityData,
                                        @Nullable CompoundTag entityNbt) {
        this.setItemSlot(EquipmentSlot.MAINHAND,
                new ItemStack(ModRegistry.PLATINUM_INFUSED_HATCHET_ITEM.get()));
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        fuzs.illagerinvasion.world.entity.projectile.Hatchet hatchet =
                new fuzs.illagerinvasion.world.entity.projectile.Hatchet(this.level(), this,
                        new ItemStack(ModRegistry.PLATINUM_INFUSED_HATCHET_ITEM.get()));

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - hatchet.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        hatchet.shoot(dx, dy + horizontalDist * 0.2, dz, 1.2f,
                14 - this.level().getDifficulty().getId() * 4);
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0f,
                1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(hatchet);
    }

    @Override public SoundEvent getCelebrateSound() { return SoundEvents.PILLAGER_CELEBRATE; }
    @Override public float getWalkTargetValue(BlockPos pos, LevelReader level) { return 0.0F; }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                this.isCharging()
                        ? GINServantConfig.MarauderServantChargingSpeed.get()
                        : GINServantConfig.MarauderServantMovementSpeed.get());
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other == null) return false;
        if (other == this) return true;
        if (super.isAlliedTo(other)) return true;
        if (other instanceof Vex vex) {
            return this.isAlliedTo(vex.getOwner());
        }
        return other instanceof LivingEntity living
                && living.getMobType() == MobType.ILLAGER
                && this.getTeam() == null && other.getTeam() == null;
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PILLAGER_HURT; }

    @Override
    public IllagerServantArmPose getArmPose() {
        return this.isAggressive() ? IllagerServantArmPose.ATTACKING : IllagerServantArmPose.NEUTRAL;
    }

    private static class MarauderMeleeGoal extends Goal {
        private final MarauderServant mob;
        private final double speedModifier;
        private final float attackRange;
        private int attackTime;
        private int attackAnimTick;

        MarauderMeleeGoal(MarauderServant mob, double speedModifier, float attackRange) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.attackRange = attackRange;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) return false;
            return this.mob.isGuardingArea()
                    && this.mob.distanceToSqr(target) <= this.attackRange * this.attackRange;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.attackTime = 0;
            this.attackAnimTick = 0;
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            this.mob.setAggressive(false);
            this.mob.setCharging(false);
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) return;

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.mob.distanceToSqr(target);
            double rangeSq = this.attackRange * this.attackRange;

            if (distSq > rangeSq) {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                return;
            }

            this.mob.getNavigation().stop();
            this.mob.setCharging(false);

            if (this.attackAnimTick > 0) {
                --this.attackAnimTick;
                if (this.attackAnimTick == 0) {
                    this.mob.setAggressive(false);
                }
            }

            if (this.attackTime > 0) {
                this.attackTime--;
                return;
            }

            this.mob.setAggressive(true);
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
            this.attackTime = GINServantConfig.MarauderMeleeAttackInterval.get();
            this.attackAnimTick = GINServantConfig.MarauderMeleeAttackAnimDuration.get();
        }
    }
}