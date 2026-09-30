package com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound;

import com.Polarice3.Goety.common.entities.ai.AvoidTargetGoal;
import com.Polarice3.Goety.common.entities.ally.undead.bound.AbstractBoundIllager;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MobUtil;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.AllySurrendered;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import fuzs.illagerinvasion.util.TeleportUtil;
import fuzs.illagerinvasion.world.entity.monster.InvokerFangs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class BoundInvoker extends AbstractBoundIllager implements PowerableMob {
    private static final EntityDataAccessor<Boolean> DATA_IS_SHIELDED =
            SynchedEntityData.defineId(BoundInvoker.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CASTING =
            SynchedEntityData.defineId(BoundInvoker.class, EntityDataSerializers.BOOLEAN);

    public int areaDamageCooldown;
    public int teleportCooldown;
    public int fangAoeCooldown;
    public boolean isAoeCasting = false;

    private boolean flying = false;

    @Nullable
    private Sheep wololoTarget;

    public BoundInvoker(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override public boolean isCastingSpell() { return this.entityData.get(DATA_IS_CASTING); }
    public void setCasting(boolean casting) { this.entityData.set(DATA_IS_CASTING, casting); }

    @Override
    public BoundArmPose getArmPose() {
        return this.isCastingSpell() ? BoundArmPose.SPELLCASTING : super.getArmPose();
    }

    @Override
    public void tryKill(Player player) {
        if (this.killChance <= 0) {
            this.warnKill(player);
        } else {
            super.tryKill(player);
        }
    }

    @Override public boolean isPowered() { return this.isShielded(); }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BoundCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidTargetGoal<>(this, LivingEntity.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.addGoal(3, new BoundFlyAboveTargetGoal());
        this.goalSelector.addGoal(4, new BoundTeleportGoal());
        this.goalSelector.addGoal(5, new BoundAreaDamageGoal());
        this.goalSelector.addGoal(5, new BoundSummonVexGoal());
        this.goalSelector.addGoal(5, new BoundFangAoeGoal());
        this.goalSelector.addGoal(6, new BoundFangGoal());
        this.goalSelector.addGoal(6, new BoundWololoGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !this.isAlliedTo(target)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                target -> target instanceof AbstractIllager && !this.isAlliedTo(target)));
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.BoundInvokerMovementSpeed.get())
                .add(Attributes.FLYING_SPEED, GINServantConfig.BoundInvokerFlyingSpeed.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.BoundInvokerFollowRange.get())
                .add(Attributes.ARMOR, GINServantConfig.BoundInvokerArmor.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.BoundInvokerHealth.get())
                .add(Attributes.ATTACK_DAMAGE, GINServantConfig.BoundInvokerAttackDamage.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.BoundInvokerHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.BoundInvokerArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), GINServantConfig.BoundInvokerFollowRange.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), GINServantConfig.BoundInvokerAttackDamage.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SHIELDED, false);
        this.entityData.define(DATA_IS_CASTING, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setShielded(tag.getBoolean("IsShielded"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsShielded", this.isShielded());
    }

    public boolean isShielded() { return this.entityData.get(DATA_IS_SHIELDED); }
    public void setShielded(boolean shielded) { this.entityData.set(DATA_IS_SHIELDED, shielded); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                if (!this.isShielded() && this.random.nextInt(2) == 0) {
                    this.playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 0.8F + this.random.nextFloat() * 0.4F);
                    this.setShielded(true);
                }
            } else if (this.isShielded() && this.random.nextInt(3) == 0) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + 1, this.getZ(),
                        30, 0.5D, 0.7D, 0.5D, 0.5D);
                this.playSound(ModRegistry.INVOKER_SHIELD_BREAK_SOUND_EVENT.get(), 1.0f, 0.8F + this.random.nextFloat() * 0.4F);
                this.setShielded(false);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source)
                || source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                || source.is(DamageTypeTags.IS_FIRE)
                || (this.isShielded() && source.is(DamageTypeTags.IS_PROJECTILE));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!super.doHurtTarget(target)) return false;
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), this);
        }
        return true;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other == this.getTrueOwner()) return true;
        if (other instanceof Owned owned && owned.getTrueOwner() == this.getTrueOwner()) return true;
        return super.isAlliedTo(other);
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.INVOKER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.INVOKER_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.INVOKER_HURT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getCastingSoundEvent() { return ModRegistry.INVOKER_COMPLETE_CAST_SOUND_EVENT.get(); }

    @Override
    public void die(DamageSource cause) {
        this.playSound(ModSounds.DEAD_MOAN.get(), 2.0F, 1.0F);
        super.die(cause);
        if (!this.level().isClientSide) {
            Item primalEssence = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("illagerinvasion", "primal_essence"));
            if (primalEssence != null && primalEssence != Items.AIR) {
                this.spawnAtLocation(new ItemStack(primalEssence));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getRandomX(0.5D), this.getY() + 0.5D, this.getRandomZ(0.5D),
                        (0.5D - this.random.nextDouble()) * 0.15D, 0.01F,
                        (0.5D - this.random.nextDouble()) * 0.15D);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        --this.teleportCooldown;
        --this.areaDamageCooldown;
        --this.fangAoeCooldown;
        super.customServerAiStep();

        if (this.isAoeCasting && this.isCastingSpell()) {
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 1, this.getZ(),
                    2, 0.2, 0.2, 0.2, 0.06);
        }

        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && motion.y < 0.0 && !this.flying) {
            this.setDeltaMovement(motion.multiply(1.0, 0.6, 1.0));
        }

        ((ServerLevel) this.level()).sendParticles(ParticleTypes.SMOKE,
                this.getX(), this.getY() + 0.3, this.getZ(),
                1, 0.2, 0.2, 0.2, 0.005);
    }

    @Nullable Sheep getWololoTarget() { return this.wololoTarget; }
    void setWololoTarget(@Nullable Sheep sheep) { this.wololoTarget = sheep; }

    private boolean isSafeFangPosition(double x, double y, double z) {
        AABB checkArea = new AABB(x - 0.5, y, z - 0.5, x + 0.5, y + 2.0, z + 0.5);
        return this.level().getEntitiesOfClass(LivingEntity.class, checkArea,
                e -> e != this && this.isAlliedTo(e)).isEmpty();
    }

    public class BoundCastingSpellGoal extends Goal {
        @Override public boolean canUse() { return BoundInvoker.this.isCastingSpell(); }

        @Override
        public void tick() {
            LivingEntity lookTarget = BoundInvoker.this.getTarget() != null
                    ? BoundInvoker.this.getTarget()
                    : BoundInvoker.this.getWololoTarget();
            if (lookTarget != null) {
                BoundInvoker.this.getLookControl().setLookAt(lookTarget,
                        BoundInvoker.this.getMaxHeadYRot(), BoundInvoker.this.getMaxHeadXRot());
            }
        }
    }

    public class BoundFlyAboveTargetGoal extends Goal {
        private static final double MIN_HORIZONTAL_DIST = 4.0;
        private static final double MAX_HORIZONTAL_DIST = 8.0;
        private static final double HEIGHT_ABOVE_TARGET = 3.0;
        private static final double VERTICAL_SPEED = 0.08;
        private static final double HORIZONTAL_SPEED = 0.06;
        private static final double RANDOM_DRIFT_CHANCE = 0.02;

        public BoundFlyAboveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = BoundInvoker.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override public boolean canContinueToUse() { return this.canUse(); }
        @Override public void start() { BoundInvoker.this.flying = true; }
        @Override public void stop() { BoundInvoker.this.flying = false; }

        @Override
        public void tick() {
            LivingEntity target = BoundInvoker.this.getTarget();
            if (target == null) return;

            double dx = target.getX() - BoundInvoker.this.getX();
            double dz = target.getZ() - BoundInvoker.this.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            double dy = target.getY() + HEIGHT_ABOVE_TARGET - BoundInvoker.this.getY();

            Vec3 motion = BoundInvoker.this.getDeltaMovement();
            double moveX, moveZ;

            if (horizontalDist > MAX_HORIZONTAL_DIST + 1.0) {
                moveX = dx / horizontalDist * HORIZONTAL_SPEED;
                moveZ = dz / horizontalDist * HORIZONTAL_SPEED;
            } else if (horizontalDist < MIN_HORIZONTAL_DIST - 0.5) {
                if (horizontalDist > 0.01) {
                    moveX = -dx / horizontalDist * HORIZONTAL_SPEED * 1.2;
                    moveZ = -dz / horizontalDist * HORIZONTAL_SPEED * 1.2;
                } else {
                    moveX = 0;
                    moveZ = 0;
                }
            } else {
                if (BoundInvoker.this.random.nextDouble() < RANDOM_DRIFT_CHANCE) {
                    double angle = BoundInvoker.this.random.nextDouble() * 2 * Math.PI;
                    moveX = Math.cos(angle) * HORIZONTAL_SPEED * 0.5;
                    moveZ = Math.sin(angle) * HORIZONTAL_SPEED * 0.5;
                } else {
                    moveX = motion.x * 0.1;
                    moveZ = motion.z * 0.1;
                }
            }

            double moveY = Math.abs(dy) > 0.8 ? Math.signum(dy) * VERTICAL_SPEED : motion.y * 0.1;

            double lerp = 0.3;
            BoundInvoker.this.setDeltaMovement(
                    motion.x + (moveX - motion.x) * lerp,
                    motion.y + (moveY - motion.y) * lerp,
                    motion.z + (moveZ - motion.z) * lerp);
            BoundInvoker.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        @Override public boolean requiresUpdateEveryTick() { return true; }
    }

    public class BoundSummonVexGoal extends BoundUseSpellGoal {
        private final TargetingConditions SurrenderedCountTargeting =
                TargetingConditions.forNonCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();

        @Override
        public boolean canUse() {
            if (!super.canUse()) return false;
            int count = BoundInvoker.this.level().getNearbyEntities(AllySurrendered.class,
                    SurrenderedCountTargeting, BoundInvoker.this,
                    BoundInvoker.this.getBoundingBox().inflate(16.0D)).size();
            return count < 3;
        }

        @Override protected int getCastingTime() { return GINServantConfig.BoundInvokerSummonVexCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.BoundInvokerSummonVexInterval.get(); }

        @Override
        public void start() {
            BoundInvoker.this.setCasting(true);
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
            BoundInvoker.this.setCasting(false);
        }

        @Override
        protected void performSpellCasting() {
            ServerLevel serverLevel = (ServerLevel) BoundInvoker.this.level();
            int count = GINServantConfig.BoundInvokerSummonVexCount.get();
            for (int i = 0; i < count; ++i) {
                BlockPos pos = BoundInvoker.this.blockPosition().offset(
                        -2 + random.nextInt(5), 1, -2 + random.nextInt(5));
                AllySurrendered surrendered = GINMod.ALLY_SURRENDERED.get().create(BoundInvoker.this.level());
                if (surrendered == null) continue;
                surrendered.moveTo(pos, 0, 0);
                surrendered.finalizeSpawn(serverLevel,
                        BoundInvoker.this.level().getCurrentDifficultyAt(pos),
                        MobSpawnType.MOB_SUMMONED, null, null);
                surrendered.setTrueOwner(BoundInvoker.this);
                surrendered.setBoundOrigin(pos);
                surrendered.setLimitedLife(20 * (30 + random.nextInt(90)));
                serverLevel.addFreshEntityWithPassengers(surrendered);
            }
        }

        @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.INVOKER_SUMMON_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.SUMMON_VEX; }
    }

    public class BoundFangGoal extends BoundUseSpellGoal {
        @Override protected int getCastingTime() { return GINServantConfig.BoundInvokerFangCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.BoundInvokerFangInterval.get(); }

        @Override public void start() { BoundInvoker.this.setCasting(true); super.start(); }
        @Override public void stop() { super.stop(); BoundInvoker.this.setCasting(false); }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = BoundInvoker.this.getTarget();
            if (target == null) return;

            double minY = Math.min(target.getY(), BoundInvoker.this.getY());
            double maxY = Math.max(target.getY(), BoundInvoker.this.getY()) + 1.0;
            float angle = (float) Mth.atan2(target.getZ() - BoundInvoker.this.getZ(),
                    target.getX() - BoundInvoker.this.getX());

            if (BoundInvoker.this.distanceToSqr(target) < 9.0) {
                for (int i = 0; i < 5; ++i) {
                    float a = angle + i * (float) Math.PI * 0.4f;
                    createFangs(BoundInvoker.this.getX() + Mth.cos(a) * 1.5,
                            BoundInvoker.this.getZ() + Mth.sin(a) * 1.5,
                            minY, maxY, a, 0);
                }
                for (int i = 0; i < 8; ++i) {
                    float a = angle + i * (float) Math.PI * 2.0f / 8.0f + 1.2566371f;
                    createFangs(BoundInvoker.this.getX() + Mth.cos(a) * 2.5,
                            BoundInvoker.this.getZ() + Mth.sin(a) * 2.5,
                            minY, maxY, a, 3);
                }
                for (int i = 0; i < 8; ++i) {
                    float a = angle + i * (float) Math.PI * 2.0f / 8.0f + 1.2566371f;
                    createFangs(BoundInvoker.this.getX() + Mth.cos(a) * 3.5,
                            BoundInvoker.this.getZ() + Mth.sin(a) * 3.5,
                            minY, maxY, a, 3);
                }
            } else {
                for (int i = 0; i < 16; ++i) {
                    double dist = 1.25 * (i + 1);
                    createFangs(BoundInvoker.this.getX() + Mth.cos(angle) * dist,
                            BoundInvoker.this.getZ() + Mth.sin(angle) * dist,
                            minY, maxY, angle, i);
                }
                for (int i = 0; i < 16; ++i) {
                    double dist = 1.25 * (i + 1);
                    createFangs(BoundInvoker.this.getX() + Mth.cos(angle + 0.4f) * dist,
                            BoundInvoker.this.getZ() + Mth.sin(angle + 0.3f) * dist,
                            minY, maxY, angle, i);
                }
                for (int i = 0; i < 16; ++i) {
                    double dist = 1.25 * (i + 1);
                    createFangs(BoundInvoker.this.getX() + Mth.cos(angle - 0.4f) * dist,
                            BoundInvoker.this.getZ() + Mth.sin(angle - 0.3f) * dist,
                            minY, maxY, angle, i);
                }
            }
        }

        private void createFangs(double x, double z, double minY, double maxY, float yaw, int warmup) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, maxY, z);
            boolean found = false;
            double yOffset = 0.0;

            while (pos.getY() > Mth.floor(minY) - 1) {
                BlockPos below = pos.below();
                BlockState stateBelow = BoundInvoker.this.level().getBlockState(below);
                if (stateBelow.isFaceSturdy(BoundInvoker.this.level(), below, Direction.UP)) {
                    if (!BoundInvoker.this.level().isEmptyBlock(pos)) {
                        VoxelShape shape = BoundInvoker.this.level().getBlockState(pos)
                                .getCollisionShape(BoundInvoker.this.level(), pos);
                        if (!shape.isEmpty()) {
                            yOffset = shape.max(Direction.Axis.Y);
                        }
                    }
                    found = true;
                    break;
                }
                pos.move(Direction.DOWN);
            }

            if (found && isSafeFangPosition(x, pos.getY() + 0.2 + yOffset, z)) {
                InvokerFangs fangs = new InvokerFangs(BoundInvoker.this.level(),
                        x, pos.getY() + 0.2 + yOffset, z, yaw, warmup, BoundInvoker.this);
                BoundInvoker.this.level().addFreshEntity(fangs);
            }
        }

        @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.INVOKER_FANGS_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.FANGS; }
    }

    public class BoundFangAoeGoal extends BoundUseSpellGoal {
        @Override
        public boolean canUse() {
            if (BoundInvoker.this.getTarget() == null) return false;
            if (BoundInvoker.this.fangAoeCooldown > 0) return false;
            return !getTargets().isEmpty();
        }

        private List<LivingEntity> getTargets() {
            return BoundInvoker.this.level().getEntitiesOfClass(LivingEntity.class,
                    BoundInvoker.this.getBoundingBox().inflate(18),
                    entity -> !BoundInvoker.this.isAlliedTo(entity)
                            && !(entity instanceof AbstractIllager)
                            && !(entity instanceof Ravager));
        }

        @Override public void start() { BoundInvoker.this.setCasting(true); super.start(); }
        @Override public void stop() { super.stop(); BoundInvoker.this.setCasting(false); }

        @Override
        protected void performSpellCasting() {
            for (LivingEntity target : getTargets()) {
                double minY = Math.min(target.getY(), BoundInvoker.this.getY());
                double maxY = Math.max(target.getY(), BoundInvoker.this.getY()) + 1.0;
                float angle = (float) Mth.atan2(target.getZ() - BoundInvoker.this.getZ(),
                        target.getX() - BoundInvoker.this.getX());
                for (int i = 0; i < 5; ++i) {
                    float a = angle + i * (float) Math.PI * 0.4f;
                    createFangs(target.getX() + Mth.cos(a) * 1.5,
                            target.getZ() + Mth.sin(a) * 1.5,
                            minY, maxY, a, 0);
                }
            }
            BoundInvoker.this.fangAoeCooldown = GINServantConfig.BoundInvokerFangAoeCooldown.get();
        }

        private void createFangs(double x, double z, double minY, double maxY, float yaw, int warmup) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, maxY, z);
            boolean found = false;
            double yOffset = 0.0;
            while (pos.getY() > Mth.floor(minY) - 1) {
                BlockPos below = pos.below();
                BlockState stateBelow = BoundInvoker.this.level().getBlockState(below);
                if (stateBelow.isFaceSturdy(BoundInvoker.this.level(), below, Direction.UP)) {
                    if (!BoundInvoker.this.level().isEmptyBlock(pos)) {
                        VoxelShape shape = BoundInvoker.this.level().getBlockState(pos)
                                .getCollisionShape(BoundInvoker.this.level(), pos);
                        if (!shape.isEmpty()) {
                            yOffset = shape.max(Direction.Axis.Y);
                        }
                    }
                    found = true;
                    break;
                }
                pos.move(Direction.DOWN);
            }
            if (found && isSafeFangPosition(x, pos.getY() + 0.2 + yOffset, z)) {
                InvokerFangs fangs = new InvokerFangs(BoundInvoker.this.level(),
                        x, pos.getY() + 0.2 + yOffset, z, yaw, warmup + 4, BoundInvoker.this);
                BoundInvoker.this.level().addFreshEntity(fangs);
            }
        }

        @Override protected int getCastingTime() { return GINServantConfig.BoundInvokerFangAoeCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.BoundInvokerFangAoeInterval.get(); }
        @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.INVOKER_FANGS_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.FANGS; }
    }

    public class BoundAreaDamageGoal extends BoundUseSpellGoal {
        @Override
        public boolean canUse() {
            return BoundInvoker.this.getTarget() != null && BoundInvoker.this.areaDamageCooldown <= 0;
        }

        @Override
        public void start() {
            BoundInvoker.this.setCasting(true);
            BoundInvoker.this.isAoeCasting = true;
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
            BoundInvoker.this.setCasting(false);
            BoundInvoker.this.isAoeCasting = false;
        }

        @Override
        protected void performSpellCasting() {
            BoundInvoker.this.areaDamageCooldown = GINServantConfig.BoundInvokerAreaDamageCooldown.get();

            double radius = GINServantConfig.BoundInvokerAreaDamageRadius.get();
            float damage = GINServantConfig.BoundInvokerAreaDamageAmount.get().floatValue();
            double pushH = GINServantConfig.BoundInvokerAreaDamagePushHorizontal.get();
            double pushY = GINServantConfig.BoundInvokerAreaDamagePushY.get();

            List<LivingEntity> list = BoundInvoker.this.level().getEntitiesOfClass(LivingEntity.class,
                    BoundInvoker.this.getBoundingBox().inflate(radius),
                    e -> !(e instanceof AbstractIllager)
                            && !(e instanceof Ravager)
                            && !e.isAlliedTo(BoundInvoker.this)
                            && e != BoundInvoker.this.getTrueOwner());

            ServerLevel serverLevel = (ServerLevel) BoundInvoker.this.level();
            for (LivingEntity e : list) {
                double dx = e.getX() - BoundInvoker.this.getX();
                double dz = e.getZ() - BoundInvoker.this.getZ();
                double distSq = Math.max(dx * dx + dz * dz, 0.001);
                e.push(dx / distSq * pushH, pushY, dz / distSq * pushH);
                e.hurtMarked = true;
                e.hurt(BoundInvoker.this.damageSources().magic(), damage);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        e.getX(), e.getY() + 1, e.getZ(),
                        10, 0.2, 0.2, 0.2, 0.015);
            }
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    BoundInvoker.this.getX(), BoundInvoker.this.getY() + 1, BoundInvoker.this.getZ(),
                    350, 1.0, 0.8, 1.0, 0.3);
            BoundInvoker.this.isAoeCasting = false;
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.BoundInvokerAreaDamageWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.BoundInvokerAreaDamageCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.BoundInvokerAreaDamageInterval.get(); }
        @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.INVOKER_BIG_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.FANGS; }
    }

    public class BoundTeleportGoal extends BoundUseSpellGoal {
        @Override
        public boolean canUse() {
            if (BoundInvoker.this.getTarget() == null) return false;
            if (BoundInvoker.this.teleportCooldown > 0) return false;
            return !getNearbyThreats().isEmpty();
        }

        private List<LivingEntity> getNearbyThreats() {
            return BoundInvoker.this.level().getEntitiesOfClass(LivingEntity.class,
                    BoundInvoker.this.getBoundingBox().inflate(6),
                    e -> (e instanceof Player player && !player.getAbilities().instabuild)
                            || e instanceof IronGolem);
        }

        @Override
        public boolean canContinueToUse() {
            return !getNearbyThreats().isEmpty() && super.canContinueToUse();
        }

        @Override
        public void start() {
            BoundInvoker.this.setCasting(true);
            super.start();
            BoundInvoker.this.teleportCooldown = GINServantConfig.BoundInvokerTeleportCooldown.get();
        }

        @Override public void stop() { super.stop(); BoundInvoker.this.setCasting(false); }

        @Override
        protected void performSpellCasting() {
            ((ServerLevel) BoundInvoker.this.level()).sendParticles(ParticleTypes.SMOKE,
                    BoundInvoker.this.getX(), BoundInvoker.this.getY() + 1, BoundInvoker.this.getZ(),
                    30, 0.3, 0.5, 0.3, 0.015);
            TeleportUtil.tryRandomTeleport(BoundInvoker.this);
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.BoundInvokerTeleportWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.BoundInvokerTeleportCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.BoundInvokerTeleportInterval.get(); }
        @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.INVOKER_TELEPORT_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.FANGS; }
    }

    public class BoundWololoGoal extends BoundUseSpellGoal {
        private final TargetingConditions sheepPredicate = TargetingConditions.forNonCombat().range(16.0)
                .selector(e -> ((Sheep) e).getColor() == DyeColor.BLUE);

        @Override
        public boolean canUse() {
            if (BoundInvoker.this.getTarget() != null) return false;
            if (BoundInvoker.this.isCastingSpell()) return false;
            if (BoundInvoker.this.tickCount < this.nextAttackTickCount) return false;
            if (!ForgeEventFactory.getMobGriefingEvent(BoundInvoker.this.level(), BoundInvoker.this)) return false;
            List<Sheep> list = BoundInvoker.this.level().getNearbyEntities(Sheep.class, sheepPredicate,
                    BoundInvoker.this, BoundInvoker.this.getBoundingBox().inflate(16.0, 4.0, 16.0));
            if (list.isEmpty()) return false;
            BoundInvoker.this.setWololoTarget(list.get(random.nextInt(list.size())));
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return BoundInvoker.this.getWololoTarget() != null && this.attackWarmupDelay > 0;
        }

        @Override public void start() { BoundInvoker.this.setCasting(true); super.start(); }

        @Override
        public void stop() {
            super.stop();
            BoundInvoker.this.setCasting(false);
            BoundInvoker.this.setWololoTarget(null);
        }

        @Override
        protected void performSpellCasting() {
            Sheep sheep = BoundInvoker.this.getWololoTarget();
            if (sheep != null && sheep.isAlive()) {
                sheep.setColor(DyeColor.RED);
            }
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.BoundInvokerWololoWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.BoundInvokerWololoCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.BoundInvokerWololoInterval.get(); }
        @Override protected SoundEvent getSpellPrepareSound() { return SoundEvents.EVOKER_PREPARE_WOLOLO; }
        @Override protected BoundSpell getSpell() { return BoundSpell.WOLOLO; }
    }
}