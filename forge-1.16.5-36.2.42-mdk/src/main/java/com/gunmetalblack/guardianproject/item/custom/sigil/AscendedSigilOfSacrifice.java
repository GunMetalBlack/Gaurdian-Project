package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraft.util.SoundEvents;

import static com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities.grabPlayerCapability;
public class AscendedSigilOfSacrifice extends AbstractSigilItem {
    //Controls how long the damage ability lasts
    int baseSigilDuration = 0;

    public AscendedSigilOfSacrifice(Properties properties, Effect effect, int baseDuration) {
        super(properties, effect, baseDuration);
        baseSigilDuration = baseDuration;
    }

    @Override
    protected void onSigilTrigger(PlayerEntity player, World world, float power) {
        //Set the level of stage for the sacrifice sigil power
        IGaurdianPlayerDataHolderCapability playerData = grabPlayerCapability(player);
      //Get the player health attribute
        ModifiableAttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        //Save the players current health to be reapplied latter
      playerData.getActiveSigils().add(this);
       if(playerData.getSacrficeSigilStage() == 0) {
           //Moves the stage to next meaning next activation is for if statement 1
           playerData.setSacrficeSigilStage(1);
           //Sets the duration of the damage boost
           playerData.setSacrificeSigilTDuration(baseSigilDuration);

           applyPotionEffect(player, 1, Effects.MOVEMENT_SPEED);

           //Punishment for the players health
           setPlayerHeathOffset(0.15f,playerData,healthAttr,player);
           //Cool text and player feedback
           player.displayClientMessage(new StringTextComponent("§7[§4Sigil§7] §fThe brand stings... vitality for strength."), true);
       } else if (playerData.getSacrficeSigilStage() == 1) {
           playerData.setSacrficeSigilStage(2);
           playerData.setSacrificeSigilTDuration(baseSigilDuration);
           setPlayerHeathOffset(0.30f,playerData,healthAttr,player);
           applyPotionEffect(player, 2, Effects.MOVEMENT_SPEED);
           player.displayClientMessage(new StringTextComponent("§7[§4Sigil§7] §6Your blood begins to hum with power."), true);
       }else if (playerData.getSacrficeSigilStage() == 2) {

           playerData.setSacrificeSigilTDuration(baseSigilDuration);
            //Set what pecentage of the players health they are losing
           setPlayerHeathOffset(0.65f,playerData,healthAttr,player);

           applyPotionEffect(player, 4, Effects.MOVEMENT_SPEED);
           //Effect for final form
           spawnSigilParticlesOnPlayer(world, player,ParticleTypes.FLASH,20f, 12,0.5,0.01);
           world.playSound(null, player.getX(), player.getY(), player.getZ(),
                   SoundEvents.WITHER_DEATH, SoundCategory.PLAYERS,
                   1.0F, 0.5F + (power * 0.5F));
           world.playSound(null, player.getX(), player.getY(), player.getZ(),
                   SoundEvents.ENDER_DRAGON_HURT, SoundCategory.PLAYERS,
                   1.0F, 0.5F + (power * 0.5F));
           player.displayClientMessage(new StringTextComponent("§7[§4Sigil§7] §c§lYOU ARE ASCENDING. THE PRICE IS PAID."), true);
       }
    }
    @Override
    public void onPlayerDealDamage(PlayerEntity player, LivingHurtEvent event)
    {
        IGaurdianPlayerDataHolderCapability playerData = grabPlayerCapability(player);
        int stage = playerData.getSacrficeSigilStage();

        if(stage != 0) {
            float damageBeingDelt = event.getAmount();
            LivingEntity victim = event.getEntityLiving();

            // 1. Handle Damage Scaling
            if (stage == 1) {
                damageBeingDelt *= 1.5f;
                applyCustomKnockback(victim, player, 0.5F); // Standard knockback
            } else if (stage == 2) {
                damageBeingDelt *= 3f;
                applyCustomKnockback(victim, player, 1.5F); // Strong knockback
            } else if (stage == 3) {
                damageBeingDelt *= 10f;
                applyCustomKnockback(victim, player, 5.0F); // "Yeet" level knockback
            }

            event.setAmount(damageBeingDelt);
        }
    }

