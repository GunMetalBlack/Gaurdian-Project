package com.gunmetalblack.guardianproject.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Reusable spherical terrain blast with vanilla damage, drops and network synchronization. */
public final class FuzzyExplosion {
    private FuzzyExplosion() {}

    /**
     * Geometry adapted from ICBMBlastEventUtil#getBlockPositionsWithinFuzzySphere:
     * https://github.com/jdawg3636/ICBM/blob/master/src/main/java/com/jdawg3636/icbm/common/event/ICBMBlastEventUtil.java
     * Its original attribution is songxia23's Guns, Rockets and Atomic Explosions (MIT).
     * Boundary probability 0 omits boundary samples; 1 includes every sample.
     */
    public static List<BlockPos> getBlockPositionsWithinFuzzySphere(double x, double y, double z,
            double radius, Random random, double boundaryProbability) {
        if (!Double.isFinite(radius) || radius <= 0 || radius > 80) throw new IllegalArgumentException("Radius must be in (0, 80]");
        if (!(boundaryProbability >= 0 && boundaryProbability <= 1)) throw new IllegalArgumentException("Probability must be in [0, 1]");
        List<BlockPos> positions = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            int yLimit = (int) Math.sqrt(r * r - dx * dx);
            for (int dy = -yLimit; dy <= yLimit; dy++) {
                int zLimit = (int) Math.sqrt(r * r - dx * dx - dy * dy);
                for (int dz = -zLimit; dz <= zLimit; dz++) {
                    boolean boundary = Math.abs(dx) == r || Math.abs(dy) == yLimit || Math.abs(dz) == zLimit;
                    if (!boundary || random.nextFloat() < boundaryProbability) positions.add(new BlockPos(x + dx, y + dy, z + dz));
                }
            }
        }
        return positions;
    }

    public static void detonate(ServerWorld world, Entity source, double x, double y, double z,
            float power, double radius, double boundaryProbability) {
        List<BlockPos> positions = getBlockPositionsWithinFuzzySphere(x, y, z, radius, world.random, boundaryProbability);
        positions.removeIf(pos -> pos.getY() < 0 || pos.getY() >= world.getMaxBuildHeight()
                || !world.hasChunkAt(pos) || !world.getWorldBorder().isWithinBounds(pos)
                || world.getBlockState(pos).isAir() || world.getBlockState(pos).getDestroySpeed(world, pos) < 0);
        Explosion explosion = new Explosion(world, source, null, null, x, y, z, power, false, Explosion.Mode.DESTROY) {
            @Override public void explode() {
                // Populate before super: Forge's detonation event can inspect/filter our terrain list.
                getToBlow().addAll(positions);
                super.explode();
                // super adds ray-traced terrain too; keep only the requested fuzzy geometry.
                getToBlow().retainAll(new java.util.HashSet<>(positions));
                java.util.Set<BlockPos> unique = new java.util.LinkedHashSet<>(getToBlow());
                getToBlow().clear();
                getToBlow().addAll(unique);
            }
        };
        if (ForgeEventFactory.onExplosionStart(world, explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(true);
        for (ServerPlayerEntity player : world.players()) {
            if (player.distanceToSqr(x, y, z) < 4096) player.connection.send(new SExplosionPacket(
                    x, y, z, power, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
        }
    }
}
