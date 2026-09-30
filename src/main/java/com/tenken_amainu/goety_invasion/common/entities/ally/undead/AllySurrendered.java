package com.tenken_amainu.goety_invasion.common.entities.ally.undead;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ai.MinionFollowGoal;
import com.Polarice3.Goety.common.entities.ai.SummonTargetGoal;
import com.Polarice3.Goety.common.entities.neutral.Minion;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.utils.MobUtil;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class AllySurrendered extends Minion implements RangedAttackMob {

    protected static final EntityDataAccessor<Byte> DATA_VEX_FLAGS =
            SynchedEntityData.defineId(AllySurrendered.class, EntityDataSerializers.BYTE);
    private static final int CHARGING_FLAG = 1;

    @Nullable
    private BlockPos bounds;

    private boolean alive;
    private int lifeTicks;
    private int appliedPotencyLevel = -1;

    public AllySurrendered(EntityType<? extends AllySurrendered> entityType, Level world) {
        super(entityType, world);
        this.moveControl = new VexMoveControl(this);
    }

    @Override public MobType getMobType() { return MobType.UNDEAD; }
    @Override public boolean isSunBurnTick() { return false; }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.ATTACK_DAMAGE, 7.5);
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), 14);
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), 7.5);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MinionFollowGoal(this, 0.5D, 6.0f, 3.0f, true));
        this.goalSelector.addGoal(3, new ChargeTargetGoal());
        this.goalSelector.addGoal(8, new LookAtTargetGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));

        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Vex.class, false,
                vex -> !this.isHostile() && !MobUtil.areAllies(this, vex)));
        this.targetSelector.addGoal(1, new SummonTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Raider.class));
        this.targetSelector.addGoal(3, new TrackOwnerTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VEX_FLAGS, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (!this.level().isClientSide()) {
            this.applyPotencyUpgrades();
        }

        if (this.alive && --this.lifeTicks <= 0) {
            this.lifeTicks = 20;
            this.hurt(this.damageSources().starve(), 1.0f);
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            for (int i = 0; i < 2; ++i) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.WHITE_ASH,
                        this.xo, this.yo + 1.2, this.zo, 2,
                        0.2D, 0D, 0.2D, 0.025D);
            }
        }
        super.aiStep();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = AttributesConfig.SummonedVexDamage.get().floatValue();
        float knockback = 0.0F;

        if (target instanceof LivingEntity livingTarget) {
            damage += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), livingTarget.getMobType());
            knockback += EnchantmentHelper.getKnockbackBonus(this);
        }

        int fireAspect = EnchantmentHelper.getFireAspect(this);
        if (fireAspect > 0) {
            target.setSecondsOnFire(fireAspect * 4);
        }

        boolean flag = this.doHurtTarget(damage, target);
        if (!flag) return false;

        if (knockback > 0.0F && target instanceof LivingEntity living) {
            living.knockback(knockback * 0.5F,
                    Mth.sin(this.getYRot() * Mth.DEG_TO_RAD),
                    -Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
        }

        if (target instanceof Player player) {
            this.maybeDisableShield(player, this.getMainHandItem(),
                    player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
        }

        this.doEnchantDamageEffects(this, target);
        this.setLastHurtMob(target);

        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
        }
        return true;
    }

    private void applyPotencyUpgrades() {
        LivingEntity owner = this.getTrueOwner();
        if (owner == null) return;

        int potencyLevel = this.getPotencyLevel(owner);
        if (potencyLevel == this.appliedPotencyLevel) return;
        this.appliedPotencyLevel = potencyLevel;
        this.applyEquipmentForPotency(potencyLevel);
        this.applyBuffEffect(potencyLevel);
    }

    private int getPotencyLevel(LivingEntity owner) {
        Enchantment potencyEnchantment = ModEnchantments.POTENCY.get();
        if (potencyEnchantment == null) return 0;

        int maxLevel = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = owner.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            int level = EnchantmentHelper.getItemEnchantmentLevel(potencyEnchantment, stack);
            if (level > maxLevel) maxLevel = level;
        }
        return maxLevel;
    }

    private void applyEquipmentForPotency(int potencyLevel) {
        ItemStack weapon = new ItemStack(potencyLevel >= 2 ? Items.IRON_AXE : Items.IRON_SWORD);
        if (potencyLevel >= 3) {
            weapon.enchant(Enchantments.SHARPNESS, Math.min(potencyLevel - 2, Enchantments.SHARPNESS.getMaxLevel()));
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);

        ItemStack helmet = ItemStack.EMPTY;
        if (potencyLevel >= 1) {
            helmet = new ItemStack(Items.IRON_HELMET);
            if (potencyLevel >= 3) {
                helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION,
                        Math.min(potencyLevel - 2, Enchantments.ALL_DAMAGE_PROTECTION.getMaxLevel()));
            }
        }
        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setDropChance(EquipmentSlot.HEAD, 0.0f);

        ItemStack chestplate = ItemStack.EMPTY;
        if (potencyLevel >= 2) {
            chestplate = new ItemStack(Items.IRON_CHESTPLATE);
            if (potencyLevel >= 3) {
                chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION,
                        Math.min(potencyLevel - 2, Enchantments.ALL_DAMAGE_PROTECTION.getMaxLevel()));
            }
        }
        this.setItemSlot(EquipmentSlot.CHEST, chestplate);
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);

        this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        this.setDropChance(EquipmentSlot.LEGS, 0.0f);
        this.setDropChance(EquipmentSlot.FEET, 0.0f);
    }

    private void applyBuffEffect(int potencyLevel) {
        MobEffect buffEffect = GoetyEffects.BUFF.get();
        if (buffEffect == null) return;

        if (potencyLevel > 0) {
            this.addEffect(new MobEffectInstance(buffEffect, -1, potencyLevel - 1, false, false));
        } else {
            this.removeEffect(buffEffect);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.SURRENDERED_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.SURRENDERED_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.SURRENDERED_HURT_SOUND_EVENT.get(); }
    @Override public float getLightLevelDependentMagicValue() { return 1.0f; }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType spawnReason, @Nullable SpawnGroupData entityData,
                                        @Nullable CompoundTag entityNbt) {
        this.populateDefaultEquipmentSlots(world.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(world.getRandom(), difficulty);
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("BoundX")) {
            this.bounds = new BlockPos(nbt.getInt("BoundX"), nbt.getInt("BoundY"), nbt.getInt("BoundZ"));
        }
        if (nbt.contains("LifeTicks")) {
            this.setLifeTicks(nbt.getInt("LifeTicks"));
        }
        this.appliedPotencyLevel = -1;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.bounds != null) {
            nbt.putInt("BoundX", this.bounds.getX());
            nbt.putInt("BoundY", this.bounds.getY());
            nbt.putInt("BoundZ", this.bounds.getZ());
        }
        if (this.alive) {
            nbt.putInt("LifeTicks", this.lifeTicks);
        }
    }

    @Nullable public BlockPos getBounds() { return this.bounds; }
    public void setBounds(@Nullable BlockPos pos) { this.bounds = pos; }

    private boolean areFlagsSet(int mask) {
        return (this.entityData.get(DATA_VEX_FLAGS) & mask) != 0;
    }

    private void setVexFlag(int mask, boolean value) {
        int i = this.entityData.get(DATA_VEX_FLAGS);
        this.entityData.set(DATA_VEX_FLAGS, (byte) (value ? (i | mask) : (i & ~mask)));
    }

    public boolean isCharging() { return this.areFlagsSet(CHARGING_FLAG); }
    public void setCharging(boolean charging) { this.setVexFlag(CHARGING_FLAG, charging); }

    public void setLifeTicks(int lifeTicks) {
        this.alive = true;
        this.lifeTicks = lifeTicks;
    }

    @Override public void performRangedAttack(LivingEntity target, float distance) {}

    class VexMoveControl extends MoveControl {
        public VexMoveControl(AllySurrendered owner) {
            super(owner);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) return;

            Vec3 vec3d = new Vec3(this.wantedX - AllySurrendered.this.getX(),
                    this.wantedY - AllySurrendered.this.getY(),
                    this.wantedZ - AllySurrendered.this.getZ());
            double d = vec3d.length();
            if (d < AllySurrendered.this.getBoundingBox().getSize()) {
                this.operation = MoveControl.Operation.WAIT;
                AllySurrendered.this.setDeltaMovement(AllySurrendered.this.getDeltaMovement().scale(0.5));
                return;
            }

            AllySurrendered.this.setDeltaMovement(
                    AllySurrendered.this.getDeltaMovement().add(vec3d.scale(this.speedModifier * 0.05 / d)));

            double lookX, lookZ;
            if (AllySurrendered.this.getTarget() == null) {
                Vec3 motion = AllySurrendered.this.getDeltaMovement();
                lookX = motion.x;
                lookZ = motion.z;
            } else {
                lookX = AllySurrendered.this.getTarget().getX() - AllySurrendered.this.getX();
                lookZ = AllySurrendered.this.getTarget().getZ() - AllySurrendered.this.getZ();
            }
            AllySurrendered.this.setYRot(-((float) Mth.atan2(lookX, lookZ)) * 57.295776f);
            AllySurrendered.this.yBodyRot = AllySurrendered.this.getYRot();
        }
    }

    class ChargeTargetGoal extends Goal {
        public ChargeTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = AllySurrendered.this.getTarget();
            if (target == null
                    || AllySurrendered.this.getMoveControl().hasWanted()
                    || AllySurrendered.this.random.nextInt(Goal.reducedTickDelay(7)) != 0) {
                return false;
            }
            return AllySurrendered.this.distanceToSqr(target) > 4.0;
        }

        @Override
        public boolean canContinueToUse() {
            return AllySurrendered.this.getMoveControl().hasWanted()
                    && AllySurrendered.this.isCharging()
                    && AllySurrendered.this.getTarget() != null
                    && AllySurrendered.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity target = AllySurrendered.this.getTarget();
            if (target != null) {
                Vec3 vec3d = target.getEyePosition();
                AllySurrendered.this.moveControl.setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0);
            }
            AllySurrendered.this.setCharging(true);
            AllySurrendered.this.playSound(ModRegistry.SURRENDERED_CHARGE_SOUND_EVENT.get(), 1.0f, 1.0f);
        }

        @Override public void stop() { AllySurrendered.this.setCharging(false); }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            LivingEntity target = AllySurrendered.this.getTarget();
            if (target == null) return;
            if (AllySurrendered.this.getBoundingBox().intersects(target.getBoundingBox())) {
                AllySurrendered.this.doHurtTarget(target);
                AllySurrendered.this.setCharging(false);
                return;
            }
            if (AllySurrendered.this.distanceToSqr(target) < 9.0) {
                Vec3 vec3d = target.getEyePosition();
                AllySurrendered.this.moveControl.setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0);
            }
        }
    }

    class LookAtTargetGoal extends Goal {
        public LookAtTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !AllySurrendered.this.getMoveControl().hasWanted()
                    && AllySurrendered.this.random.nextInt(Goal.reducedTickDelay(7)) == 0;
        }

        @Override public boolean canContinueToUse() { return false; }

        @Override
        public void tick() {
            BlockPos blockPos = AllySurrendered.this.getBounds();
            if (blockPos == null) {
                blockPos = AllySurrendered.this.blockPosition();
            }
            for (int i = 0; i < 3; ++i) {
                BlockPos target = blockPos.offset(
                        AllySurrendered.this.random.nextInt(15) - 7,
                        AllySurrendered.this.random.nextInt(11) - 5,
                        AllySurrendered.this.random.nextInt(15) - 7);
                if (!AllySurrendered.this.level().isEmptyBlock(target)) continue;

                AllySurrendered.this.moveControl.setWantedPosition(
                        target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 0.25);
                if (AllySurrendered.this.getTarget() != null) break;

                AllySurrendered.this.getLookControl().setLookAt(
                        target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        180.0f, 20.0f);
                break;
            }
        }
    }

    class TrackOwnerTargetGoal extends TargetGoal {
        private final TargetingConditions targetPredicate;

        public TrackOwnerTargetGoal(PathfinderMob mob) {
            super(mob, false);
            this.targetPredicate = TargetingConditions.forNonCombat()
                    .ignoreLineOfSight()
                    .ignoreInvisibilityTesting();
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = AllySurrendered.this.getTrueOwner();
            if (!(owner instanceof Mob mobOwner)) return false;
            return mobOwner.getTarget() != null && this.canAttack(mobOwner.getTarget(), this.targetPredicate);
        }

        @Override
        public void start() {
            LivingEntity owner = AllySurrendered.this.getTrueOwner();
            if (owner instanceof Mob mobOwner) {
                AllySurrendered.this.setTarget(mobOwner.getTarget());
            }
            super.start();
        }
    }
}