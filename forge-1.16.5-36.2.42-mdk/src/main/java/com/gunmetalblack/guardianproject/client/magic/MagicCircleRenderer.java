package com.gunmetalblack.guardianproject.client.magic;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.common.ModEffects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

/** Client-only reusable textured spell circles; activation predicates may use synced effects. */
@Mod.EventBusSubscriber(modid = GuardianProjectMod.MOD_ID, value = Dist.CLIENT)
public final class MagicCircleRenderer {
    public static final ResourceLocation ARCANE_CIRCLE = new ResourceLocation(GuardianProjectMod.MOD_ID, "textures/magic/arcane_circle.png");
    public static final ResourceLocation VIGOR_SHIELD = new ResourceLocation(GuardianProjectMod.MOD_ID, "textures/magic/vigor_shield.png");
    private static final List<SpellVisual> VISUALS = new ArrayList<>();

    static {
        register(entity -> entity.hasEffect(ModEffects.TRAGEDY_TARGET.get()), ARCANE_CIRCLE, 0xFF6B16,
                new Circle(0.1F, 4F, 1.2F, 0),
                new Circle(3F, 3.2F, -1.8F, 0),
                new Circle(6F, 2.5F, 2.2F, 0));
        register(player -> player.hasEffect(ModEffects.VIGOR_SHIELD.get()), VIGOR_SHIELD, 0xFFFFFF,
                new Circle(1.0F, 0.7F, 2F, 90, 1.3F, 0),
                new Circle(1.0F, 0.7F, 2F, 90, 1.3F, 120),
                new Circle(1.0F, 0.7F, 2F, 90, 1.3F, 240));
        register(player -> player.hasEffect(ModEffects.MARTYRDOM.get()), ARCANE_CIRCLE, 0xFF243C,
                new Circle(0.08F, 2.1F, 0.7F, 0),
                new Circle(2.7F, 1.45F, -1.0F, 0));
    }

    private MagicCircleRenderer() {}

    /** Register on the client during setup. Radius/height are blocks; speed is degrees per tick. */
    public static void register(Predicate<LivingEntity> active, ResourceLocation texture, int rgb, Circle... circles) {
        VISUALS.add(new SpellVisual(active, texture, rgb, circles.clone()));
    }

    public static final class Circle {
        public final float height, radius, speed, tilt, orbitRadius, orbitPhase;
        /** Tilt 0 is horizontal; 90 is vertical. */
        public Circle(float height, float radius, float speed, float tilt) {
            this(height, radius, speed, tilt, 0);
        }
        public Circle(float height, float radius, float speed, float tilt, float orbitRadius) {
            this(height, radius, speed, tilt, orbitRadius, 0);
        }
        /** Orbit phase in degrees allows evenly spaced groups of shields. */
        public Circle(float height, float radius, float speed, float tilt, float orbitRadius, float orbitPhase) {
            this.height = height; this.radius = radius; this.speed = speed; this.tilt = tilt;
            this.orbitRadius = orbitRadius;
            this.orbitPhase = orbitPhase;
        }
    }

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vector3d camera = mc.gameRenderer.getMainCamera().getPosition();
        MatrixStack matrices = event.getMatrixStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableLighting();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.color4f(1, 1, 1, 1);
        try {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof LivingEntity)) continue;
                LivingEntity player = (LivingEntity) entity;
                if (!player.isAlive() || player.distanceToSqr(camera.x, camera.y, camera.z) > 192 * 192) continue;
                float partial = event.getPartialTicks();
                double x = MathHelper.lerp(partial, player.xOld, player.getX()) - camera.x;
                double y = MathHelper.lerp(partial, player.yOld, player.getY()) - camera.y;
                double z = MathHelper.lerp(partial, player.zOld, player.getZ()) - camera.z;
                float time = player.tickCount + partial;
                if (player.hasEffect(ModEffects.CALAMITY_BEAM.get())) CalamityBeamRenderer.draw(matrices, x, y, z, time);
                for (SpellVisual visual : VISUALS) {
                    if (!visual.active.test(player)) continue;
                    mc.getTextureManager().bind(visual.texture);
                    for (Circle circle : visual.circles) {
                        matrices.pushPose();
                        try {
                            matrices.translate(x, y + circle.height, z);
                            if (circle.orbitRadius != 0) {
                                matrices.mulPose(Vector3f.YP.rotationDegrees(time * circle.speed + circle.orbitPhase));
                                matrices.translate(circle.orbitRadius, 0, 0);
                                matrices.mulPose(Vector3f.YP.rotationDegrees(90));
                            }
                            matrices.mulPose(Vector3f.XP.rotationDegrees(circle.tilt));
                            if (circle.orbitRadius == 0) matrices.mulPose(Vector3f.YP.rotationDegrees(time * circle.speed));
                            float alpha = 0.72F + 0.12F * (float) Math.sin(time * 0.08);
                            draw(matrices.last().pose(), circle.radius, visual.rgb, alpha);
                        } finally { matrices.popPose(); }
                    }
                }
            }
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableLighting();
            RenderSystem.disableBlend();
            RenderSystem.color4f(1, 1, 1, 1);
        }
    }

    /** Draw at any caller-provided transform, including stationary ritual or projectile positions.
     * Caller binds its texture and configures blending/depth before calling on the render thread. */
    public static void draw(Matrix4f matrix, float radius, int rgb, float alpha) {
        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        vertex(buffer, matrix, -radius, -radius, 0, 0, rgb, alpha);
        vertex(buffer, matrix, -radius, radius, 0, 1, rgb, alpha);
        vertex(buffer, matrix, radius, radius, 1, 1, rgb, alpha);
        vertex(buffer, matrix, radius, -radius, 1, 0, rgb, alpha);
        Tessellator.getInstance().end();
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float z, float u, float v, int rgb, float alpha) {
        buffer.vertex(matrix, x, 0, z).color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, (int) (alpha * 255))
                .uv(u, v).endVertex();
    }

    private static final class SpellVisual {
        final Predicate<LivingEntity> active;
        final ResourceLocation texture;
        final int rgb;
        final Circle[] circles;
        SpellVisual(Predicate<LivingEntity> active, ResourceLocation texture, int rgb, Circle[] circles) {
            this.active = active; this.texture = texture; this.rgb = rgb; this.circles = circles;
        }
    }
}
