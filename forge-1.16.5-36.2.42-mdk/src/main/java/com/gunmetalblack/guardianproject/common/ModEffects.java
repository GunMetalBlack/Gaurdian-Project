package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, GuardianProjectMod.MOD_ID);
    public static final RegistryObject<Effect> CALAMITY_BEAM = EFFECTS.register("calamity_beam", () -> new Effect(EffectType.NEUTRAL, 0xFF5511) {});
    public static final RegistryObject<Effect> TRAGEDY_TARGET = EFFECTS.register("tragedy_target", () -> new Effect(EffectType.NEUTRAL, 0xFF6B16) {});
    public static final RegistryObject<Effect> VIGOR_SHIELD = EFFECTS.register("vigor_shield", () -> new Effect(EffectType.BENEFICIAL, 0x43FF9B) {});
    public static final RegistryObject<Effect> MARTYRDOM = EFFECTS.register("martyrdom", () -> new Effect(EffectType.BENEFICIAL, 0xDD142C) {});
    public static final RegistryObject<Effect> CRIPPLED = EFFECTS.register("crippled", () -> new Effect(EffectType.HARMFUL, 0x8B1630) {
        @Override public boolean isDurationEffectTick(int duration, int amplifier) { return duration > 0; }
        @Override public void applyEffectTick(LivingEntity entity, int amplifier) {
            // Allow each tick's damage through vanilla's hurt immunity window.
            int immunity = entity.invulnerableTime;
            entity.invulnerableTime = 0;
            entity.hurt(DamageSource.MAGIC.bypassArmor(), entity.getHealth() * 0.5F);
            entity.invulnerableTime = Math.max(immunity, entity.invulnerableTime);
        }
    });
    private ModEffects() {}
}
