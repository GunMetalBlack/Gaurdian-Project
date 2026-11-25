package com.gunmetalblack.guardianproject.client;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.common.MartyrdomJump;
import com.gunmetalblack.guardianproject.common.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID, value = Dist.CLIENT)
public final class MartyrdomJumpInput {
    private static boolean held;
    @SubscribeEvent
    public static void input(InputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        boolean active = event.getPlayer().hasEffect(ModEffects.MARTYRDOM.get());
        boolean next = active && mc.screen == null && mc.options.keyJump.isDown();
        if (next != held) {
            MartyrdomJump.CHANNEL.sendToServer(new MartyrdomJump.KeyState(next));
            held = next;
        }
        if (active) event.getMovementInput().jumping = false;
    }
}
