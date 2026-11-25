package com.gunmetalblack.guardianproject.client;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.common.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;

@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MeteorRendering {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.METEOR.get(),
                manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 12F, true));
    }
}
