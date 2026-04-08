package com.gunmetalblack.guardianproject.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class SingleUseSigil extends Item {
    public SingleUseSigil(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getLevel();
        if(!world.isClientSide())
        {
            PlayerEntity player = context.getPlayer();
            onSigilUse(player, world);
            stack.shrink(1);
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // Standard for items that can be held indefinitely
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW; // This makes the player's arm pull back like a bow
    }

    public static void onSigilUse(PlayerEntity player, World world)
    {
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMBIENT_NETHER_WASTES_MOOD, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.addEffect(new EffectInstance(Effects.ABSORPTION, 100, 2));
    }
}
