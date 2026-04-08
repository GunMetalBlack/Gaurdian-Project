package com.gunmetalblack.guardianproject.item.custom.sigil.tools;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class AbstractSigilItem extends Item {
    private final Effect effect;
    private final int baseDuration;

    public AbstractSigilItem(Properties properties, Effect effect, int baseDuration) {
        super(properties);
        this.effect = effect;
        this.baseDuration = baseDuration;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.startUsingItem(hand);
        return ActionResult.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            int durationHeld = this.getUseDuration(stack) - timeLeft;
            float power = getPowerForTime(durationHeld);

            if (power > 0.1F) {
                if (!world.isClientSide()) {
                    // 1. Audio
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS,
                            1.0F, 0.5F + (power * 0.5F));

                    // 2. Particles
                    spawnSigilParticles(world, player, power);

                    // 3. Potion Logic
                    applyPotionEffect(player, power);

                    // 4. Custom subclass logic
                    onSigilTrigger(player, world, power);

                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                }
            }
        }
    }

    protected void applyPotionEffect(PlayerEntity player, float power) {
        int scaledDuration = (int)(this.baseDuration * power);
        if (scaledDuration > 0) {
            player.addEffect(new EffectInstance(this.effect, scaledDuration, 2));
        }
    }

    protected void spawnSigilParticles(World world, PlayerEntity player, float power) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            int count = (int)(50 * power); // More charge = more particles

            // Default particles - can be changed in subclasses by overriding this method
            serverWorld.sendParticles(getPrimaryParticle(),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    count, 0.5, 0.5, 0.5, 0.03);
        }
    }

    /**
     * Override this in subclasses to change the color/type of the sigil's magic
     */
    protected IParticleData getPrimaryParticle() {
        return ParticleTypes.ENCHANT;
    }

    protected void onSigilTrigger(PlayerEntity player, World world, float power) {}

    @Override
    public int getUseDuration(ItemStack stack) { return 72000; }

    @Override
    public UseAction getUseAnimation(ItemStack stack) { return UseAction.BOW; }

    public static float getPowerForTime(int ticks) {
        float f = (float)ticks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }
}