package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Ten seconds of aim-controlled sky fire, drilling a nine-block-wide angled molten channel. */
public class SigilOfCalamity extends AbstractSigilItem {
    public static final int DURATION = 200;
    private static final String TIMER = "calamityTicks";
    private static final String COLUMN = "calamityColumn";
    private static final String DEPTH = "calamityDepth";
    public SigilOfCalamity(Properties properties) { super(properties, Effects.DAMAGE_BOOST, 0); }
    @Override protected void applyConstructorPotionEffect(PlayerEntity player, float power) {}

    @Override protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        data.getSigilData().putInt(TIMER, DURATION);
        data.getSigilData().remove(COLUMN);
        data.getActiveSigils().add(this);
        world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS, 1F, 0.5F);
    }

    @Override public void onPlayerTick(PlayerEntity player) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null || !(player.level instanceof ServerWorld)) return;
        int remaining = data.getSigilData().getInt(TIMER) - 1;
        if (remaining <= 0) {
            com.gunmetalblack.guardianproject.common.CalamityBeamVisual.clear((ServerWorld) player.level, data.getSigilData());
            data.getSigilData().remove(TIMER);
            data.getSigilData().remove(COLUMN);
            data.getSigilData().remove(DEPTH);
            data.getActiveSigils().remove(this);
            return;
        }
        data.getSigilData().putInt(TIMER, remaining);
        if (remaining % 4 != 0) return;
        RayTraceResult hit = player.pick(128, 1, false);
        if (hit.getType() != RayTraceResult.Type.BLOCK) {
            com.gunmetalblack.guardianproject.common.CalamityBeamVisual.clear((ServerWorld) player.level, data.getSigilData());
            return;
        }
        ServerWorld world = (ServerWorld) player.level;
        BlockPos aimed = new BlockPos(hit.getLocation());
        BlockPos column = new BlockPos(aimed.getX(), 0, aimed.getZ());
        int top = Math.min(world.getMaxBuildHeight() - 1, aimed.getY());
        if (data.getSigilData().contains(COLUMN) && data.getSigilData().getLong(COLUMN) == column.asLong()) {
            top = Math.min(top, data.getSigilData().getInt(DEPTH));
        }
        int bottom = Math.max(0, top - 7);
        double cutX = aimed.getX() + 0.5 - (aimed.getY() - bottom) * 0.375;
        double cutZ = aimed.getZ() + 0.5;
        // Local impacts add rumble, knockback and small craters before melting.
        // Terrain changes go through our permission checks below, not a second unfiltered explosion.
        world.explode(player, cutX, top - 3, cutZ, 1.5F, false, net.minecraft.world.Explosion.Mode.NONE);
        int shallowestStop = -1;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            if (dx * dx + dz * dz > 16) continue;
            // Revisit the existing channel to vaporize lava before extending its cutting front.
            for (int y = aimed.getY(); y >= bottom; y--) {
                BlockPos pos = new BlockPos(aimed.getX() + 0.5 + dx - (aimed.getY() - y) * 0.375, y, aimed.getZ() + dz);
                if (!world.hasChunkAt(pos) || !world.getWorldBorder().isWithinBounds(pos)) break;
                BlockState state = world.getBlockState(pos);
                if (state.isAir()) continue;
                if (!canAlter(world, player, pos)) {
                    shallowestStop = Math.max(shallowestStop, y);
                    break;
                }
                if (state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA))
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                else if (!state.isAir() && state.getFluidState().isEmpty()) {
                    // Destructive melting deliberately converts blocks (including containers) without drops.
                    world.setBlockAndUpdate(pos, state.getMaterial().isSolid()
                            ? Blocks.LAVA.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        data.getSigilData().putLong(COLUMN, column.asLong());
        data.getSigilData().putInt(DEPTH, shallowestStop >= 0 ? shallowestStop : Math.max(0, bottom - 1));
        scorch(world, player, aimed);
        scorch(world, player, new BlockPos(cutX, bottom, cutZ));
        Vector3d center = new Vector3d(cutX, bottom, cutZ);
        int beamTop = world.getMaxBuildHeight() - 1;
        com.gunmetalblack.guardianproject.common.CalamityBeamVisual.update(world, data.getSigilData(), center.x, center.y, center.z);
        world.sendParticles(ParticleTypes.FLAME, center.x, top + 1, center.z, 35, 3, 1, 3, 0.1);
        world.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, top + 2, center.z, 15, 2, 1, 2, 0.08);
        world.sendParticles(ParticleTypes.LAVA, center.x, top + 0.5, center.z, 20, 1.5, 0.4, 1.5, 0);
        for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class,
                new AxisAlignedBB(center.x - 4.5, bottom, center.z - 4.5, center.x + 60, beamTop, center.z + 4.5))) {
            double alongX = center.x + (victim.getY() - bottom) * 0.375;
            if (Math.abs(victim.getX() - alongX) > 4.5 || victim.hasEffect(com.gunmetalblack.guardianproject.common.ModEffects.CALAMITY_BEAM.get())) continue;
            victim.setSecondsOnFire(15);
            // Explosion + fire flags let armor, Blast/Fire Protection and Resistance work normally.
            victim.hurt(DamageSource.explosion(player).setIsFire(), 80F);
        }
    }

    private boolean canAlter(ServerWorld world, PlayerEntity player, BlockPos pos) {
        if (!world.hasChunkAt(pos) || !world.getWorldBorder().isWithinBounds(pos)
                || !player.mayBuild() || !world.mayInteract(player, pos)) return false;
        BlockState state = world.getBlockState(pos);
        if (state.getDestroySpeed(world, pos) < 0) return false;
        // Fluids evaporate independently; highly blast-resistant solids stop the cutting front.
        if (state.getFluidState().isEmpty() && state.getBlock().getExplosionResistance() >= 100F) return false;
        net.minecraftforge.event.world.BlockEvent.BreakEvent event =
                new net.minecraftforge.event.world.BlockEvent.BreakEvent(world, pos.immutable(), state, player);
        return !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
    }

    private void ignite(ServerWorld world, PlayerEntity player, BlockPos pos) {
        if (world.isEmptyBlock(pos) && canAlter(world, player, pos)
                && Blocks.FIRE.defaultBlockState().canSurvive(world, pos)) {
            world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        }
    }

    private void scorch(ServerWorld world, PlayerEntity player, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 2, 7))) {
            if (!world.hasChunkAt(pos) || !world.getWorldBorder().isWithinBounds(pos)) continue;
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !canAlter(world, player, pos)) continue;
            if (state.getFluidState().is(FluidTags.WATER)) world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            else if (state.is(Blocks.GRASS_BLOCK)) {
                world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                ignite(world, player, pos.above());
            } else if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS)) {
                world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                ignite(world, player, pos);
            } else if (!state.isAir() && state.getMaterial().isSolid() && world.isEmptyBlock(pos.above())
                    && world.random.nextInt(8) == 0) {
                ignite(world, player, pos.above());
            }
        }
    }
}
