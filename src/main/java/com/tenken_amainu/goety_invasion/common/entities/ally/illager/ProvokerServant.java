package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.entities.ally.illager.SpellcasterIllagerServant;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.MobUtil;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ProvokerServant extends SpellcasterIllagerServant implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> IS_CASTING_SPELL =
            SynchedEntityData.defineId(ProvokerServant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_BOW =
            SynchedEntityData.defineId(ProvokerServant.class, EntityDataSerializers.BOOLEAN);

    private RangedBowAttackGoal<ProvokerServant> bowGoal;
    private MeleeAttackGoal meleeGoal;
    private BuffAllyGoal buffAllyGoal;

    public ProvokerServant(EntityType<? extends ProvokerServant> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.buffAllyGoal = new BuffAllyGoal();
        this.bowGoal = new RangedBowAttackGoal<>(
                this,
                GINServantConfig.ProvokerBowSpeed.get(),
                GINServantConfig.ProvokerBowAttackInterval.get(),
                GINServantConfig.ProvokerBowRange.get().floatValue());
        this.meleeGoal = new MeleeAttackGoal(this, 1.2, false) {
            @Override
            public void stop() {
                super.stop();
                ProvokerServant.this.setAggressive(false);
            }

            @Override
            public void start() {
                super.start();
                ProvokerServant.this.setAggressive(true);
            }
        };

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, this.buffAllyGoal);
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.ProvokerServantMovementSpeed.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.ProvokerServantHealth.get())
                .add(Attributes.ARMOR, GINServantConfig.ProvokerServantArmor.get())
                .add(Attributes.ATTACK_DAMAGE, GINServantConfig.ProvokerServantAttackDamage.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.ProvokerServantFollowRange.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.ProvokerServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.ProvokerServantArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), GINServantConfig.ProvokerServantAttackDamage.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CASTING_SPELL, false);
        this.entityData.define(IS_CHARGING_BOW, false);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide && slot.getType() == EquipmentSlot.Type.HAND) {
            this.reassessWeaponGoal();
        }
    }

    public void reassessWeaponGoal() {
        if (this.level().isClientSide) return;
        if (this.bowGoal == null || this.meleeGoal == null) return;
        this.goalSelector.removeGoal(this.bowGoal);
        this.goalSelector.removeGoal(this.meleeGoal);
        this.goalSelector.addGoal(3,
                this.getMainHandItem().getItem() instanceof BowItem ? this.bowGoal : this.meleeGoal);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack projectileStack = this.getProjectile(this.getItemInHand(
                ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem)));
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectileStack, distanceFactor);
        if (arrow == null) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontalDist * 0.2, dz, 1.6F,
                (float) (14 - this.level().getDifficulty().getId() * 4));

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F,
                1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    public boolean isCastingSpell() { return this.entityData.get(IS_CASTING_SPELL); }
    public void setCastingSpell(boolean casting) { this.entityData.set(IS_CASTING_SPELL, casting); }

    @Override protected SoundEvent getCastingSoundEvent() { return SoundEvents.EVOKER_CAST_SPELL; }

    @Override
    public IllagerServantArmPose getArmPose() {
        if (this.isCastingSpell()) return IllagerServantArmPose.SPELLCASTING;
        if (this.isAggressive() || this.getTarget() != null) return IllagerServantArmPose.BOW_AND_ARROW;
        return IllagerServantArmPose.CROSSED;
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.PROVOKER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.PROVOKER_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.PROVOKER_HURT_SOUND_EVENT.get(); }
    @Override public SoundEvent getCelebrateSound() { return ModRegistry.PROVOKER_CELEBRATE_SOUND_EVENT.get(); }

    private class BuffAllyGoal extends Goal {
        private int spellTicks;
        private int cooldownTicks;

        public BuffAllyGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldownTicks > 0) return false;
            return !getTargets().isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return spellTicks > 0;
        }

        @Override
        public void start() {
            spellTicks = getWarmupTime();
            ProvokerServant.this.setCastingSpell(true);
            ProvokerServant.this.getNavigation().stop();
            ProvokerServant.this.playSound(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, 1.0F, 1.0F);
        }

        @Override
        public void stop() {
            ProvokerServant.this.setCastingSpell(false);
            spellTicks = 0;
        }

        @Override
        public void tick() {
            if (spellTicks > 0) {
                --spellTicks;
                if (spellTicks == 0) {
                    performSpell();
                    cooldownTicks = getCastingInterval();
                    ProvokerServant.this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 1.0F);
                } else {
                    List<LivingEntity> targets = getTargets();
                    if (!targets.isEmpty()) {
                        ProvokerServant.this.getLookControl().setLookAt(targets.get(0), 30.0F, 30.0F);
                    }
                }
            } else if (cooldownTicks > 0) {
                --cooldownTicks;
            }
        }

        private List<LivingEntity> getTargets() {
            double radius = GINServantConfig.ProvokerBuffRadius.get();
            return ProvokerServant.this.level().getEntitiesOfClass(LivingEntity.class,
                    ProvokerServant.this.getBoundingBox().inflate(radius),
                    entity -> (entity == ProvokerServant.this
                            || ProvokerServant.this.isAlliedTo(entity)) && entity.isAlive());
        }

        private void buff(LivingEntity entity) {
            int duration = GINServantConfig.ProvokerBuffDuration.get();
            int amp = GINServantConfig.ProvokerBuffAmplifier.get();
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amp));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amp));
            if (ProvokerServant.this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        entity.getX(), entity.getY() + 1, entity.getZ(),
                        10, 0.4, 0.4, 0.4, 0.15);
            }
        }

        private void performSpell() {
            getTargets().forEach(this::buff);
        }

        private int getWarmupTime() { return GINServantConfig.ProvokerBuffWarmup.get(); }
        private int getCastingInterval() { return GINServantConfig.ProvokerBuffInterval.get(); }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.getTrueOwner() != player || player.getOffhandItem().getItem() instanceof IWand) {
            return super.mobInteract(player, hand);
        }

        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();

        if (item instanceof BowItem || stack.is(ModTags.Items.PILLAGER_WEAPONS)) {
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
            this.dropEquipment(EquipmentSlot.MAINHAND, this.getMainHandItem().copyAndClear());
            this.setItemSlot(EquipmentSlot.MAINHAND, stack.copyWithCount(1));
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            spawnHappyParticles();
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (item instanceof ArrowItem) {
            this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
            this.dropEquipment(EquipmentSlot.OFFHAND, this.getOffhandItem().copyAndClear());
            this.setItemSlot(EquipmentSlot.OFFHAND, stack.split(64));
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
            spawnHappyParticles();
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private void spawnHappyParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
                    0, d0, d1, d2, 0.5);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag tag) {
        RandomSource random = level.getRandom();
        this.populateDefaultEquipmentSlots(random, difficulty);
        this.populateDefaultEquipmentEnchantments(random, difficulty);
        this.reassessWeaponGoal();
        return super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
    }

    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    protected void enchantSpawnedWeapon(RandomSource random, float difficultyScaled) {
        super.enchantSpawnedWeapon(random, difficultyScaled);
        if (random.nextInt(300) != 0) return;
        ItemStack bow = this.getMainHandItem();
        if (!(bow.getItem() instanceof BowItem)) return;
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(bow);
        enchants.putIfAbsent(Enchantments.POWER_ARROWS, 1);
        EnchantmentHelper.setEnchantments(enchants, bow);
        this.setItemSlot(EquipmentSlot.MAINHAND, bow);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.reassessWeaponGoal();
    }

    @Override public float getWalkTargetValue(BlockPos pos, LevelReader level) { return 0.0F; }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) {
        return weapon instanceof BowItem;
    }
}