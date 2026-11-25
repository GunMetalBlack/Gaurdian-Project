package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.item.ModItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID)
public final class MartyrdomJump {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GuardianProjectMod.MOD_ID, "martyrdom_jump"), () -> "1", "1"::equals, "1"::equals);
    public static void register() {
        CHANNEL.registerMessage(0, KeyState.class, (msg, buf) -> buf.writeBoolean(msg.held),
                buf -> new KeyState(buf.readBoolean()), MartyrdomJump::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
    public static final class KeyState {
        final boolean held;
        public KeyState(boolean held) { this.held = held; }
    }
    private static void handle(KeyState message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player == null) return;
            com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
            if (data == null) return;
            net.minecraft.nbt.CompoundNBT state = data.getSigilData();
            if (!data.getActiveSigils().contains(ModItems.MARTYRDOM_SIGIL.get())) { state.remove("jumpChargeStart"); return; }
            if (!player.isAlive()) { state.remove("jumpChargeStart"); return; }
            if (message.held) {
                // Martyrdom allows charging on the ground or in midair.
                if (!state.contains("jumpChargeStart")) state.putInt("jumpChargeStart", player.tickCount);
            } else if (state.contains("jumpChargeStart")) {
                int held = Math.max(0, player.tickCount - state.getInt("jumpChargeStart"));
                state.remove("jumpChargeStart");
                double charge = Math.min(held / 40.0, 1.0);
                Vector3d facing = player.getLookAngle().multiply(1, 0, 1).normalize();
                player.setDeltaMovement(facing.scale(0.3 + charge * 2.2).add(0, 0.65 + charge * 1.85, 0));
                player.hasImpulse = true;
                player.hurtMarked = true;
                player.fallDistance = 0;
                player.getPersistentData().putBoolean("guardianMartyrdomLanding", true);
                player.connection.send(new SEntityVelocityPacket(player));
            }
        });
        ctx.setPacketHandled(true);
    }
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof net.minecraft.entity.player.PlayerEntity)) return;
        net.minecraft.entity.player.PlayerEntity player = (net.minecraft.entity.player.PlayerEntity) event.getEntityLiving();
        boolean protectedLanding = player.getPersistentData().getBoolean("guardianMartyrdomLanding");
        if (protectedLanding) player.getPersistentData().remove("guardianMartyrdomLanding");
        if (protectedLanding || player.hasEffect(ModEffects.MARTYRDOM.get())) {
            event.setCanceled(true);
            player.fallDistance = 0;
        }
    }
}
