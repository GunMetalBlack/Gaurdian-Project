package com.gunmetalblack.guardianproject.client.magic;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.common.ModEffects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID, value = Dist.CLIENT)
public final class EywaVisuals {
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level == null || mc.isPaused()) return;
        for (PlayerEntity player : mc.level.players()) {
            if (!player.hasEffect(ModEffects.DIVINE_EYWA.get()) || !player.isAlive()) continue;
            if (mc.player != null && player.distanceToSqr(mc.player) > 64 * 64) continue;
            if (player.tickCount % 3 != 0) continue;
            double angle = player.getRandom().nextDouble() * Math.PI * 2;
            double x = player.getX() + Math.cos(angle) * 0.7;
            double z = player.getZ() + Math.sin(angle) * 0.7;
            double y = player.getY() + 1.3 + player.getRandom().nextDouble();
            mc.level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, -0.035, 0);
            mc.level.addParticle(new RedstoneParticleData(1F, 0.86F, 0.42F, 0.65F), x, y, z, 0, -0.02, 0);
        }
    }

    /** Geometry-only halo, emissive and visible from either side. */
    public static void halo(MatrixStack matrices, double x, double y, double z, float time) {
        matrices.pushPose();
        matrices.translate(x, y + 2.2 + Math.sin(time * 0.045) * 0.045, z);
        RenderSystem.disableTexture();
        try {
            ring(matrices.last().pose(), 0.43F, 0.51F, 235);
            ring(matrices.last().pose(), 0.38F, 0.57F, 45);
        } finally { RenderSystem.enableTexture(); matrices.popPose(); }
    }
    private static void ring(Matrix4f matrix, float inner, float outer, int alpha) {
        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 64; i++) {
            double a = i * Math.PI * 2 / 64, b = (i + 1) * Math.PI * 2 / 64;
            vertex(buffer, matrix, a, inner, alpha); vertex(buffer, matrix, a, outer, alpha);
            vertex(buffer, matrix, b, outer, alpha); vertex(buffer, matrix, b, inner, alpha);
        }
        Tessellator.getInstance().end();
    }
    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double angle, float radius, int alpha) {
        buffer.vertex(matrix, (float) Math.cos(angle) * radius, 0, (float) Math.sin(angle) * radius)
                .color(255, 233, 140, alpha).endVertex();
    }
}
