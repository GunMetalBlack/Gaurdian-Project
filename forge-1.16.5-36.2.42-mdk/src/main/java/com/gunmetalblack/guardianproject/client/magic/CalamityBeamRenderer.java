package com.gunmetalblack.guardianproject.client.magic;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

/** Solid emissive cylinders, aligned with the server's angled excavation path. */
public final class CalamityBeamRenderer {
    public static void draw(MatrixStack matrices, double x, double y, double z, float time) {
        matrices.pushPose();
        try {
            matrices.translate(x, y, z);
            matrices.mulPose(Vector3f.ZP.rotationDegrees(-20.556F));
            RenderSystem.disableTexture();
            try {
                cylinder(matrices.last().pose(), 4.2F, 150F, 255, 55, 5, 45);
                cylinder(matrices.last().pose(), 2.8F, 150F, 255, 110, 15, 100);
                cylinder(matrices.last().pose(), 1.2F, 150F, 255, 245, 185, 240);
            } finally { RenderSystem.enableTexture(); }
            Minecraft.getInstance().getTextureManager().bind(MagicCircleRenderer.ARCANE_CIRCLE);
            for (int i = 0; i < 4; i++) {
                matrices.pushPose();
                matrices.translate(0, 5 + i * 12 + (time % 60) * 0.08, 0);
                matrices.mulPose(Vector3f.YP.rotationDegrees(time * (i % 2 == 0 ? 1.5F : -1.5F)));
                MagicCircleRenderer.draw(matrices.last().pose(), 5.5F - i * 0.4F, 0xFF6416, 0.85F);
                matrices.popPose();
            }
        } finally { matrices.popPose(); }
    }
    private static void cylinder(Matrix4f matrix, float radius, float length, int r, int g, int b, int alpha) {
        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 32; i++) {
            double a = i * Math.PI * 2 / 32, c = (i + 1) * Math.PI * 2 / 32;
            float x1 = (float) Math.cos(a) * radius, z1 = (float) Math.sin(a) * radius;
            float x2 = (float) Math.cos(c) * radius, z2 = (float) Math.sin(c) * radius;
            buffer.vertex(matrix, x1, 0, z1).color(r,g,b,alpha).endVertex();
            buffer.vertex(matrix, x1, length, z1).color(r,g,b,alpha).endVertex();
            buffer.vertex(matrix, x2, length, z2).color(r,g,b,alpha).endVertex();
            buffer.vertex(matrix, x2, 0, z2).color(r,g,b,alpha).endVertex();
        }
        Tessellator.getInstance().end();
    }
}
