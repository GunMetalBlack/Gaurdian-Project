package com.gunmetalblack.guardianproject.common;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.network.IPacket;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.particles.ParticleTypes;

public class MeteorEntity extends FireballEntity {
    public MeteorEntity(EntityType<? extends MeteorEntity> type, World world) { super(type, world); }
    @Override protected void onHit(RayTraceResult hit) {
        if (!level.isClientSide && !removed) {
            FuzzyExplosion.detonate((ServerWorld) level, getOwner(), getX(), getY(), getZ(), 24F, 24, 0.5);
            remove();
        }
    }
    @Override public boolean hurt(net.minecraft.util.DamageSource source, float amount) { return false; }
    @Override public void tick() {
        super.tick();
        if (!level.isClientSide && !removed) {
            ((ServerWorld) level).sendParticles(ParticleTypes.FLAME, getX(), getY() + 1, getZ(), 25, 1.6, 1.6, 1.6, 0.08);
            ((ServerWorld) level).sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2, getZ(), 12, 1.7, 1.7, 1.7, 0.05);
            if (tickCount > 200) remove();
        }
    }
    @Override public IPacket<?> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
