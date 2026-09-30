package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.Polarice3.Goety.utils.MobUtil;
import com.google.common.collect.Maps;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BasherServant extends AbstractIllagerServant {
    private static final String TAG_STUN_TICKS = "Stunned";
    private static final EntityDataAccessor<Boolean> DATA_STUNNED =
            SynchedEntityData.defineId(BasherServant.class, EntityDataSerializers.BOOLEAN);

    private int stunTicks;
    private int blockedCount;

    public BasherServant(EntityType<? extends BasherServant> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false) {
            @Override
            public void stop() {
                super.stop();
                BasherServant.this.setAggressive(false);
            }

            @Override
            public void start() {
                super.start();
                BasherServant.this.setAggressive(true);
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
            boolean isRaided = ((ServerLevel) this.level()).isRaided(this.blockPosition());
            ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(isRaided);
        }
        super.customServerAiStep();
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.BasherServantMovementSpeed.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.BasherServantHealth.get())
                .add(Attributes.ARMOR, GINServantConfig.BasherServantArmor.get())
                .add(Attributes.ATTACK_DAMAGE, GINServantConfig.BasherServantAttackDamage.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.BasherServantFollowRange.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.BasherServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.BasherServantArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), GINServantConfig.BasherServantAttackDamage.get());
    }

    public void reassessWeaponGoal() {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STUNNED, false);
    }

    public boolean isStunned() {
        return this.entityData.get(DATA_STUNNED);
    }

    public void setStunTicks(int stunTicks) {
        this.stunTicks = stunTicks;
        this.entityData.set(DATA_STUNNED, stunTicks > 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt(TAG_STUN_TICKS, this.stunTicks);
        nbt.putInt("BlockedCount", this.blockedCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setStunTicks(nbt.getInt(TAG_STUN_TICKS));
        this.blockedCount = nbt.getInt("BlockedCount");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive() && this.stunTicks > 0) {
            this.setStunTicks(this.stunTicks - 1);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isStunned();
    }

    @Override
    public boolean hasLineOfSight(Entity entity) {
        return !this.isStunned() && super.hasLineOfSight(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (this.getTrueOwner() != null && attacker == this.getTrueOwner()) {
            return super.hurt(source, amount);
        }

        ItemStack mainHand = this.getMainHandItem();
        if (!this.isAggressive() || !mainHand.is(Items.SHIELD)) {
            return super.hurt(source, amount);
        }

        boolean isAxe = attacker instanceof LivingEntity living && living.getMainHandItem().is(ItemTags.AXES);
        boolean isGolem = attacker instanceof IronGolem;
        boolean exceedBlockLimit = this.blockedCount >= GINServantConfig.BasherShieldBlockLimit.get();

        if (isAxe || isGolem || exceedBlockLimit) {
            this.playSound(SoundEvents.SHIELD_BREAK, 1.0f, 1.0f);
            this.setStunTicks(GINServantConfig.BasherStunDuration.get());
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, mainHand),
                        this.getX(), this.getY() + 1.5, this.getZ(), 30,
                        0.3, 0.2, 0.3, 0.003);
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
            this.blockedCount = 0;
            return super.hurt(source, amount);
        }

        if (source.getDirectEntity() instanceof AbstractArrow
                || source.getDirectEntity() instanceof LivingEntity) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 1.0f);
            this.blockedCount++;
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public IllagerServantArmPose getArmPose() {
        if (this.isCelebrating()) return IllagerServantArmPose.CELEBRATING;
        if (this.isAggressive()) return IllagerServantArmPose.ATTACKING;
        return IllagerServantArmPose.CROSSED;
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.BASHER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.BASHER_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.BASHER_HURT_SOUND_EVENT.get(); }
    @Override public SoundEvent getCelebrateSound() { return ModRegistry.BASHER_CELEBRATE_SOUND_EVENT.get(); }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType spawnReason, @Nullable SpawnGroupData entityData,
                                        @Nullable CompoundTag entityNbt) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.populateDefaultEquipmentSlots(world.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(world.getRandom(), difficulty);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected void enchantSpawnedWeapon(RandomSource random, float difficulty) {
        if (random.nextInt(300) != 0) return;
        ItemStack stack = this.getMainHandItem();
        if (!stack.is(Items.SHIELD)) return;
        Map<Enchantment, Integer> map = Maps.newHashMap();
        map.put(Enchantments.UNBREAKING, 1);
        EnchantmentHelper.setEnchantments(map, stack);
        this.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.getTrueOwner() != player || player.getOffhandItem().getItem() instanceof IWand) {
            return super.mobInteract(player, hand);
        }

        if (stack.is(Items.SHIELD)) {
            ItemStack mainHandItem = this.getMainHandItem();
            if (mainHandItem.is(Items.SHIELD)) {
                this.blockedCount = 0;
                this.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);
            } else {
                if (!mainHandItem.isEmpty()) {
                    this.spawnAtLocation(mainHandItem);
                }
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
                this.blockedCount = 0;
                this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
            }
            spawnHappyParticles();
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ItemTags.AXES)) {
            ItemStack currentMainHand = this.getMainHandItem();
            if (!currentMainHand.isEmpty()) {
                this.spawnAtLocation(currentMainHand);
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, stack.split(1));
            this.blockedCount = 0;
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
            spawnHappyParticles();
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private void spawnHappyParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D),
                    0, d0, d1, d2, 0.5F);
        }
    }
}