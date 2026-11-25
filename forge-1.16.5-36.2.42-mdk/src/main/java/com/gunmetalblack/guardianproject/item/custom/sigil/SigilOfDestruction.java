package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/** A single-use ten-fireball volley using vanilla ghast projectiles. */
public class SigilOfDestruction extends AbstractSigilItem {
    public static final int PROJECTILE_COUNT = 10;

    public SigilOfDestruction(Properties properties) { super(properties, Effects.DAMAGE_BOOST, 0); }
    @Override protected void applyConstructorPotionEffect(PlayerEntity player, float power) {}

    @Override protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        if (world.isClientSide) return;
        Vector3d forward = player.getLookAngle().normalize();
        Vector3d reference = Math.abs(forward.y) > 0.95 ? new Vector3d(0, 0, 1) : new Vector3d(0, 1, 0);
        Vector3d right = forward.cross(reference).normalize();
        Vector3d up = right.cross(forward).normalize();
        Vector3d origin = player.getEyePosition(1F).add(forward.scale(1.2));
        for (int i = 0; i < PROJECTILE_COUNT; i++) {
            double angle = Math.PI * 2 * i / PROJECTILE_COUNT;
            Vector3d offset = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vector3d direction = forward.add(offset.scale(0.18)).normalize();
            FireballEntity fireball = new FireballEntity(world, player, direction.x, direction.y, direction.z) {
                @Override protected boolean canHitEntity(Entity target) {
                    // Sibling projectiles overlap initially; do not detonate the volley on itself.
                    if (target instanceof FireballEntity && ((FireballEntity) target).getOwner() == getOwner()) return false;
                    return super.canHitEntity(target);
                }
            };
            Vector3d spawn = origin.add(offset.scale(0.35));
            fireball.setPos(spawn.x, spawn.y, spawn.z);
            fireball.setDeltaMovement(direction.scale(0.6));
            fireball.explosionPower = 1;
            world.addFreshEntity(fireball);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GHAST_SHOOT, SoundCategory.PLAYERS, 1F, 0.9F);
    }
}
