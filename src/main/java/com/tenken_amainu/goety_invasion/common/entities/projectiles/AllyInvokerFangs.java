package com.tenken_amainu.goety_invasion.common.entities.projectiles;

import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AllyInvokerFangs extends Entity {
    private int warmup;
    private boolean startedAttack;
    private int ticksLeft;
    private boolean playingAnimation;
    private @Nullable LivingEntity owner;
    private @Nullable UUID ownerUuid;
    private float damage = 10.0F;

    public AllyInvokerFangs(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.ticksLeft = 22;
    }

    public AllyInvokerFangs(Level level, double x, double y, double z, float yaw, int warmup, LivingEntity owner, float damage) {
        this(ModRegistry.INVOKER_FANGS_ENTITY_TYPE.get(), level);
        this.warmup = warmup;
        this.damage = damage;
        this.setOwner(owner);
        this.setYRot(yaw * (180F / (float) Math.PI));
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {}

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUuid != null && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(this.ownerUuid) instanceof LivingEntity living) {
                this.owner = living;
            }
        }
        return this.owner;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUuid = owner == null ? null : owner.getUUID();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.warmup = nbt.getInt("Warmup");
        if (nbt.hasUUID("Owner")) {
            this.ownerUuid = nbt.getUUID("Owner");
        }
        if (nbt.contains("CustomDamage")) {
            this.damage = nbt.getFloat("CustomDamage");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Warmup", this.warmup);
        if (this.ownerUuid != null) {
            nbt.putUUID("Owner", this.ownerUuid);
        }
        nbt.putFloat("CustomDamage", this.damage);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (!this.playingAnimation) return;
            --this.ticksLeft;
            if (this.ticksLeft == 14) {
                for (int i = 0; i < 12; ++i) {
                    double d = this.getX() + (this.random.nextDouble() * 2.0F - 1.0F) * this.getBbWidth() * 0.5;
                    double e = this.getY() + 0.05 + this.random.nextDouble();
                    double f = this.getZ() + (this.random.nextDouble() * 2.0F - 1.0F) * this.getBbWidth() * 0.5;
                    double g = (this.random.nextDouble() * 2.0F - 1.0F) * 0.3;
                    double h = 0.3 + this.random.nextDouble() * 0.3;
                    double j = (this.random.nextDouble() * 2.0F - 1.0F) * 0.3;
                    this.level().addParticle(ParticleTypes.CRIT, d, e + 1.0, f, g, h, j);
                }
            }
            return;
        }

        if (--this.warmup >= 0) return;

        if (this.warmup == -8) {
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(0.2, 0.0, 0.2))) {
                this.damage(target);
            }
        }
        if (!this.startedAttack) {
            this.level().broadcastEntityEvent(this, (byte) 4);
            this.startedAttack = true;
        }
        if (--this.ticksLeft < 0) {
            this.discard();
        }
    }

    private void damage(LivingEntity target) {
        LivingEntity owner = this.getOwner();
        if (target == owner) return;
        if (owner != null && owner.isAlliedTo(target)) return;
        if (!target.isAlive() || target.isInvulnerable()) return;

        if (owner == null) {
            target.hurt(this.damageSources().magic(), this.damage);
            target.push(0.0, 1.7, 0.0);
        } else {
            target.hurt(this.damageSources().indirectMagic(this, owner), this.damage);
            target.push(0.0, 0.6, 0.0);
        }

        if (this.isOnFire()) {
            target.setSecondsOnFire(5);
        }
    }

    @Override
    public void handleEntityEvent(byte status) {
        super.handleEntityEvent(status);
        if (status == 4) {
            this.playingAnimation = true;
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        ModRegistry.INVOKER_FANGS_SOUND_EVENT.get(), this.getSoundSource(),
                        1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
            }
        }
    }

    public float getAnimationProgress(float tickDelta) {
        if (!this.playingAnimation) return 0.0F;
        int i = this.ticksLeft - 2;
        return i <= 0 ? 1.0F : 1.0F - ((float) i - tickDelta) / 20.0F;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}