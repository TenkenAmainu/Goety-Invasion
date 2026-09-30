package com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound;

import com.Polarice3.Goety.common.entities.ai.AvoidTargetGoal;
import com.Polarice3.Goety.common.entities.ally.undead.bound.AbstractBoundIllager;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MobUtil;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.blockentity.AllyMagicFireBlockEntity;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import fuzs.illagerinvasion.util.TeleportUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class BoundSorcerer extends AbstractBoundIllager {

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.BoundSorcererMovementSpeed.get())
                .add(Attributes.FLYING_SPEED, GINServantConfig.BoundSorcererFlyingSpeed.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.BoundSorcererFollowRange.get())
                .add(Attributes.ARMOR, GINServantConfig.BoundSorcererArmor.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.BoundSorcererHealth.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.BoundSorcererHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.BoundSorcererArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), GINServantConfig.BoundSorcererFollowRange.get());
    }

    public enum BoundSorcererSpell {
        NONE(0, 0, 0, 0),
        TELEPORT(1, 0.7, 0.0, 0.7),
        FLAMES(2, 1.0, 0.5, 0.0);

        final int id;
        final double r, g, b;

        BoundSorcererSpell(int id, double r, double g, double b) {
            this.id = id;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        static BoundSorcererSpell byId(int id) {
            BoundSorcererSpell[] values = values();
            return id >= 0 && id < values.length ? values[id] : NONE;
        }
    }

    private static final EntityDataAccessor<Byte> DATA_SPELL =
            SynchedEntityData.defineId(BoundSorcerer.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_IS_CASTING =
            SynchedEntityData.defineId(BoundSorcerer.class, EntityDataSerializers.BOOLEAN);

    private BoundSorcererSpell currentSpell = BoundSorcererSpell.NONE;
    private int castTeleportCooldown;
    private int conjureFlamesCooldown;

    public BoundSorcerer(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BoundCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidTargetGoal<>(this, LivingEntity.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.addGoal(4, new BoundTeleportGoal());
        this.goalSelector.addGoal(5, new BoundConjureFlamesGoal());
    }

    @Override
    public void miscGoal() {
        this.goalSelector.addGoal(8, new RaiderWanderGoal<>(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL, (byte) 0);
        this.entityData.define(DATA_IS_CASTING, false);
    }

    @Override public boolean isCastingSpell() { return this.entityData.get(DATA_IS_CASTING); }
    public void setCasting(boolean casting) { this.entityData.set(DATA_IS_CASTING, casting); }

    @Override
    public BoundArmPose getArmPose() {
        return this.isCastingSpell() ? BoundArmPose.SPELLCASTING : super.getArmPose();
    }

    private void setSpell(BoundSorcererSpell spell) {
        this.currentSpell = spell;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_SPELL, (byte) spell.id);
        }
    }

    private BoundSorcererSpell getSpell() {
        if (this.level().isClientSide) {
            return BoundSorcererSpell.byId(this.entityData.get(DATA_SPELL));
        }
        return this.currentSpell;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) return;

        float f = this.yBodyRot * Mth.DEG_TO_RAD + Mth.cos((float) this.tickCount * 0.6662F) * 0.25F;
        float f1 = Mth.cos(f);
        float f2 = Mth.sin(f);

        this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                this.getX() + (double) f1 * 0.6D, this.getY() + 1.8D, this.getZ() + (double) f2 * 0.6D,
                1.0D, 1.0D, 1.0D);
        this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                this.getX() - (double) f1 * 0.6D, this.getY() + 1.8D, this.getZ() - (double) f2 * 0.6D,
                1.0D, 1.0D, 1.0D);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.castTeleportCooldown > 0) --this.castTeleportCooldown;
        if (this.conjureFlamesCooldown > 0) --this.conjureFlamesCooldown;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.castTeleportCooldown = tag.getInt("CastTeleportCooldown");
        this.conjureFlamesCooldown = tag.getInt("ConjureFlamesCooldown");
        this.currentSpell = BoundSorcererSpell.byId(tag.getInt("Spell"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CastTeleportCooldown", this.castTeleportCooldown);
        tag.putInt("ConjureFlamesCooldown", this.conjureFlamesCooldown);
        tag.putInt("Spell", this.currentSpell.id);
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.SORCERER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.SORCERER_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.SORCERER_HURT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getCastingSoundEvent() { return ModRegistry.SORCERER_COMPLETE_CAST_SOUND_EVENT.get(); }
    @Override public SoundEvent getCelebrateSound() { return ModRegistry.SORCERER_CELEBRATE_SOUND_EVENT.get(); }

    @Override
    public void die(DamageSource cause) {
        this.playSound(ModSounds.DEAD_MOAN.get(), 2.0F, 1.0F);
        super.die(cause);
    }

    @Override public float getVoicePitch() { return 0.45F; }

    @Override
    public void tryKill(Player player) {
        if (this.killChance <= 0) {
            this.warnKill(player);
        } else {
            super.tryKill(player);
        }
    }

    @Override public int xpReward() { return 15; }

    class BoundCastingSpellGoal extends AbstractBoundIllager.BoundCastingSpellGoal {
        @Override
        public void tick() {
            if (BoundSorcerer.this.getTarget() != null) {
                BoundSorcerer.this.getLookControl().setLookAt(
                        BoundSorcerer.this.getTarget(),
                        BoundSorcerer.this.getMaxHeadYRot(),
                        BoundSorcerer.this.getMaxHeadXRot());
            }
        }
    }

    public class BoundTeleportGoal extends BoundUseSpellGoal {
        @Override
        public boolean canUse() {
            LivingEntity target = BoundSorcerer.this.getTarget();
            if (target == null) return false;
            if (BoundSorcerer.this.isCastingSpell()) return false;
            if (BoundSorcerer.this.castTeleportCooldown > 0) return false;
            double range = GINServantConfig.BoundSorcererTeleportRange.get();
            return BoundSorcerer.this.distanceToSqr(target) < range * range && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            BoundSorcerer.this.setCasting(true);
            BoundSorcerer.this.setSpell(BoundSorcererSpell.TELEPORT);
        }

        @Override
        public void stop() {
            super.stop();
            BoundSorcerer.this.setCasting(false);
            BoundSorcerer.this.setSpell(BoundSorcererSpell.NONE);
        }

        @Override
        protected void performSpellCasting() {
            BoundSorcerer.this.castTeleportCooldown = GINServantConfig.BoundSorcererTeleportCooldown.get();
            double x = BoundSorcerer.this.getX();
            double y = BoundSorcerer.this.getY() + 1;
            double z = BoundSorcerer.this.getZ();
            if (BoundSorcerer.this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, x, y, z, 30, 0.3, 0.5, 0.3, 0.015);
            }
            TeleportUtil.tryRandomTeleport(BoundSorcerer.this);
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.BoundSorcererTeleportWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.BoundSorcererTeleportCastingTime.get(); }
        @Override protected int getCastingInterval() { return 1; }
        @Nullable @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.SORCERER_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.WOLOLO; }
    }

    public class BoundConjureFlamesGoal extends BoundUseSpellGoal {
        @Override
        public boolean canUse() {
            LivingEntity target = BoundSorcerer.this.getTarget();
            if (target == null) return false;
            if (BoundSorcerer.this.isCastingSpell()) return false;
            if (BoundSorcerer.this.conjureFlamesCooldown > 0) return false;
            double range = GINServantConfig.BoundSorcererFlamesRange.get();
            return BoundSorcerer.this.distanceToSqr(target) < range * range && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            BoundSorcerer.this.setCasting(true);
            BoundSorcerer.this.setSpell(BoundSorcererSpell.FLAMES);
        }

        @Override
        public void stop() {
            super.stop();
            BoundSorcerer.this.setCasting(false);
            BoundSorcerer.this.setSpell(BoundSorcererSpell.NONE);
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = BoundSorcerer.this.getTarget();
            if (target == null) return;

            BoundSorcerer.this.conjureFlamesCooldown = GINServantConfig.BoundSorcererFlamesCooldown.get();

            LivingEntity owner = BoundSorcerer.this.getTrueOwner();
            UUID ownerUUID = owner != null ? owner.getUUID() : null;

            Level level = BoundSorcerer.this.level();
            BlockPos center = target.blockPosition();
            int radius = GINServantConfig.BoundSorcererFlamesRadius.get();
            float damage = GINServantConfig.BoundSorcererFlamesDamage.get().floatValue();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, 0, dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && !state.canBeReplaced()) continue;
                    level.setBlock(pos, GINMod.ALLY_MAGIC_FIRE.get().defaultBlockState(), 3);
                    if (level.getBlockEntity(pos) instanceof AllyMagicFireBlockEntity fireBE) {
                        fireBE.setOwner(ownerUUID);
                    }
                }
            }

            target.hurt(BoundSorcerer.this.damageSources().magic(), damage);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModRegistry.MAGIC_FLAME_PARTICLE_TYPE.get(),
                        target.getX(), target.getY() + 1, target.getZ(),
                        30, 0.3, 0.5, 0.3, 0.08);
            }
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.BoundSorcererFlamesWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.BoundSorcererFlamesCastingTime.get(); }
        @Override protected int getCastingInterval() { return 1; }
        @Nullable @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.SORCERER_CAST_SOUND_EVENT.get(); }
        @Override protected BoundSpell getSpell() { return BoundSpell.FANGS; }
    }
}