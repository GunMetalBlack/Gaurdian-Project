package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.ModEffects;
import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class SigilOfVigor extends AbstractSigilItem {
    public static final int DURATION = 3600;
    public static final int REGENERATION_TICKS = 100;
    private static final String TIMER = "vigorTicks", COOLDOWN = "vigorShieldCooldown";

    public SigilOfVigor(Properties properties) { super(properties, Effects.DAMAGE_RESISTANCE, DURATION); }
    @Override protected void applyConstructorPotionEffect(PlayerEntity player, float power) {}

    @Override protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        boolean alreadyActive = data.getActiveSigils().contains(this);
        data.getActiveSigils().add(this);
        data.getSigilData().putInt(TIMER, DURATION);
        // Refresh duration without letting repeated consumables bypass shield regeneration.
        if (!alreadyActive) data.getSigilData().putInt(COOLDOWN, 0);
        if (data.getSigilData().getInt(COOLDOWN) == 0) showShield(player);
    }

    private void showShield(PlayerEntity player) {
        player.addEffect(new EffectInstance(ModEffects.VIGOR_SHIELD.get(), 25, 0, false, false, true));
    }

    @Override public void onPlayerAttacked(PlayerEntity player, LivingAttackEvent event) {
        if (event.getAmount() <= 0 || event.isCanceled()) return;
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null || data.getSigilData().getInt(COOLDOWN) > 0) return;
        event.setCanceled(true);
        data.getSigilData().putInt(COOLDOWN, REGENERATION_TICKS);
        player.removeEffect(ModEffects.VIGOR_SHIELD.get());
        player.level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundCategory.PLAYERS, 1F, 1.4F);
        if (player.level instanceof ServerWorld) ((ServerWorld) player.level).sendParticles(
                ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 35, 0.7, 0.8, 0.7, 0.15);
    }

    @Override public void onPlayerTick(PlayerEntity player) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        int remaining = data.getSigilData().getInt(TIMER) - 1;
        if (remaining <= 0) {
            data.getSigilData().remove(TIMER);
            data.getSigilData().remove(COOLDOWN);
            data.getActiveSigils().remove(this);
            player.removeEffect(ModEffects.VIGOR_SHIELD.get());
            return;
        }
        data.getSigilData().putInt(TIMER, remaining);
        int cooldown = data.getSigilData().getInt(COOLDOWN);
        if (cooldown > 0) {
            data.getSigilData().putInt(COOLDOWN, --cooldown);
            if (cooldown == 0) player.level.playSound(null, player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.7F, 1.5F);
        }
        if (cooldown == 0) showShield(player);
    }
}
