package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ai.AvoidTargetGoal;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ally.illager.SpellcasterIllagerServant;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.projectiles.AllyNecroSkullBolt;
import com.tenken_amainu.goety_invasion.common.entities.projectiles.AllySkullBolt;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class NecromancerServant extends SpellcasterIllagerServant implements PowerableMob {

    private static final EntityDataAccessor<Boolean> DATA_IS_SHIELDED =
            SynchedEntityData.defineId(NecromancerServant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_NECRO_LEVEL =
            SynchedEntityData.defineId(NecromancerServant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_SOUL_JAR =
            SynchedEntityData.defineId(NecromancerServant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_HEART_OF_NIGHT =
            SynchedEntityData.defineId(NecromancerServant.class, EntityDataSerializers.BOOLEAN);

    private int conjureSkullCooldown;
    private final List<EntityType<?>> summonList = new ArrayList<>();

    public static int getMaxLevel() {
        return GINServantConfig.NecromancerMaxLevel.get();
    }

    @Override
    public void tryKill(Player player) {
        if (this.killChance <= 0) {
            this.warnKill(player);
        } else {
            super.tryKill(player);
        }
    }

    private static final Map<ResourceLocation, ResourceLocation> SUMMON_TYPE_REGISTRY = new HashMap<>();

    public static void registerSummonType(ResourceLocation itemId, ResourceLocation entityId) {
        SUMMON_TYPE_REGISTRY.put(itemId, entityId);
    }

    static {
        registerSummonType(new ResourceLocation("goety_cataclysm", "desert_raid_focus"),
                new ResourceLocation("goety_cataclysm", "koboleton_servant"));
        registerSummonType(new ResourceLocation("goety_cataclysm", "cursed_grave_focus"),
                new ResourceLocation("goety_cataclysm", "draugr_servant"));
        registerSummonType(new ResourceLocation("goety_cataclysm", "cursed_cairn_focus"),
                new ResourceLocation("goety_cataclysm", "elite_draugr_servant"));
        registerSummonType(new ResourceLocation("goety_cataclysm", "cursed_tomb_focus"),
                new ResourceLocation("goety_cataclysm", "royal_draugr_servant"));
        registerSummonType(new ResourceLocation("goetyawaken", "champion_focus"),
                new ResourceLocation("goetyawaken", "vanguard_champion"));
        registerSummonType(new ResourceLocation("goetyominous", "murmur_focus"),
                new ResourceLocation("goetyominous", "murmur_servant"));
        registerSummonType(new ResourceLocation("goetyominous", "urbhadhach_focus"),
                new ResourceLocation("goetyominous", "urbhadhach_servant"));
        registerSummonType(new ResourceLocation("goetytwilight", "skeleton_druid_focus"),
                new ResourceLocation("goetytwilight", "skeleton_druid_servant"));
    }

    public NecromancerServant(EntityType<? extends NecromancerServant> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.summonList.add(ModEntityType.ZOMBIE_SERVANT.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new NecromancerCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidTargetGoal<>(this, LivingEntity.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.addGoal(3, new SummonUndeadGoal());
        this.goalSelector.addGoal(4, new ConjureSkullGoal());
    }

    @Override
    public void miscGoal() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new RaiderWanderGoal<>(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.NecromancerServantMovementSpeed.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.NecromancerServantFollowRange.get())
                .add(Attributes.ARMOR, GINServantConfig.NecromancerServantArmor.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.NecromancerServantHealth.get());
    }

    @Override
    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.NecromancerServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.NecromancerServantArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), GINServantConfig.NecromancerServantFollowRange.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SHIELDED, false);
        this.entityData.define(DATA_NECRO_LEVEL, 0);
        this.entityData.define(DATA_HAS_SOUL_JAR, false);
        this.entityData.define(DATA_HAS_HEART_OF_NIGHT, false);
    }

    public boolean getShieldedState() { return this.entityData.get(DATA_IS_SHIELDED); }
    public void setShieldedState(boolean shielded) { this.entityData.set(DATA_IS_SHIELDED, shielded); }

    public int getNecroLevel() {
        return Mth.clamp(this.entityData.get(DATA_NECRO_LEVEL), 0, getMaxLevel());
    }

    public void setNecroLevel(int level) {
        int clamped = Mth.clamp(level, 0, getMaxLevel());
        this.entityData.set(DATA_NECRO_LEVEL, clamped);
        AttributeInstance healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            double health = GINServantConfig.NecromancerServantHealth.get()
                    * (1.0D + clamped * GINServantConfig.NecromancerHealthBonusPerLevel.get());
            if (this.hasHeartOfNight()) {
                health *= GINServantConfig.NecromancerHeartOfNightHealthMultiplier.get();
            }
            healthAttr.setBaseValue(health);
        }
        this.refreshDimensions();
        this.heal(this.getMaxHealth());
    }

    public boolean hasIllSoulJar() { return this.entityData.get(DATA_HAS_SOUL_JAR); }
    public void setHasIllSoulJar(boolean has) { this.entityData.set(DATA_HAS_SOUL_JAR, has); }
    public boolean hasHeartOfNight() { return this.entityData.get(DATA_HAS_HEART_OF_NIGHT); }
    public void setHasHeartOfNight(boolean has) { this.entityData.set(DATA_HAS_HEART_OF_NIGHT, has); }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_NECRO_LEVEL.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float scale = 1.0F + getNecroLevel() * GINServantConfig.NecromancerScaleBonusPerLevel.get().floatValue();
        return super.getDimensions(pose).scale(scale);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getShieldedState()) {
            amount *= GINServantConfig.NecromancerShieldDamageReduction.get().floatValue();
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPowered() {
        return this.getShieldedState();
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        --this.conjureSkullCooldown;

        double auraRadius = GINServantConfig.NecromancerAuraRadius.get();
        List<Mob> undeadAllies = this.level().getEntitiesOfClass(Mob.class,
                this.getBoundingBox().inflate(auraRadius),
                mob -> mob.getMobType() == MobType.UNDEAD && mob.isAlliedTo(this));

        if (!undeadAllies.isEmpty()) {
            undeadAllies.forEach(this::applyUndeadBuff);
            if (this.tickCount % 10 == 0) undeadAllies.forEach(this::spawnBuffParticles);
        }
        if (this.tickCount % 20 == 0) this.setShieldedState(!undeadAllies.isEmpty());
        if (this.getTarget() != null) {
            double shareRadius = GINServantConfig.NecromancerTargetShareRadius.get();
            this.level().getEntitiesOfClass(Mob.class,
                            this.getBoundingBox().inflate(shareRadius),
                            mob -> mob.getMobType() == MobType.UNDEAD && mob.isAlliedTo(this))
                    .forEach(mob -> mob.setTarget(this.getTarget()));
        }
    }

    private void applyUndeadBuff(LivingEntity entity) {
        int duration = GINServantConfig.NecromancerAuraEffectDuration.get();
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration,
                GINServantConfig.NecromancerAuraDamageBoostAmplifier.get()));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration,
                GINServantConfig.NecromancerAuraSpeedAmplifier.get()));
        entity.clearFire();
        if (entity instanceof Mob mob) mob.setTarget(this.getTarget());
    }

    private void spawnBuffParticles(LivingEntity entity) {
        if (this.level().isClientSide) return;
        ServerLevel sl = (ServerLevel) this.level();
        sl.sendParticles(ModRegistry.NECROMANCER_BUFF_PARTICLE_TYPE.get(),
                entity.getX(), entity.getY() + 1.0, entity.getZ(), 1, 0.4, 0.5, 0.4, 0.015);
        sl.sendParticles(ModRegistry.NECROMANCER_BUFF_PARTICLE_TYPE.get(),
                this.getX(), this.getY() + 1.0, this.getZ(), 1, 0.4, 0.5, 0.4, 0.015);
    }

    @Override
    public void spellParticles() {
        if (this.level().isClientSide && this.isCastingSpell()) {
            float f = this.yBodyRot * Mth.DEG_TO_RAD + Mth.cos((float) this.tickCount * 0.6662F) * 0.25F;
            float f1 = Mth.cos(f);
            float f2 = Mth.sin(f);
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                    this.getX() + f1 * 0.6, this.getY() + 1.8, this.getZ() + f2 * 0.6,
                    0.5, 0.0, 1.0);
            this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                    this.getX() - f1 * 0.6, this.getY() + 1.8, this.getZ() - f2 * 0.6,
                    0.5, 0.0, 1.0);
            return;
        }
        super.spellParticles();
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide
                && this.hasIllSoulJar()
                && this.hasEffect(getWoundedEffect())) {
            this.dropLegacyIllSoulJar();
        }
        super.die(cause);
    }

    private void dropLegacyIllSoulJar() {
        if (!(this.level() instanceof ServerLevel) || !(this.getTrueOwner() instanceof Player player)) return;
        ItemStack legacyJar = new ItemStack(GINMod.ILL_NECRO_SOUL_JAR.get());
        legacyJar.getOrCreateTag().putBoolean("Legacy", true);
        if (!player.getInventory().add(legacyJar)) {
            this.spawnAtLocation(legacyJar);
        }
    }

    @Nullable
    private MobEffect getWoundedEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "wounded"));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.getTrueOwner() != player) {
            return super.mobInteract(player, hand);
        }

        ItemStack stack = player.getItemInHand(hand);

        Item emptySoulJar = ForgeRegistries.ITEMS.getValue(new ResourceLocation("goety", "empty_soul_jar"));
        if (!this.hasIllSoulJar() && emptySoulJar != null && stack.is(emptySoulJar)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.setHasIllSoulJar(true);
            this.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
            if (this.level() instanceof ServerLevel sl) {
                ServerParticleUtil.addParticlesAroundSelf(sl, ParticleTypes.SOUL, this);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.getNecroLevel() < getMaxLevel() && stack.is(GINMod.ILL_NECRO_SOUL_JAR.get())) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.setNecroLevel(this.getNecroLevel() + 1);
            this.playSound(SoundEvents.ILLUSIONER_AMBIENT, 1.0F, 1.0F);
            if (this.level() instanceof ServerLevel sl) {
                ServerParticleUtil.addParticlesAroundSelf(sl, ParticleTypes.SCULK_SOUL, this);
            }
            return InteractionResult.SUCCESS;
        }

        if (!this.hasHeartOfNight() && stack.is(ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("goety", "heart_of_the_night")))) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            this.setHasHeartOfNight(true);
            this.setNecroLevel(this.getNecroLevel());
            this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 0.5F);
            if (this.level() instanceof ServerLevel sl) {
                ServerParticleUtil.addParticlesAroundSelf(sl, ParticleTypes.SOUL_FIRE_FLAME, this);
            }
            return InteractionResult.SUCCESS;
        }

        if (tryAddSummonType(stack, ModItems.ROTTING_FOCUS.get(), ModEntityType.ZOMBIE_SERVANT.get(), player)) return InteractionResult.SUCCESS;
        if (tryAddSummonType(stack, ModItems.OSSEOUS_FOCUS.get(), ModEntityType.SKELETON_SERVANT.get(), player)) return InteractionResult.SUCCESS;
        if (tryAddSummonType(stack, ModItems.SPOOKY_FOCUS.get(), ModEntityType.WRAITH_SERVANT.get(), player)) return InteractionResult.SUCCESS;
        if (tryAddSummonType(stack, ModItems.REAPING_FOCUS.get(), ModEntityType.REAPER_SERVANT.get(), player)) return InteractionResult.SUCCESS;
        if (tryAddSummonType(stack, ModItems.VANGUARD_FOCUS.get(), ModEntityType.VANGUARD_SERVANT.get(), player)) return InteractionResult.SUCCESS;
        if (tryAddSummonType(stack, ModItems.BLACKGUARD_FOCUS.get(), ModEntityType.BLACKGUARD_SERVANT.get(), player)) return InteractionResult.SUCCESS;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && SUMMON_TYPE_REGISTRY.containsKey(itemId)) {
            ResourceLocation entityId = SUMMON_TYPE_REGISTRY.get(itemId);
            Optional<EntityType<?>> optionalType = EntityType.byString(entityId.toString());
            if (optionalType.isPresent()) {
                EntityType<?> type = optionalType.get();
                if (!this.summonList.contains(type)) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    this.summonList.add(type);
                    this.playSound(SoundEvents.ILLUSIONER_AMBIENT, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        ServerParticleUtil.addParticlesAroundSelf(sl, ParticleTypes.ENCHANT, this);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.mobInteract(player, hand);
    }

    private boolean tryAddSummonType(ItemStack stack, Item focusItem, EntityType<?> type, Player player) {
        if (!stack.is(focusItem) || this.summonList.contains(type)) return false;
        if (!player.getAbilities().instabuild) stack.shrink(1);
        this.summonList.add(type);
        this.playSound(SoundEvents.ILLUSIONER_AMBIENT, 1.0F, 1.0F);
        if (this.level() instanceof ServerLevel sl) {
            ServerParticleUtil.addParticlesAroundSelf(sl, ParticleTypes.ENCHANT, this);
        }
        return true;
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ILLUSIONER_AMBIENT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ILLUSIONER_DEATH; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ILLUSIONER_HURT; }
    @Override public SoundEvent getCelebrateSound() { return SoundEvents.ILLUSIONER_AMBIENT; }
    @Override protected SoundEvent getCastingSoundEvent() { return SoundEvents.EVOKER_CAST_SPELL; }

    class NecromancerCastingSpellGoal extends SpellcasterCastingSpellGoal {
        @Override
        public void tick() {
            if (NecromancerServant.this.getTarget() != null) {
                NecromancerServant.this.getLookControl().setLookAt(
                        NecromancerServant.this.getTarget(),
                        NecromancerServant.this.getMaxHeadYRot(),
                        NecromancerServant.this.getMaxHeadXRot());
            }
        }
    }

    class SummonUndeadGoal extends SpellcasterUseSpellGoal {

        @Override
        public boolean canUse() {
            if (!super.canUse()) return false;
            Predicate<Entity> predicate = entity -> entity instanceof Summoned summoned &&
                    summoned.getTrueOwner() == NecromancerServant.this.getTrueOwner();
            int count = NecromancerServant.this.level().getEntitiesOfClass(Summoned.class,
                    NecromancerServant.this.getBoundingBox().inflate(64.0, 16.0, 64.0), predicate).size();
            return count < GINServantConfig.NecromancerSummonMax.get();
        }

        @Override protected int getCastingTime() { return GINServantConfig.NecromancerSummonCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.NecromancerSummonInterval.get(); }

        @Override
        protected void performSpellCasting() {
            ServerLevel serverLevel = (ServerLevel) NecromancerServant.this.level();
            int level = NecromancerServant.this.getNecroLevel();
            int baseAmount = serverLevel.isNight()
                    ? GINServantConfig.NecromancerSummonBaseNight.get()
                    : GINServantConfig.NecromancerSummonBaseDay.get();
            int totalSpawns = baseAmount + level;

            if (NecromancerServant.this.hasHeartOfNight()) {
                totalSpawns *= GINServantConfig.NecromancerSummonHeartOfNightMultiplier.get();
            }

            List<EntityType<?>> pool = new ArrayList<>(NecromancerServant.this.summonList);
            if (pool.isEmpty()) pool.add(ModEntityType.ZOMBIE_SERVANT.get());

            for (int i = 0; i < totalSpawns; i++) {
                EntityType<?> type = pool.get(NecromancerServant.this.random.nextInt(pool.size()));
                BlockPos pos = NecromancerServant.this.blockPosition().offset(
                        -2 + NecromancerServant.this.random.nextInt(5), 1,
                        -2 + NecromancerServant.this.random.nextInt(5));

                Entity entity = type.create(NecromancerServant.this.level());
                if (!(entity instanceof Summoned summoned)) continue;

                summoned.moveTo(pos, 0.0F, 0.0F);
                summoned.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos),
                        MobSpawnType.MOB_SUMMONED, null, null);
                summoned.setTrueOwner(NecromancerServant.this.getTrueOwner());

                if (NecromancerServant.this.hasHeartOfNight()) {
                    AttributeInstance healthAttr = summoned.getAttribute(Attributes.MAX_HEALTH);
                    if (healthAttr != null) {
                        healthAttr.setBaseValue(healthAttr.getBaseValue()
                                * GINServantConfig.NecromancerHeartOfNightHealthMultiplier.get());
                        summoned.setHealth(summoned.getMaxHealth());
                    }
                }

                serverLevel.addFreshEntityWithPassengers(summoned);
            }
        }

        @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.NECROMANCER_SUMMON_SOUND_EVENT.get(); }
        @Override protected IllagerServantSpell getSpell() { return IllagerServantSpell.SUMMON_VEX; }
    }

    class ConjureSkullGoal extends SpellcasterUseSpellGoal {
        @Override
        public boolean canUse() {
            if (NecromancerServant.this.getTarget() == null) return false;
            if (NecromancerServant.this.conjureSkullCooldown > 0) return false;
            return super.canUse();
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = NecromancerServant.this.getTarget();
            if (target == null) return;

            double x = NecromancerServant.this.getX();
            double y = NecromancerServant.this.getY() + 2.5;
            double z = NecromancerServant.this.getZ();
            int level = NecromancerServant.this.getNecroLevel();
            int bolts = GINServantConfig.NecromancerSkullBaseCount.get()
                    + level * GINServantConfig.NecromancerSkullPerLevel.get();
            boolean useAdvanced = NecromancerServant.this.hasHeartOfNight();

            for (int i = 0; i < bolts; i++) {
                float spread = (i - (bolts - 1) / 2.0F) * 0.3F;
                double dx = target.getX() - x + spread;
                double dy = target.getY() + target.getEyeHeight() * 0.5 - y;
                double dz = target.getZ() - z + spread;

                AbstractHurtingProjectile bolt = useAdvanced
                        ? new AllyNecroSkullBolt(NecromancerServant.this.level(), NecromancerServant.this, dx, dy, dz)
                        : new AllySkullBolt(NecromancerServant.this.level(), NecromancerServant.this, dx, dy, dz);
                bolt.setPosRaw(x, y, z);
                NecromancerServant.this.level().addFreshEntity(bolt);
            }

            if (!NecromancerServant.this.level().isClientSide) {
                ServerLevel sl = (ServerLevel) NecromancerServant.this.level();
                sl.sendParticles(ParticleTypes.SMOKE, x, y, z, 40, 0.4D, 0.4D, 0.4D, 0.15D);
            }
            NecromancerServant.this.conjureSkullCooldown = GINServantConfig.NecromancerSkullCooldown.get();
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.NecromancerSkullWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.NecromancerSkullCastingTime.get(); }
        @Override protected int getCastingInterval() { return GINServantConfig.NecromancerSkullInterval.get(); }
        @Override protected SoundEvent getSpellPrepareSound() { return SoundEvents.EVOKER_PREPARE_ATTACK; }
        @Override protected IllagerServantSpell getSpell() { return IllagerServantSpell.FANGS; }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.conjureSkullCooldown = compound.getInt("ConjureSkullCooldown");
        this.setHasIllSoulJar(compound.getBoolean("HasIllSoulJar"));
        this.setHasHeartOfNight(compound.getBoolean("HasHeartOfNight"));
        this.setNecroLevel(compound.getInt("NecroLevel"));

        this.summonList.clear();
        if (compound.contains("SummonList", Tag.TAG_LIST)) {
            ListTag list = compound.getList("SummonList", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                EntityType.byString(list.getString(i)).ifPresent(this.summonList::add);
            }
        } else {
            this.summonList.add(ModEntityType.ZOMBIE_SERVANT.get());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ConjureSkullCooldown", this.conjureSkullCooldown);
        compound.putInt("NecroLevel", this.getNecroLevel());
        compound.putBoolean("HasIllSoulJar", this.hasIllSoulJar());
        compound.putBoolean("HasHeartOfNight", this.hasHeartOfNight());

        ListTag list = new ListTag();
        for (EntityType<?> type : this.summonList) {
            list.add(StringTag.valueOf(EntityType.getKey(type).toString()));
        }
        compound.put("SummonList", list);
    }
}