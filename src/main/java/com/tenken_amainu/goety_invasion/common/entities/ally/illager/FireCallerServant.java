package com.tenken_amainu.goety_invasion.common.entities.ally.illager;

import com.Polarice3.Goety.common.entities.ai.AvoidTargetGoal;
import com.Polarice3.Goety.common.entities.ally.illager.SpellcasterIllagerServant;
import com.Polarice3.Goety.common.entities.ally.illager.raider.RaiderServant;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.MobUtil;
import com.tenken_amainu.goety_invasion.GINMod;
import com.tenken_amainu.goety_invasion.common.entities.ally.undead.bound.BoundFireCaller;
import com.tenken_amainu.goety_invasion.common.entities.projectiles.AllyFlyingMagma;
import com.tenken_amainu.goety_invasion.config.GINServantConfig;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireCallerServant extends SpellcasterIllagerServant {

    private int conjureSkullCooldown = 160;
    private int areaDamageCooldown = 300;

    public FireCallerServant(EntityType<? extends FireCallerServant> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FireCallerCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidTargetGoal<>(this, LivingEntity.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.addGoal(3, new FireCallerAreaDamageGoal());
        this.goalSelector.addGoal(4, new FireCallerConjureSkullGoal());

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, RaiderServant.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
    }

    @Override
    public void miscGoal() {
        this.goalSelector.addGoal(8, new RaiderWanderGoal<>(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, GINServantConfig.FireCallerServantFollowRange.get())
                .add(Attributes.ARMOR, GINServantConfig.FireCallerServantArmor.get())
                .add(Attributes.MAX_HEALTH, GINServantConfig.FireCallerServantHealth.get());
    }

    public void setConfigurableAttributes() {
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), GINServantConfig.FireCallerServantHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), GINServantConfig.FireCallerServantArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), GINServantConfig.FireCallerServantFollowRange.get());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.conjureSkullCooldown = compound.getInt("ConjureSkullCooldown");
        this.areaDamageCooldown = compound.getInt("AreaDamageCooldown");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ConjureSkullCooldown", this.conjureSkullCooldown);
        compound.putInt("AreaDamageCooldown", this.areaDamageCooldown);
    }

    @Override public int xpReward() { return 15; }
    @Override protected ResourceLocation getDefaultLootTable() { return null; }

    @Override protected SoundEvent getAmbientSound() { return ModRegistry.FIRECALLER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModRegistry.FIRECALLER_DEATH_SOUND_EVENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModRegistry.FIRECALLER_HURT_SOUND_EVENT.get(); }
    @Override public SoundEvent getCelebrateSound() { return ModRegistry.FIRECALLER_AMBIENT_SOUND_EVENT.get(); }
    @Override protected SoundEvent getCastingSoundEvent() { return ModRegistry.FIRECALLER_CAST_SOUND_EVENT.get(); }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide
                && this.getIdol() == null
                && this.getTrueOwner() != null
                && GINServantConfig.FireCallerNamelessConvert.get()
                && CuriosFinder.hasNamelessSet(this.getTrueOwner())) {
            BoundFireCaller servant = this.convertTo(GINMod.BOUND_FIRECALLER.get(), true);
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

    @Override
    public void tick() {
        super.tick();
        if (this.conjureSkullCooldown > 0) --this.conjureSkullCooldown;
        if (this.areaDamageCooldown > 0) --this.areaDamageCooldown;
    }

    @Override
    public boolean validLootToStore(ItemStack itemStack) {
        return super.validLootToStore(itemStack) && !itemStack.is(ModItems.OMINOUS_SADDLE.get());
    }

    class FireCallerCastingSpellGoal extends SpellcasterCastingSpellGoal {
        @Override
        public void tick() {
            if (FireCallerServant.this.getTarget() != null) {
                FireCallerServant.this.getLookControl().setLookAt(
                        FireCallerServant.this.getTarget(),
                        FireCallerServant.this.getMaxHeadYRot(),
                        FireCallerServant.this.getMaxHeadXRot());
            }
        }
    }

    public class FireCallerConjureSkullGoal extends SpellcasterUseSpellGoal {

        private List<LivingEntity> nearbyPlayersOrGolems() {
            return FireCallerServant.this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    FireCallerServant.this.getBoundingBox().inflate(5),
                    e -> (e instanceof Player || e instanceof IronGolem)
                            && !FireCallerServant.this.isFriendlyTo(e));
        }

        @Override
        public boolean canUse() {
            if (FireCallerServant.this.getTarget() == null) return false;
            if (FireCallerServant.this.isCastingSpell()) return false;
            if (FireCallerServant.this.conjureSkullCooldown > 0) return false;
            return this.nearbyPlayersOrGolems().isEmpty();
        }

        @Override
        public void tick() {
            if (FireCallerServant.this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        FireCallerServant.this.getX(),
                        FireCallerServant.this.getY() + 2.5,
                        FireCallerServant.this.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        FireCallerServant.this.getX(),
                        FireCallerServant.this.getY() + 2.5,
                        FireCallerServant.this.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.05D);
            }
            super.tick();
        }

        private void shootSkullAt(LivingEntity target) {
            this.shootSkullAt(
                    target.getX(),
                    target.getY() + (double) target.getEyeHeight() * 0.5,
                    target.getZ());
        }

        private void shootSkullAt(double targetX, double targetY, double targetZ) {
            double d = FireCallerServant.this.getX();
            double e = FireCallerServant.this.getY() + 2.5;
            double f = FireCallerServant.this.getZ();
            double g = targetX - d;
            double h = targetY - e;
            double i = targetZ - f;

            AllyFlyingMagma magma = new AllyFlyingMagma(
                    FireCallerServant.this.level(),
                    FireCallerServant.this,
                    g, h, i);
            magma.setOwner(FireCallerServant.this);
            magma.setPosRaw(d, e, f);
            FireCallerServant.this.level().addFreshEntity(magma);
        }

        @Override
        protected void performSpellCasting() {
            if (FireCallerServant.this.getTarget() != null) {
                this.shootSkullAt(FireCallerServant.this.getTarget());
            }
            if (FireCallerServant.this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        FireCallerServant.this.getX(),
                        FireCallerServant.this.getY() + 2.5,
                        FireCallerServant.this.getZ(),
                        40, 0.4D, 0.4D, 0.4D, 0.15D);
            }
            FireCallerServant.this.conjureSkullCooldown = 160;
        }

        @Override protected int getCastWarmupTime() { return 60; }
        @Override protected int getCastingTime() { return 60; }
        @Override protected int getCastingInterval() { return 400; }
        @Override protected SoundEvent getSpellPrepareSound() { return SoundEvents.EVOKER_PREPARE_ATTACK; }
        @Override protected IllagerServantSpell getSpell() { return IllagerServantSpell.FANGS; }
    }

    public class FireCallerAreaDamageGoal extends SpellcasterUseSpellGoal {

        @Override
        public boolean canUse() {
            if (FireCallerServant.this.getTarget() == null) return false;
            if (FireCallerServant.this.isCastingSpell()) return false;
            return FireCallerServant.this.areaDamageCooldown <= 0;
        }

        private List<LivingEntity> getTargets() {
            return FireCallerServant.this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    FireCallerServant.this.getBoundingBox().inflate(6),
                    e -> !(e instanceof AbstractIllager)
                            && !(e instanceof Ravager)
                            && !FireCallerServant.this.isFriendlyTo(e));
        }

        private void buff(LivingEntity entity) {
            if (FireCallerServant.this.isFriendlyTo(entity)) return;

            entity.push(0.0f, 1.2f, 0.0f);
            entity.hurt(FireCallerServant.this.damageSources().magic(), 6.0f);
            entity.setRemainingFireTicks(120);

            if (FireCallerServant.this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        entity.getX(), entity.getY() + 2, entity.getZ(),
                        10, 0.2D, 0.2D, 0.2D, 0.015D);
            }
        }

        @Override
        protected void performSpellCasting() {
            this.getTargets().forEach(this::buff);
            FireCallerServant.this.areaDamageCooldown = 300;
        }

        @Override protected int getCastWarmupTime() { return 50; }
        @Override protected int getCastingTime() { return 50; }
        @Override protected int getCastingInterval() { return 400; }
        @Override protected SoundEvent getSpellPrepareSound() { return SoundEvents.EVOKER_PREPARE_ATTACK; }
        @Override protected IllagerServantSpell getSpell() { return IllagerServantSpell.WOLOLO; }
    }

    public boolean isFriendlyTo(LivingEntity target) {
        if (target == this || target == this.getTrueOwner()) return true;
        if (this.isAlliedTo(target) || target.isAlliedTo(this)) return true;
        return MobUtil.areAllies(this, target);
    }
}