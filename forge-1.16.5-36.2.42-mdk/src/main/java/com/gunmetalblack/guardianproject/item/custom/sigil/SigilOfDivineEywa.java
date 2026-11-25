package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.EywaDamageLedger;
import com.gunmetalblack.guardianproject.common.ModEffects;
import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class SigilOfDivineEywa extends AbstractSigilItem {
    public static final int DURATION = 1200;
    private static final String TIMER = "eywaTicks";
    public SigilOfDivineEywa(Properties properties) { super(properties, Effects.GLOWING, 0); }
    @Override protected void applyConstructorPotionEffect(PlayerEntity player, float power) {}
    @Override protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null || data.getSigilData().getBoolean(EywaDamageLedger.REPAY)) return;
        data.getActiveSigils().add(this);
        data.getSigilData().putInt(TIMER, DURATION);
        player.addEffect(new EffectInstance(ModEffects.DIVINE_EYWA.get(), DURATION, 0, false, false));
    }
    @Override public void onPlayerTick(PlayerEntity player) {
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        int remaining = data.getSigilData().getInt(TIMER) - 1;
        if (remaining > 0) {
            data.getSigilData().putInt(TIMER, remaining);
            if (!player.hasEffect(ModEffects.DIVINE_EYWA.get())) player.addEffect(new EffectInstance(ModEffects.DIVINE_EYWA.get(), remaining, 0, false, false));
            return;
        }
        data.getActiveSigils().remove(this);
        data.getSigilData().remove(TIMER);
        player.removeEffect(ModEffects.DIVINE_EYWA.get());
        data.getSigilData().putBoolean(EywaDamageLedger.REPAY, true);
    }
}