    // Helper method to keep your code clean
    private void applyCustomKnockback(LivingEntity target, PlayerEntity attacker, float strength) {
        // Math.sin/cos logic helps determine the horizontal direction based on the player's rotation
        target.knockback(strength, MathHelper.sin(attacker.yRot * ((float)Math.PI / 180F)), -MathHelper.cos(attacker.yRot * ((float)Math.PI / 180F)));

        // Ensure the client knows the entity moved
        target.hurtMarked = true;
    }

    void setPlayerHeathOffset(float reducePercentage, IGaurdianPlayerDataHolderCapability playerData, ModifiableAttributeInstance healthAttr, PlayerEntity player) {
        // 1. Get the current base (e.g., 17) and remove our OLD offset (e.g., -3) to find the original (20)
        float currentBase = (float) healthAttr.getBaseValue();
        float trueBase = currentBase - playerData.getCurrentlyAppliedMaxHealthOffset();

        // 2. Calculate the NEW offset from scratch based on the true base
        // Example: Stage 2 (0.30f) of 20 hearts = 6 hearts reduction
        float newOffset = -(trueBase * reducePercentage);

        // 3. Update the capability so we know what to subtract next time
        playerData.setCurrentlyAppliedMaxHealthOffset(newOffset);

        // 4. Calculate final health and CLAMP to 1.0 (half a heart)
        float finalHealth = Math.max(1.0f, trueBase + newOffset);

        // 5. Apply to the attribute
        healthAttr.setBaseValue(finalHealth);

        // 6. Sync current health
        if (player.getHealth() > finalHealth) {
            player.setHealth(finalHealth);
        }
    }

    @Override
    public void onPlayerTick(PlayerEntity player) {
        IGaurdianPlayerDataHolderCapability playerData = grabPlayerCapability(player);
        //Get the player health attribute

        ModifiableAttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        System.out.println("WHAT TICK THINKS MY HEALTH IS"+healthAttr.getBaseValue());

        playerData.printCurrentPlayerData();
        if(playerData.getSacrficeSigilStage() == 0) {return;}

        if(playerData.getSacrificeSigilTDuration() == 200f)
        {
            player.displayClientMessage(new StringTextComponent("§7[§4Sigil§7] §c§lYour soul grows weak, as does the sigil."), true);
        }

        if(playerData.getSacrificeSigilTDuration() > 0)
        {
            playerData.setSacrificeSigilTDuration(playerData.getSacrificeSigilTDuration() - 1);
        }
        else
        {
            //FIX THIS SHIT
            healthAttr.setBaseValue(healthAttr.getBaseValue()-playerData.getCurrentlyAppliedMaxHealthOffset());
            playerData.setSacrficeSigilStage(0);
            playerData.getActiveSigils().remove(this);
            playerData.setCurrentlyAppliedMaxHealthOffset(0);
            applyPotionEffect(player, 1, Effects.WEAKNESS);
            player.displayClientMessage(new StringTextComponent("§7[§4Sigil§7] §aThe sacrifice is concluded."), true);
        }
    }

    @Override
    protected void spawnSigilParticlesOnPlayer(World world, PlayerEntity player, IParticleData particle, float baseCount, float power, double spread, double speed) {
        super.spawnSigilParticlesOnPlayer(world, player, ParticleTypes.DAMAGE_INDICATOR, 15, 0.3f, 0.5, 0.02);
    }

    @Override
    protected void applyPotionEffect(PlayerEntity player, float power,Effect newPotionEffect){
        super.applyPotionEffect(player, power, newPotionEffect);
    }
}
