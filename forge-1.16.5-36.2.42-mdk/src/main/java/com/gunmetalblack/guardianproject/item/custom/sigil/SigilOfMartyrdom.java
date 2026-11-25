package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.BlockBreakingKnockback;
import com.gunmetalblack.guardianproject.common.ModEffects;
import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class SigilOfMartyrdom extends AbstractSigilItem {
    public static final int DURATION = 1200;
    private static final float OVERLOAD_EXPLOSION_POWER = 80F;
    private static final double OVERLOAD_RADIUS = 80;
    private static final String TIMER = "martyrdomTicks";
    private static final String OVERLOAD_TIMER = "martyrdomOverloadTicks";
    private static final int OVERLOAD_BUILDUP = 100;
    public SigilOfMartyrdom(Properties properties) { super(properties, Effects.DAMAGE_BOOST, DURATION); }

    @Override protected void applyConstructorPotionEffect(PlayerEntity player, float power) {}

    @Override protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        if (data.getSigilData().contains(OVERLOAD_TIMER)) return;
        boolean overloaded = data.getActiveSigils().contains(this)
                && data.getSigilData().getInt(TIMER) > 0;
        data.getSigilData().putInt(TIMER, DURATION);
        data.getActiveSigils().add(this);
        refreshBuffs(player);
        if (overloaded && world instanceof net.minecraft.world.server.ServerWorld) {
            data.getSigilData().putInt(OVERLOAD_TIMER, OVERLOAD_BUILDUP);
            ((net.minecraft.world.server.ServerWorld) world).setWeatherParameters(0, 6000, true, true);
            player.addEffect(new EffectInstance(Effects.GLOWING, OVERLOAD_BUILDUP + 5, 0));
            player.displayClientMessage(new net.minecraft.util.text.StringTextComponent(
                    "§4§lMARTYRDOM OVERLOAD — THE SKY ANSWERS"), true);
            return;
        }
        if (world instanceof net.minecraft.world.server.ServerWorld) {
            net.minecraft.entity.effect.LightningBoltEntity bolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null) {
                bolt.moveTo(player.getX(), player.getY(), player.getZ());
                bolt.setVisualOnly(true);
                world.addFreshEntity(bolt);
            }
        }
    }

    private void tickOverload(PlayerEntity player, net.minecraft.world.server.ServerWorld world,
            IGaurdianPlayerDataHolderCapability data) {
        int remaining = data.getSigilData().getInt(OVERLOAD_TIMER);
        int elapsed = OVERLOAD_BUILDUP - remaining;
        refreshBuffs(player);
        // Ten actual lightning strikes, spaced half a second apart, following the caster.
        if (elapsed % 10 == 0 && elapsed < OVERLOAD_BUILDUP) {
            net.minecraft.entity.effect.LightningBoltEntity bolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null) {
                bolt.moveTo(player.getX(), player.getY(), player.getZ());
                world.addFreshEntity(bolt);
            }
        }
        if (elapsed % 2 == 0) {
            int count = 12 + elapsed / 3;
            world.sendParticles(new net.minecraft.particles.RedstoneParticleData(1F, 0.01F, 0.03F, 1.5F),
                    player.getX(), player.getY() + 1, player.getZ(), count, 0.65, 1, 0.65, 0);
            world.sendParticles(net.minecraft.particles.ParticleTypes.FLAME,
                    player.getX(), player.getY() + 1, player.getZ(), count / 2, 0.35, 0.7, 0.35, 0.12);
            world.sendParticles(net.minecraft.particles.ParticleTypes.LARGE_SMOKE,
                    player.getX(), player.getY() + 1.3, player.getZ(), 6, 0.4, 0.7, 0.4, 0.08);
        }
        if (remaining > 1) {
            data.getSigilData().putInt(OVERLOAD_TIMER, remaining - 1);
            return;
        }
        detonateOverload(player, world);
        data.getSigilData().remove(OVERLOAD_TIMER);
        data.getSigilData().remove(TIMER);
        data.getSigilData().remove("jumpChargeStart");
        data.getActiveSigils().remove(this);
        player.removeEffect(ModEffects.MARTYRDOM.get());
        player.removeEffect(Effects.GLOWING);
        // Explicit lethal cost bypasses the ability's ordinary damage protection.
        if (player instanceof net.minecraft.entity.player.ServerPlayerEntity) {
            player.setDeltaMovement(net.minecraft.util.math.vector.Vector3d.ZERO);
            com.gunmetalblack.guardianproject.common.DeferredPlayerDeath.schedule(
                    (net.minecraft.entity.player.ServerPlayerEntity) player);
        }
    }

    private void detonateOverload(PlayerEntity player, net.minecraft.world.server.ServerWorld world) {
        // Five times the original overload radius and power; destroys terrain without fires.
        com.gunmetalblack.guardianproject.common.FuzzyExplosion.detonate(world, player,
                player.getX(), player.getY() + 0.5, player.getZ(), OVERLOAD_EXPLOSION_POWER, OVERLOAD_RADIUS, 0.5);
        net.minecraft.particles.RedstoneParticleData red =
                new net.minecraft.particles.RedstoneParticleData(1F, 0.04F, 0.01F, 2F);
        // A rising column and broad crown suggest a mushroom cloud without spawning entities.
        world.sendParticles(net.minecraft.particles.ParticleTypes.LARGE_SMOKE,
                player.getX(), player.getY() + 5, player.getZ(), 100, 1.5, 4, 1.5, 0.06);
        world.sendParticles(net.minecraft.particles.ParticleTypes.LARGE_SMOKE,
                player.getX(), player.getY() + 11, player.getZ(), 120, 5, 1.5, 5, 0.08);
        for (int ring = 1; ring <= 4; ring++) {
            double radius = ring * (OVERLOAD_RADIUS / 4.0);
            for (int point = 0; point < 48; point++) {
                double angle = point * Math.PI * 2 / 48;
                world.sendParticles(red, player.getX() + Math.cos(angle) * radius,
                        player.getY() + 0.3, player.getZ() + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0);
            }
        }
        player.displayClientMessage(new net.minecraft.util.text.StringTextComponent(
                "§4§lMARTYRDOM OVERLOAD"), true);
    }

    private void refreshBuffs(PlayerEntity player) {
        // Short renewable buffs expire naturally and preserve stronger external effects.
        player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 25, 2));
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 25, 2));
        player.addEffect(new EffectInstance(Effects.JUMP, 25, 2));
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 25, 0));
        player.addEffect(new EffectInstance(ModEffects.MARTYRDOM.get(), 25, 0, false, false, true));
    }

    @Override public void onPlayerDealDamage(PlayerEntity player, LivingHurtEvent event) {
        event.setAmount(event.getAmount() * 50F);
        // Halve current health each tick for two seconds; reapply once the effect expires.
        if (!event.getEntityLiving().hasEffect(ModEffects.CRIPPLED.get()))
            event.getEntityLiving().addEffect(new EffectInstance(ModEffects.CRIPPLED.get(), 40));
        BlockBreakingKnockback.launch(event.getEntityLiving(), player, 4F);
    }

    @Override public void onPlayerTakeDamage(PlayerEntity player, LivingHurtEvent event) {
        if (event.getSource() == DamageSource.OUT_OF_WORLD) return;
        event.setAmount(Math.min(event.getAmount() * 0.01F, Math.max(0, player.getHealth() - 1F)));
    }

    @Override public void onPlayerTick(PlayerEntity player) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        if (data.getSigilData().contains(OVERLOAD_TIMER) && player.level instanceof net.minecraft.world.server.ServerWorld) {
            tickOverload(player, (net.minecraft.world.server.ServerWorld) player.level, data);
            return;
        }
        int remaining = data.getSigilData().getInt(TIMER) - 1;
        if (remaining > 0) {
            if (player.tickCount % 3 == 0 && player.level instanceof net.minecraft.world.server.ServerWorld) {
                net.minecraft.world.server.ServerWorld world = (net.minecraft.world.server.ServerWorld) player.level;
                world.sendParticles(new net.minecraft.particles.RedstoneParticleData(1F, 0.02F, 0.05F, 1F),
                        player.getX(), player.getY() + 0.9, player.getZ(), 14, 0.55, 0.85, 0.55, 0);
            }
            data.getSigilData().putInt(TIMER, remaining);
            if (player.tickCount % 10 == 0) refreshBuffs(player);
            return;
        }
        // Keep protection through the explosion, then remove the activation before setting its price.
        player.level.explode(player, player.getX(), player.getY(), player.getZ(), 4F, Explosion.Mode.NONE);
        data.getActiveSigils().remove(this);
        data.getSigilData().remove(TIMER);
        data.getSigilData().remove("jumpChargeStart");
        player.removeEffect(ModEffects.MARTYRDOM.get());
        player.setAbsorptionAmount(0);
        player.setHealth(1F);
    }
}
