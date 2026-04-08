package com.gunmetalblack.guardianproject.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SingleUseSigil extends Item {
    public SingleUseSigil(Properties properties) {
        super(properties);
    }

    // 1. This starts the "charging" process
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return ActionResult.consume(itemstack);
    }

    // 2. This triggers when the player lets go
    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            int durationHeld = this.getUseDuration(stack) - timeLeft;

            // Calculate power (0.0 to 1.0)
            float power = getPowerForTime(durationHeld);

            // Only trigger if there is meaningful power (e.g., held for at least a few ticks)
            if (power > 0.1F) {
                if (!world.isClientSide()) {
                    // Pass the power to the effect logic
                    onSigilUse(player, world, power);

                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }
    public static float getPowerForTime(int ticks) {
        float f = (float)ticks / 20.0F; // Full power reached at 1 second (20 ticks)
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 10.0F);
    }

    public static void onSigilUse(PlayerEntity player, World world, float power) {
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS, 1.0F, 0.5F + (power * 0.5F));

        // Scale duration: 200 ticks (10s) at max power, down to 0 at no power
        int baseDuration = 200;
        int scaledDuration = (int)(baseDuration * power);

        if (scaledDuration > 0) {
            player.addEffect(new EffectInstance(Effects.ABSORPTION, scaledDuration, 2));
        }
    }
}
