package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Source-preserving debt ledger, with NBT snapshots as a restart/logout fallback. */
@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID)
public final class EywaDamageLedger {
    public static final String DEBT = "eywaDebt", REPAY = "eywaRepay";
    private static final Map<UUID, Map<String, DamageSource>> LIVE_SOURCES = new HashMap<>();
    private static boolean reflecting;
    private static UUID repaying;
    public static boolean isInternalDamage() { return reflecting || repaying != null; }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void protect(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity) || event.getAmount() <= 0) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
        if (player.getUUID().equals(repaying)) return;
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null || !data.getActiveSigils().contains(ModItems.DIVINE_EYWA_SIGIL.get())) return;
        event.setCanceled(true);
        CompoundNBT hit = snapshot(event.getSource(), event.getAmount());
        String id = UUID.randomUUID().toString();
        hit.putString("id", id);
        LIVE_SOURCES.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>()).put(id, event.getSource());
        ListNBT debt = data.getSigilData().getList(DEBT, 10);
        debt.add(hit);
        data.getSigilData().put(DEBT, debt);
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) attacker = event.getSource().getDirectEntity();
        // Reflected hits can be stored by another Eywa user, but never bounce indefinitely.
        if (!reflecting && attacker != null && attacker != player && attacker.isAlive()) {
            reflecting = true;
            try {
                if (attacker instanceof LivingEntity) ((LivingEntity) attacker).invulnerableTime = 0;
                attacker.hurt(DamageSource.thorns(player), event.getAmount());
            } finally { reflecting = false; }
        }
    }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || ServerLifecycleHooks.getCurrentServer() == null) return;
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
            if (data == null || !data.getSigilData().getBoolean(REPAY)) continue;
            ListNBT debt = data.getSigilData().getList(DEBT, 10);
            data.getSigilData().remove(REPAY);
            data.getSigilData().remove(DEBT);
            Map<String, DamageSource> originals = LIVE_SOURCES.remove(player.getUUID());
            repaying = player.getUUID();
            try {
                // Synchronize position before a possibly lethal debt payment after player ticking.
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
                for (int i = 0; i < debt.size() && player.isAlive(); i++) {
                    CompoundNBT hit = debt.getCompound(i);
                    DamageSource source = originals == null ? null : originals.get(hit.getString("id"));
                    if (source == null) source = new RememberedSource(hit, player);
                    player.invulnerableTime = 0;
                    player.hurt(source, hit.getFloat("amount"));
                }
            } finally { repaying = null; }
        }
    }

    private static CompoundNBT snapshot(DamageSource source, float amount) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("type", source.getMsgId()); tag.putFloat("amount", amount);
        tag.putBoolean("armor", source.isBypassArmor()); tag.putBoolean("invul", source.isBypassInvul());
        tag.putBoolean("magicBypass", source.isBypassMagic()); tag.putBoolean("fire", source.isFire());
        tag.putBoolean("projectile", source.isProjectile()); tag.putBoolean("explosion", source.isExplosion());
        tag.putBoolean("magic", source.isMagic()); tag.putBoolean("difficulty", source.scalesWithDifficulty());
        tag.putFloat("exhaustion", source.getFoodExhaustion());
        if (source.getEntity() != null) {
            tag.putUUID("attacker", source.getEntity().getUUID());
            tag.putString("attackerName", ITextComponent.Serializer.toJson(source.getEntity().getDisplayName()));
        }
        if (source.getDirectEntity() != null) tag.putUUID("direct", source.getDirectEntity().getUUID());
        Vector3d position = source.getSourcePosition();
        if (position != null) { tag.putBoolean("position", true); tag.putDouble("x", position.x); tag.putDouble("y", position.y); tag.putDouble("z", position.z); }
        return tag;
    }

    private static class RememberedSource extends DamageSource {
        final CompoundNBT tag;
        final ServerPlayerEntity victim;
        RememberedSource(CompoundNBT tag, ServerPlayerEntity victim) {
            super(tag.getString("type")); this.tag = tag; this.victim = victim;
            if (tag.getBoolean("armor")) bypassArmor(); if (tag.getBoolean("invul")) bypassInvul();
            if (tag.getBoolean("magicBypass")) bypassMagic(); if (tag.getBoolean("fire")) setIsFire();
            if (tag.getBoolean("projectile")) setProjectile(); if (tag.getBoolean("explosion")) setExplosion();
            if (tag.getBoolean("magic")) setMagic(); if (tag.getBoolean("difficulty")) setScalesWithDifficulty();
        }
        private Entity resolve(String key) {
            if (!tag.hasUUID(key)) return null;
            UUID id = tag.getUUID(key);
            for (ServerWorld world : victim.getServer().getAllLevels()) {
                Entity entity = world.getEntity(id); if (entity != null) return entity;
            }
            return null;
        }
        @Override public Entity getEntity() { return resolve("attacker"); }
        @Override public Entity getDirectEntity() { return resolve("direct"); }
        @Override public float getFoodExhaustion() { return tag.getFloat("exhaustion"); }
        @Override public Vector3d getSourcePosition() { return tag.getBoolean("position") ? new Vector3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")) : null; }
        @Override public ITextComponent getLocalizedDeathMessage(LivingEntity entity) {
            if (tag.contains("attackerName")) return new TranslationTextComponent("death.attack." + msgId,
                    entity.getDisplayName(), ITextComponent.Serializer.fromJson(tag.getString("attackerName")));
            return super.getLocalizedDeathMessage(entity);
        }
    }
    @SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        LIVE_SOURCES.remove(event.getPlayer().getUUID());
    }
    @SubscribeEvent public static void stopped(net.minecraftforge.fml.event.server.FMLServerStoppedEvent event) { LIVE_SOURCES.clear(); repaying = null; reflecting = false; }
}
