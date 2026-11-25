package com.gunmetalblack.guardianproject.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;

/** A short-lived vanilla tracked marker synchronizes the beam endpoint to nearby clients. */
public final class CalamityBeamVisual {
    private static final String KEY = "calamityBeamMarker";
    public static void update(ServerWorld world, CompoundNBT data, double x, double y, double z) {
        Entity marker = data.hasUUID(KEY) ? world.getEntity(data.getUUID(KEY)) : null;
        if (!(marker instanceof ArmorStandEntity) || !marker.isAlive()) {
            ArmorStandEntity stand = new ArmorStandEntity(world, x, y, z);
            stand.setInvisible(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            CompoundNBT tag = new CompoundNBT();
            stand.saveWithoutId(tag);
            tag.putBoolean("Marker", true);
            stand.load(tag);
            stand.addEffect(new EffectInstance(ModEffects.CALAMITY_BEAM.get(), 15, 0, false, false));
            world.addFreshEntity(stand);
            data.putUUID(KEY, stand.getUUID());
            marker = stand;
        }
        marker.setPos(x, y, z);
        marker.getPersistentData().putInt("guardianMeteorMarkerLife", 15);
        ((ArmorStandEntity) marker).addEffect(new EffectInstance(ModEffects.CALAMITY_BEAM.get(), 15, 0, false, false));
    }
    public static void clear(ServerWorld world, CompoundNBT data) {
        Entity marker = data.hasUUID(KEY) ? world.getEntity(data.getUUID(KEY)) : null;
        if (marker != null) marker.remove();
        data.remove(KEY);
    }
}
