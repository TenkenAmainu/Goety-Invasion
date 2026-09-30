package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.common.entities.ai.AvoidTargetGoal;
import com.Polarice3.Goety.common.entities.ally.illager.SpellcasterIllagerServant;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.blockentity.AllyMagicFireBlockEntity;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundSorcerer;
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
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.UUID;

public class SorcererServant extends SpellcasterIllagerServant {

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, GINServantConfig.SorcererServantMovementSpeed.get())
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.SorcererServantFollowRange.get())
                .add(Attributes.ARMOR, GINServantConfig.SorcererServantArmor.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.SorcererServantHealth.get());
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide
                && this.getIdol() == null
                && this.getTrueOwner() != null
                && GINServantConfig.SorcererNamelessConvert.get()
                && CuriosFinder.hasNamelessSet(this.getTrueOwner())) {
            BoundSorcerer servant = this.convertTo(GINMod.BOUND_SORCERER.get(), true);
            if (servant != null) {
                servant.setTrueOwner(this.getTrueOwner());
                ForgeEventFactory.onLivingConvert(this, servant);
                if (!this.isSilent()) {
                    this.level().levelEvent((Player) null, 1026, this.blockPosition(), 0);
                }
            }
        }
        super.die(cause);
    }

    public enum SorcererSpell {
        NONE(0, 0, 0, 0),
        TELEPORT(1, 0.7, 0.0, 0.7),
        FLAMES(2, 1.0, 0.5, 0.0);

        final int id;
        final double r, g, b;

        SorcererSpell(int id, double r, double g, double b) {
            this.id = id;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        static SorcererSpell byId(int id) {
            SorcererSpell[] values = values();
            return id >= 0 && id < values.length ? values[id] : NONE;
        }
    }

    private static final EntityDataAccessor<Byte> DATA_SORCERER_SPELL =
            SynchedEntityData.defineId(SorcererServant.class, EntityDataSerializers.BYTE);

    private SorcererSpell currentSorcererSpell = SorcererSpell.NONE;
    private int castTeleportCooldown;
    private int conjureFlamesCooldown;

    public SorcererServant(EntityType<? extends SorcererServant> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SORCERER_SPELL, (byte) 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.castTeleportCooldown = tag.getInt("CastTeleportCooldown");
        this.conjureFlamesCooldown = tag.getInt("ConjureFlamesCooldown");
        this.currentSorcererSpell = SorcererSpell.byId(tag.getInt("SorcererSpell"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CastTeleportCooldown", this.castTeleportCooldown);
        tag.putInt("ConjureFlamesCooldown", this.conjureFlamesCooldown);
        tag.putInt("SorcererSpell", this.currentSorcererSpell.id);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (castTeleportCooldown > 0) castTeleportCooldown--;
        if (conjureFlamesCooldown > 0) conjureFlamesCooldown--;
    }

    private void setSorcererSpell(SorcererSpell spell) {
        this.currentSorcererSpell = spell;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_SORCERER_SPELL, (byte) spell.id);
        }
    }

    private SorcererSpell getSorcererSpell() {
        if (this.level().isClientSide) {
            return SorcererSpell.byId(this.entityData.get(DATA_SORCERER_SPELL));
        }
        return this.currentSorcererSpell;
    }

    @Override
    public void spellParticles() {
        if (this.level().isClientSide && this.isCastingSpell()) {
            SorcererSpell spell = getSorcererSpell();
            if (spell != SorcererSpell.NONE) {
                float f = this.yBodyRot * Mth.DEG_TO_RAD + Mth.cos((float) this.tickCount * 0.6662F) * 0.25F;
                float f1 = Mth.cos(f);
                float f2 = Mth.sin(f);
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                        this.getX() + f1 * 0.6, this.getY() + 1.8, this.getZ() + f2 * 0.6,
                        spell.r, spell.g, spell.b);
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                        this.getX() - f1 * 0.6, this.getY() + 1.8, this.getZ() - f2 * 0.6,
                        spell.r, spell.g, spell.b);
                return;
            }
        }
        super.spellParticles();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidTargetGoal<>(this, LivingEntity.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.addGoal(4, new CastTeleportGoal());
        this.goalSelector.addGoal(5, new ConjureFlamesGoal());
        this.goalSelector.addGoal(8, new RaiderWanderGoal<>(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    public IllagerServantArmPose getArmPose() {
        return isCastingSpell() ? IllagerServantArmPose.SPELLCASTING : IllagerServantArmPose.CROSSED;
    }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.SORCERER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.SORCERER_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModRegistry.SORCERER_HURT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getCastingSoundEvent() { return ModRegistry.SORCERER_COMPLETE_CAST_SOUND_EVENT.get(); }
    @Override public SoundEvent getCelebrateSound() { return ModRegistry.SORCERER_CELEBRATE_SOUND_EVENT.get(); }

    class CastingSpellGoal extends SpellcasterCastingSpellGoal {
        @Override
        public void tick() {
            super.tick();
            if (SorcererServant.this.getTarget() != null) {
                SorcererServant.this.getLookControl().setLookAt(
                        SorcererServant.this.getTarget(),
                        SorcererServant.this.getMaxHeadYRot(),
                        SorcererServant.this.getMaxHeadXRot());
            }
        }

        @Override
        public void stop() {
            super.stop();
            SorcererServant.this.setSorcererSpell(SorcererSpell.NONE);
        }
    }

    class CastTeleportGoal extends SpellcasterUseSpellGoal {
        @Override
        public boolean canUse() {
            LivingEntity target = SorcererServant.this.getTarget();
            if (target == null) return false;
            if (SorcererServant.this.isCastingSpell()) return false;
            if (SorcererServant.this.castTeleportCooldown > 0) return false;
            double range = GINServantConfig.SorcererTeleportRange.get();
            return SorcererServant.this.distanceToSqr(target) < range * range && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            SorcererServant.this.setSorcererSpell(SorcererSpell.TELEPORT);
        }

        @Override
        protected void performSpellCasting() {
            SorcererServant.this.castTeleportCooldown = GINServantConfig.SorcererTeleportCooldown.get();
            double x = SorcererServant.this.getX();
            double y = SorcererServant.this.getY() + 1;
            double z = SorcererServant.this.getZ();
            if (SorcererServant.this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, x, y, z, 30, 0.3, 0.5, 0.3, 0.015);
            }
            TeleportUtil.tryRandomTeleport(SorcererServant.this);
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.SorcererTeleportWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.SorcererTeleportCastingTime.get(); }
        @Override protected int getCastingInterval() { return 1; }
        @Nullable @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.SORCERER_CAST_SOUND_EVENT.get(); }
        @Override protected IllagerServantSpell getSpell() { return IllagerServantSpell.DISAPPEAR; }
    }

    class ConjureFlamesGoal extends SpellcasterUseSpellGoal {
        @Override
        public boolean canUse() {
            LivingEntity target = SorcererServant.this.getTarget();
            if (target == null) return false;
            if (SorcererServant.this.isCastingSpell()) return false;
            if (SorcererServant.this.conjureFlamesCooldown > 0) return false;
            double range = GINServantConfig.SorcererFlamesRange.get();
            return SorcererServant.this.distanceToSqr(target) < range * range && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            SorcererServant.this.setSorcererSpell(SorcererSpell.FLAMES);
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = SorcererServant.this.getTarget();
            if (target == null) return;

            SorcererServant.this.conjureFlamesCooldown = GINServantConfig.SorcererFlamesCooldown.get();

            LivingEntity owner = SorcererServant.this.getTrueOwner();
            UUID ownerUUID = owner != null ? owner.getUUID() : null;

            Level level = SorcererServant.this.level();
            BlockPos center = target.blockPosition();
            int radius = GINServantConfig.SorcererFlamesRadius.get();
            float damage = GINServantConfig.SorcererFlamesDamage.get().floatValue();

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

            target.hurt(SorcererServant.this.damageSources().magic(), damage);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ModRegistry.MAGIC_FLAME_PARTICLE_TYPE.get(),
                        target.getX(), target.getY() + 1, target.getZ(),
                        30, 0.3, 0.5, 0.3, 0.08);
            }
        }

        @Override protected int getCastWarmupTime() { return GINServantConfig.SorcererFlamesWarmup.get(); }
        @Override protected int getCastingTime() { return GINServantConfig.SorcererFlamesCastingTime.get(); }
        @Override protected int getCastingInterval() { return 1; }
        @Nullable @Override protected SoundEvent getSpellPrepareSound() { return ModRegistry.SORCERER_CAST_SOUND_EVENT.get(); }
        @Override protected IllagerServantSpell getSpell() { return IllagerServantSpell.DISAPPEAR; }
    }
}