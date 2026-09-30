package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.common.entities.ai.SummonTargetGoal;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.Polarice3.Goety.common.entities.ally.illager.raider.RaiderServant;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.utils.MobUtil;
import com.tenken_amainu.goety_invasion.common.ai.goal.AllyPotionBowAttackGoal;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AlchemistServant extends AbstractIllagerServant implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_USING_ITEM =
            SynchedEntityData.defineId(AlchemistServant.class, EntityDataSerializers.BOOLEAN);

    private int potionCooldown = 0;
    private ItemStack savedBow = ItemStack.EMPTY;

    public AlchemistServant(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.getMainHandItem().isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AllyPotionBowAttackGoal<>(this, 1.0D, 20, 15.0F));
    }

    @Override
    public void targetSelectGoal() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, RaiderServant.class));
        this.targetSelector.addGoal(2, new SummonTargetGoal(this));
    }

    @Override
    public void miscGoal() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new RaiderWanderGoal<>(this, 1.0D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_USING_ITEM, false);
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, GINServantConfig.AlchemistServantHealth.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.AlchemistServantFollowRange.get())
                .add(Attributes.ARMOR, GINServantConfig.AlchemistServantArmor.get())
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.AlchemistServantMovementSpeed.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.AlchemistServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.AlchemistServantArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), GINServantConfig.AlchemistServantFollowRange.get());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        if (this.potionCooldown > 0) {
            --this.potionCooldown;
        }

        if (this.potionCooldown <= 0 && this.getMainHandItem().getItem() instanceof BowItem) {
            this.savedBow = this.getMainHandItem().copy();
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.LINGERING_POTION));
        }

        List<AreaEffectCloud> clouds = this.level().getEntitiesOfClass(
                AreaEffectCloud.class,
                this.getBoundingBox().inflate(30.0),
                Entity::isAlive);
        for (AreaEffectCloud cloud : clouds) {
            removeEffectsInCloud(cloud);
        }

        if (this.random.nextFloat() < 7.5E-4F) {
            this.level().broadcastEntityEvent(this, (byte) 15);
        }
    }

    private void removeEffectsInCloud(AreaEffectCloud cloud) {
        List<LivingEntity> allies = this.level().getEntitiesOfClass(
                LivingEntity.class,
                cloud.getBoundingBox().inflate(0.3),
                e -> e.isAlive() && this.isAlliedTarget(e));
        for (LivingEntity ally : allies) {
            cloud.getPotion().getEffects().stream()
                    .findAny()
                    .map(MobEffectInstance::getEffect)
                    .ifPresent(ally::removeEffect);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack mainHand = this.getMainHandItem();
        if (mainHand.is(Items.LINGERING_POTION)) {
            this.throwPotion(target);
        } else if (mainHand.getItem() instanceof BowItem) {
            this.shootArrow(target, velocity);
        }
    }

    private void throwPotion(LivingEntity target) {
        Vec3 delta = target.getDeltaMovement();
        double d0 = target.getX() + delta.x - this.getX();
        double d1 = target.getEyeY() - 1.1 - this.getY();
        double d2 = target.getZ() + delta.z - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        ThrownPotion thrownPotion = new ThrownPotion(this.level(), this);
        thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), this.selectPotionForTarget(target)));
        thrownPotion.setXRot(thrownPotion.getXRot() + 20.0F);
        thrownPotion.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);
        this.playSound(SoundEvents.WITCH_THROW, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        this.level().addFreshEntity(thrownPotion);

        this.setItemSlot(EquipmentSlot.MAINHAND, this.savedBow.isEmpty() ? new ItemStack(Items.BOW) : this.savedBow.copy());
        this.potionCooldown = GINServantConfig.AlchemistPotionCooldown.get();
    }

    private Potion selectPotionForTarget(LivingEntity target) {
        if (this.isAlliedTarget(target) && this.getTarget() != target) {
            if (target.isInvertedHealAndHarm()) {
                return Potions.HARMING;
            }
            return target.getHealth() <= target.getMaxHealth() * 0.25F ? Potions.HEALING : Potions.REGENERATION;
        }
        double distanceSq = this.distanceToSqr(target);
        if (distanceSq >= 64.0 && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            return Potions.SLOWNESS;
        }
        if (target.getHealth() >= 8.0F
                && target.canBeAffected(new MobEffectInstance(MobEffects.POISON))
                && !target.hasEffect(MobEffects.POISON)) {
            return Potions.POISON;
        }
        if (distanceSq <= 9.0
                && !target.hasEffect(MobEffects.WEAKNESS)
                && this.random.nextFloat() < 0.25F) {
            return Potions.WEAKNESS;
        }
        return target.isInvertedHealAndHarm() ? Potions.HEALING : Potions.HARMING;
    }

    private void shootArrow(LivingEntity target, float velocity) {
        ItemStack arrowStack = this.getProjectile(this.getItemInHand(
                ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem)));
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, velocity);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.33) - arrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.2, d2, 1.6F,
                (float) (14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F,
                1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.potionCooldown = tag.getInt("PotionCooldown");
        if (tag.contains("SavedBow")) {
            this.savedBow = ItemStack.of(tag.getCompound("SavedBow"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PotionCooldown", this.potionCooldown);
        if (!this.savedBow.isEmpty()) {
            tag.put("SavedBow", this.savedBow.save(new CompoundTag()));
        }
    }

    public boolean isAlliedTarget(LivingEntity target) {
        if (this.getTrueOwner() == null) {
            return MobUtil.areAllies(this, target);
        }
        return target == this.getTrueOwner()
                || MobUtil.getOwner(target) == this.getTrueOwner()
                || (MobUtil.areAllies(this.getTrueOwner(), target) && MobUtil.notTargetingAlly(target, this));
    }

    @Override public SoundEvent getCelebrateSound() { return SoundEvents.EVOKER_CELEBRATE; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ILLUSIONER_AMBIENT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ILLUSIONER_DEATH; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ILLUSIONER_HURT; }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != 15) {
            super.handleEntityEvent(id);
            return;
        }
        for (int i = 0; i < this.random.nextInt(35) + 10; ++i) {
            this.level().addParticle(ParticleTypes.WITCH,
                    this.getX() + this.random.nextGaussian() * 0.13,
                    this.getBoundingBox().maxY + 0.5 + this.random.nextGaussian() * 0.13,
                    this.getZ() + this.random.nextGaussian() * 0.13,
                    0.0, 0.0, 0.0);
        }
    }

    public AbstractIllagerServant.IllagerServantArmPose getArmPose() {
        if (this.isAggressive() && this.getMainHandItem().getItem() instanceof BowItem) {
            return IllagerServantArmPose.BOW_AND_ARROW;
        }
        if (this.isAggressive() && this.getMainHandItem().is(Items.LINGERING_POTION)) {
            return IllagerServantArmPose.ATTACKING;
        }
        if (this.isCelebrating()) {
            return IllagerServantArmPose.CELEBRATING;
        }
        return IllagerServantArmPose.CROSSED;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.getTrueOwner() == null || player != this.getTrueOwner()) {
            return super.mobInteract(player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof BowItem || stack.is(Items.LINGERING_POTION)) {
            return equipWeapon(player, stack);
        }
        if (stack.getItem() instanceof ArmorItem) {
            return equipArmor(player, stack);
        }
        return super.mobInteract(player, hand);
    }

    private InteractionResult equipWeapon(Player player, ItemStack weaponStack) {
        ItemStack currentWeapon = this.getMainHandItem();

        if (!currentWeapon.isEmpty() && !player.getAbilities().instabuild) {
            ItemStack oldWeapon = currentWeapon.copy();
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!player.getInventory().add(oldWeapon)) {
                this.spawnAtLocation(oldWeapon);
            }
        } else if (!currentWeapon.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, weaponStack.split(1));
        playWeaponEquipSound(weaponStack);
        spawnInteractionParticles();

        if (weaponStack.getItem() instanceof BowItem) {
            this.savedBow = ItemStack.EMPTY;
            this.potionCooldown = 0;
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult equipArmor(Player player, ItemStack armorStack) {
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(armorStack);
        if (slot.getType() != EquipmentSlot.Type.ARMOR) {
            return InteractionResult.PASS;
        }
        ItemStack oldArmor = this.getItemBySlot(slot);
        if (!oldArmor.isEmpty()) {
            this.spawnAtLocation(oldArmor);
            this.setItemSlot(slot, ItemStack.EMPTY);
        }
        this.setItemSlot(slot, armorStack.split(1));
        this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
        spawnInteractionParticles();
        return InteractionResult.SUCCESS;
    }

    private void playWeaponEquipSound(ItemStack stack) {
        if (stack.is(Items.LINGERING_POTION)) {
            this.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 1.0F);
        } else {
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
        }
    }

    private void spawnInteractionParticles() {
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

    @Override
    public boolean canPickUpLoot() {
        return false;
    }
}