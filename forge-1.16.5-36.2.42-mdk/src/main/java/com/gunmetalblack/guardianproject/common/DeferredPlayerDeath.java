package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.LinkedHashSet;
import java.util.Set;

/** Apply spell suicide costs outside the world's per-player tick traversal. */
@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID)
public final class DeferredPlayerDeath {
    private static final Set<ServerPlayerEntity> PENDING = new LinkedHashSet<>();
    private DeferredPlayerDeath() {}

    public static void schedule(ServerPlayerEntity player) { PENDING.add(player); }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Set<ServerPlayerEntity> pending = new LinkedHashSet<>(PENDING);
        PENDING.clear();
        for (ServerPlayerEntity player : pending) {
            if (!player.isAlive() || player.getServer() == null
                    || player.getServer().getPlayerList().getPlayer(player.getUUID()) != player) continue;
            player.setDeltaMovement(Vector3d.ZERO);
            player.fallDistance = 0;
            player.connection.send(new SEntityVelocityPacket(player));
            // Unlike velocity packets, teleport starts vanilla's position-confirmation barrier.
            // Movement queued during the large blast is ignored until the client acknowledges it.
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
            player.kill();
        }
    }
}
