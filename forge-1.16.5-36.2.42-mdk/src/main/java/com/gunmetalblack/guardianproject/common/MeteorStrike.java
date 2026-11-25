package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Server-ticked warning and launch sequence shared by targeted meteor spells. */
@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID)
public final class MeteorStrike {
    private static final List<Strike> STRIKES = new ArrayList<>();
    public static void begin(ServerPlayerEntity caster, Vector3d target) {
        ServerWorld world = caster.getLevel();
        ArmorStandEntity marker = new ArmorStandEntity(world, target.x, target.y, target.z);
        marker.setInvisible(true);
        marker.setNoGravity(true);
        marker.setInvulnerable(true);
        net.minecraft.nbt.CompoundNBT tag = new net.minecraft.nbt.CompoundNBT();
        marker.saveWithoutId(tag);
        tag.putBoolean("Marker", true);
        marker.load(tag);
        marker.getPersistentData().putInt("guardianMeteorMarkerLife", 125);
        marker.addEffect(new EffectInstance(ModEffects.TRAGEDY_TARGET.get(), 200, 0, false, false));
        world.addFreshEntity(marker);
        STRIKES.add(new Strike(caster, world, target, marker));
    }
    @SubscribeEvent public static void markerTick(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
        net.minecraft.entity.LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide || !entity.getPersistentData().contains("guardianMeteorMarkerLife")) return;
        int remaining = entity.getPersistentData().getInt("guardianMeteorMarkerLife") - 1;
        if (remaining <= 0) entity.remove();
        else entity.getPersistentData().putInt("guardianMeteorMarkerLife", remaining);
    }
    @SubscribeEvent public static void stopped(net.minecraftforge.fml.event.server.FMLServerStoppedEvent event) {
        STRIKES.clear();
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<Strike> iterator = STRIKES.iterator();
        while (iterator.hasNext()) {
            Strike strike = iterator.next();
            if (strike.caster.getServer() == null || strike.caster.getServer().getLevel(strike.world.dimension()) != strike.world) {
                strike.marker.remove(); iterator.remove(); continue;
            }
            if (strike.age % 4 == 0) {
                for (int y = 0; y <= 40; y++) strike.world.sendParticles(
                        new RedstoneParticleData(1F, 0.3F, 0.02F, 1.4F),
                        strike.target.x, strike.target.y + y, strike.target.z, 1, 0, 0, 0, 0);
            }
            if (++strike.age == 60) {
                Vector3d start = strike.target.add(24, 60, 0);
                if (strike.world.hasChunkAt(new net.minecraft.util.math.BlockPos(start))) {
                    MeteorEntity meteor = new MeteorEntity(ModEntities.METEOR.get(), strike.world);
                    meteor.setOwner(strike.caster);
                    meteor.setPos(start.x, start.y, start.z);
                    Vector3d velocity = strike.target.subtract(start).normalize().scale(1.8);
                    meteor.setDeltaMovement(velocity);
                    meteor.xPower = velocity.x * 0.04;
                    meteor.yPower = velocity.y * 0.04;
                    meteor.zPower = velocity.z * 0.04;
                    strike.world.addFreshEntity(meteor);
                }
            }
            if (strike.age >= 120) { strike.marker.remove(); iterator.remove(); }
        }
    }
    private static class Strike {
        final ServerPlayerEntity caster; final ServerWorld world; final Vector3d target; final ArmorStandEntity marker;
        int age;
        Strike(ServerPlayerEntity caster, ServerWorld world, Vector3d target, ArmorStandEntity marker) {
            this.caster = caster; this.world = world; this.target = target; this.marker = marker;
        }
    }
}
