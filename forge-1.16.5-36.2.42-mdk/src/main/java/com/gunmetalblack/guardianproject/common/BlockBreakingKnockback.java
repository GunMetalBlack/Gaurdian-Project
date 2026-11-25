package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/** Server-controlled launches. Distances and speeds use blocks and ticks. */
@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID)
public final class BlockBreakingKnockback {
    private static final Map<UUID, Launch> LAUNCHES = new HashMap<>();

    private BlockBreakingKnockback() {}

    public static void launch(LivingEntity target, LivingEntity attacker, float speed) {
        // Follow the attacker's aim, including pitch, for upward and downward punches.
        Vector3d direction = attacker.getLookAngle();
        launch(target, direction, speed, 20);
    }

    /** Launch any living target along a direction for at most duration ticks. */
    public static void launch(LivingEntity target, Vector3d direction, float speed, int duration) {
        if (target.level.isClientSide || direction.lengthSqr() < 0.001 || speed <= 0 || duration <= 0) return;
        Vector3d velocity = direction.normalize().scale(Math.min(speed, 4));
        // Keep the small ground-clearing lift for horizontal attacks only.
        if (Math.abs(direction.normalize().y) < 0.05) velocity = velocity.add(0, 0.2, 0);
        LAUNCHES.put(target.getUUID(), new Launch(target, velocity, Math.min(duration, 100)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Iterator<Launch> iterator = LAUNCHES.values().iterator();
        while (iterator.hasNext()) {
            Launch launch = iterator.next();
            LivingEntity target = launch.target;
            if (!target.isAlive() || launch.ticks-- <= 0) { iterator.remove(); continue; }
            boolean blocked = false;
            // Sample the swept body so fast launches cannot skip thin walls.
            int steps = Math.max(1, (int) Math.ceil(launch.velocity.length() / 0.25));
            for (int step = 0; step <= steps && !blocked; step++) {
                AxisAlignedBB box = target.getBoundingBox().move(launch.velocity.scale((double) step / steps)).deflate(0.01);
                for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
                    if (!target.level.hasChunkAt(pos)) { blocked = true; break; }
                    BlockState state = target.level.getBlockState(pos);
                    if (state.getCollisionShape(target.level, pos).isEmpty()) continue;
                    if (state.getDestroySpeed(target.level, pos) < 0 || target.level.getBlockEntity(pos) != null
                            || !target.level.destroyBlock(pos, false, target)) { blocked = true; break; }
                }
            }
            if (blocked) { target.setDeltaMovement(Vector3d.ZERO); iterator.remove(); }
            else { target.setDeltaMovement(launch.velocity); launch.velocity = launch.velocity.scale(0.85); }
            target.hurtMarked = true;
            if (target instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) target).connection.send(new SEntityVelocityPacket(target));
            }
        }
    }

    private static final class Launch {
        private final LivingEntity target;
        private Vector3d velocity;
        private int ticks;
        private Launch(LivingEntity target, Vector3d velocity, int ticks) {
            this.target = target; this.velocity = velocity; this.ticks = ticks;
        }
    }
}
