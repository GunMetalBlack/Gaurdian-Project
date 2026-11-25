package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import com.gunmetalblack.guardianproject.common.MeteorStrike;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class SigilOfTragedy extends AbstractSigilItem {
    public SigilOfTragedy(Properties properties) { super(properties, Effects.DAMAGE_BOOST, 0); }
    @Override protected void applyConstructorPotionEffect(PlayerEntity player, float power) {}
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.pick(128, 1, false).getType() != RayTraceResult.Type.BLOCK) {
            if (!world.isClientSide) player.displayClientMessage(new StringTextComponent("Aim at a block within 128 blocks."), true);
            return ActionResult.fail(player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }
    @Override public void releaseUsing(ItemStack stack, World world, net.minecraft.entity.LivingEntity entity, int timeLeft) {
        if (!(entity instanceof PlayerEntity) || entity.pick(128, 1, false).getType() != RayTraceResult.Type.BLOCK) return;
        super.releaseUsing(stack, world, entity, timeLeft);
    }
    @Override protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        if (!(player instanceof ServerPlayerEntity)) return;
        Vector3d target = player.pick(128, 1, false).getLocation().add(0, 0.1, 0);
        MeteorStrike.begin((ServerPlayerEntity) player, target);
    }
}
