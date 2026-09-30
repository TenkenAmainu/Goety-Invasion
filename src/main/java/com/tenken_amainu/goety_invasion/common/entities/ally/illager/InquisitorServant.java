package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.Polarice3.Goety.utils.MobUtil;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class InquisitorServant extends AbstractIllagerServant {
    private static final EntityDataAccessor<Boolean> STUNNED =
            SynchedEntityData.defineId(InquisitorServant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FINAL_ROAR =
            SynchedEntityData.defineId(InquisitorServant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ROAR_TICKS =
            SynchedEntityData.defineId(InquisitorServant.class, EntityDataSerializers.INT);

    public boolean finalRoar;
    public int stunTick;
    public boolean isStunned;
    public int blockedCount;

    private int shieldBlockLimit;
    private boolean needToBreakShield = false;

    public InquisitorServant(final EntityType<? extends InquisitorServant> entityType, final Level world) {
        super(entityType, world);
        this.xpReward = 25;
        this.setPathfindingMalus(BlockPathTypes.LEAVES, 0.0F);
        this.shieldBlockLimit = GINServantConfig.InquisitorShieldBlockLimit.get();
        this.stunTick = GINServantConfig.InquisitorStunDuration.get();
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.InquisitorServantMovementSpeed.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.InquisitorServantHealth.get())
                .add(Attributes.ARMOR, GINServantConfig.InquisitorServantArmor.get())
                .add(Attributes.ATTACK_DAMAGE, GINServantConfig.InquisitorServantAttackDamage.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.InquisitorServantFollowRange.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.InquisitorServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.InquisitorServantArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), GINServantConfig.InquisitorServantAttackDamage.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public void stop() {
                super.stop();
                InquisitorServant.this.setAggressive(false);
            }

            @Override
            public void start() {
                super.start();
                InquisitorServant.this.setAggressive(true);
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STUNNED, false);
        this.entityData.define(FINAL_ROAR, false);
        this.entityData.define(ROAR_TICKS, 0);
    }

    public int getRoarTicks() { return this.entityData.get(ROAR_TICKS); }
    public void setRoarTicks(int ticks) { this.entityData.set(ROAR_TICKS, ticks); }

    @Override
    protected void customServerAiStep() {
        if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
            boolean isRaided = ((ServerLevel) this.level()).isRaided(this.blockPosition());
            ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(isRaided);
        }
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (this.horizontalCollision && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            AABB box = this.getBoundingBox().inflate(1.0);
            for (BlockPos blockPos : BlockPos.betweenClosed(
                    Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                    Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
                Block block = this.level().getBlockState(blockPos).getBlock();
                if (block instanceof LeavesBlock || block instanceof DoorBlock || block instanceof WebBlock) {
                    this.level().destroyBlock(blockPos, true, this);
                    if (block instanceof DoorBlock) {
                        this.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f);
                    }
                }
            }
        }

        super.aiStep();

        if (!this.isAlive()) return;

        if (this.getStunnedState()) {
            --this.stunTick;
            if (this.stunTick <= 0) {
                this.setStunnedState(false);
                this.stunTick = GINServantConfig.InquisitorStunDuration.get();
            }
        }

        if (!this.level().isClientSide) {
            int ticks = this.getRoarTicks();
            if (ticks > 0) {
                this.setRoarTicks(ticks - 1);
            }
        }

        if (!this.level().isClientSide && this.needToBreakShield && this.isAlive()) {
            this.needToBreakShield = false;
            this.playSound(SoundEvents.SHIELD_BREAK, 1.0f, 1.0f);
            this.setStunnedState(true);
            this.setRoarTicks(GINServantConfig.InquisitorStunDuration.get());
            if (this.level() instanceof ServerLevel serverLevel) {
                ItemStack shield = this.getOffhandItem();
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, shield),
                        this.getX(), this.getY() + 1.5, this.getZ(), 30, 0.3, 0.2, 0.3, 0.003);
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.3, 0.3, 0.3, 0.1);
                this.playSound(SoundEvents.RAVAGER_ROAR, 1.0f, 1.0f);
            }
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.blockedCount = 0;
            this.shieldBlockLimit = GINServantConfig.InquisitorShieldBlockLimit.get();
            this.getTargets().forEach(this::blockedByShield);
        }
    }

    @Override
    public boolean hasLineOfSight(Entity entity) {
        return !this.getStunnedState() && super.hasLineOfSight(entity);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.getStunnedState();
    }

    public boolean getStunnedState() { return this.entityData.get(STUNNED); }
    public void setStunnedState(boolean isStunned) { this.entityData.set(STUNNED, isStunned); }
    public boolean getFinalRoarState() { return this.entityData.get(FINAL_ROAR); }
    public void setFinalRoarState(boolean hasdoneRoar) { this.entityData.set(FINAL_ROAR, hasdoneRoar); }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Stunned", this.getStunnedState());
        nbt.putBoolean("FinalRoar", this.finalRoar);
        nbt.putInt("BlockedCount", this.blockedCount);
        nbt.putInt("RoarTicks", this.getRoarTicks());
        nbt.putBoolean("NeedToBreakShield", this.needToBreakShield);
        nbt.putInt("ShieldBlockLimit", this.shieldBlockLimit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setStunnedState(nbt.getBoolean("Stunned"));
        this.setFinalRoarState(nbt.getBoolean("FinalRoar"));
        this.blockedCount = nbt.getInt("BlockedCount");
        this.setRoarTicks(nbt.getInt("RoarTicks"));
        this.needToBreakShield = nbt.getBoolean("NeedToBreakShield");
        this.shieldBlockLimit = nbt.contains("ShieldBlockLimit")
                ? nbt.getInt("ShieldBlockLimit")
                : GINServantConfig.InquisitorShieldBlockLimit.get();
    }

    @Override
    public IllagerServantArmPose getArmPose() {
        if (this.isCelebrating()) return IllagerServantArmPose.CELEBRATING;
        if (this.isAggressive()) return IllagerServantArmPose.ATTACKING;
        return IllagerServantArmPose.NEUTRAL;
    }

    @Override public SoundEvent getCelebrateSound() { return SoundEvents.VINDICATOR_CELEBRATE; }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new Navigation(this, world);
    }

    private List<LivingEntity> getTargets() {
        return this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(8.0),
                entity -> entity != this.getTrueOwner() && !this.isAlliedTo(entity));
    }

    private void knockBack(Entity entity) {
        double d = entity.getX() - this.getX();
        double e = entity.getZ() - this.getZ();
        double f = Math.max(d * d + e * e, 0.001);
        entity.push(d / f * 0.6, 0.4, e / f * 0.6);
    }

    @Override
    protected void blockedByShield(LivingEntity target) {
        if (target == this) return;
        this.knockBack(target);
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
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
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.shieldBlockLimit = GINServantConfig.InquisitorShieldBlockLimit.get();
    }

    @Override
    protected void enchantSpawnedWeapon(RandomSource random, float p_219057_) {
        super.enchantSpawnedWeapon(random, p_219057_);
        if (random.nextInt(300) != 0) return;
        ItemStack mainhand = this.getMainHandItem();
        if (!mainhand.is(Items.STONE_SWORD)) return;
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(mainhand);
        map.putIfAbsent(Enchantments.SHARPNESS, 1);
        EnchantmentHelper.setEnchantments(map, mainhand);
        this.setItemSlot(EquipmentSlot.MAINHAND, mainhand);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other == this.getTrueOwner()) return true;
        if (super.isAlliedTo(other)) return true;
        return other instanceof LivingEntity living
                && living.getMobType() == MobType.ILLAGER
                && this.getTeam() == null && other.getTeam() == null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker == this.getTrueOwner()) {
            return super.hurt(source, amount);
        }
        if (!this.isAggressive()) {
            return super.hurt(source, amount);
        }

        boolean hasShield = this.getOffhandItem().is(Items.SHIELD);

        if (attacker instanceof LivingEntity livingAttacker && hasShield) {
            ItemStack weapon = livingAttacker.getMainHandItem();
            if (weapon.is(ItemTags.AXES)
                    || attacker instanceof IronGolem
                    || this.blockedCount >= this.shieldBlockLimit) {
                boolean hurtResult = super.hurt(source, amount);
                this.needToBreakShield = true;
                return hurtResult;
            }
        }

        if (hasShield && (source.getDirectEntity() instanceof AbstractArrow
                || source.getDirectEntity() instanceof LivingEntity)) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 1.0f);
            ++this.blockedCount;
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.ILLAGER_BRUTE_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.ILLAGER_BRUTE_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.ILLAGER_BRUTE_HURT_SOUND_EVENT.get(); }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();

        if (this.getTrueOwner() != player || player.getOffhandItem().getItem() instanceof IWand) {
            return super.mobInteract(player, hand);
        }

        if (stack.is(Items.IRON_INGOT)) {
            boolean hasShield = this.getOffhandItem().is(Items.SHIELD);

            if (hasShield && this.blockedCount == 0) {
                player.displayClientMessage(
                        Component.translatable("message.goety_invasion.inquisitor_servant.shield_full"), true);
                return InteractionResult.PASS;
            }

            if (hasShield) {
                this.blockedCount = 0;
            } else {
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                this.blockedCount = 0;
                this.shieldBlockLimit = GINServantConfig.InquisitorShieldBlockLimit.get();
            }

            this.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);
            spawnHappyParticles();
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ItemTags.PLANKS)) {
            if (!this.getOffhandItem().is(Items.SHIELD)) {
                player.displayClientMessage(
                        Component.translatable("message.goety_invasion.inquisitor_servant.shield_broken"), true);
                return InteractionResult.PASS;
            }
            if (this.blockedCount == 0) {
                player.displayClientMessage(
                        Component.translatable("message.goety_invasion.inquisitor_servant.shield_full"), true);
                return InteractionResult.PASS;
            }
            this.blockedCount = 0;
            this.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);
            spawnHappyParticles();
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (item instanceof SwordItem || item instanceof AxeItem) {
            ItemStack mainhand = this.getMainHandItem();
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
            this.dropEquipment(EquipmentSlot.MAINHAND, mainhand.copyAndClear());
            this.setItemSlot(EquipmentSlot.MAINHAND, stack.copyWithCount(1));
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            spawnHappyParticles();
            if (!player.getAbilities().instabuild) stack.shrink(1);
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

    static class NodeEvaluator extends WalkNodeEvaluator {
        @Override
        protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, BlockPos blockPos, BlockPathTypes blockPathTypes) {
            return blockPathTypes == BlockPathTypes.LEAVES
                    ? BlockPathTypes.OPEN
                    : super.evaluateBlockPathType(blockGetter, blockPos, blockPathTypes);
        }
    }

    static class Navigation extends GroundPathNavigation {
        public Navigation(Mob mob, Level world) {
            super(mob, world);
        }

        @Override
        protected PathFinder createPathFinder(int range) {
            this.nodeEvaluator = new NodeEvaluator();
            return new PathFinder(this.nodeEvaluator, range);
        }
    }
}