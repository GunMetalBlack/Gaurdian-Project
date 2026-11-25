package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

/** Tier I life sigil: a fixed three-minute defensive blessing. */
public class MortalHealingSigil extends AbstractSigilItem {
    public static final int DURATION = 3 * 60 * 20;

    public MortalHealingSigil(Properties properties) {
        super(properties, Effects.ABSORPTION, DURATION);
    }

    @Override
    protected void applyConstructorPotionEffect(PlayerEntity player, float power) {
        // Amplifier zero is level I. Any successful cast grants the full duration.
        player.addEffect(new EffectInstance(Effects.ABSORPTION, DURATION, 0));
        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, DURATION, 0));
    }
}
