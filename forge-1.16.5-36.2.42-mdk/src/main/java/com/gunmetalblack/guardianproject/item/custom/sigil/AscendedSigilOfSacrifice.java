package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.RegistryObject;
import org.spongepowered.asm.mixin.Interface;

import static com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities.grabPlayerCapability;
public class AscendedSigilOfSacrifice extends AbstractSigilItem {
    int baseSigilDuration = 1000;
    public AscendedSigilOfSacrifice(Properties properties, Effect effect, int baseDuration) {
        super(properties, effect, baseDuration);
        //baseSigilDuration = baseDuration;
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
           playerData.setSacrficeSigilStage(1);
           playerData.setSacrificeSigilTDuration(baseSigilDuration);
           setPlayerHeathOffset(0.15f,playerData,healthAttr,player);
       } else if (playerData.getSacrficeSigilStage() == 1) {
           playerData.setSacrficeSigilStage(2);
           playerData.setSacrificeSigilTDuration(baseSigilDuration);
           setPlayerHeathOffset(0.30f,playerData,healthAttr,player);
       }else if (playerData.getSacrficeSigilStage() == 2) {
           playerData.setSacrificeSigilTDuration(baseSigilDuration);
           setPlayerHeathOffset(0.45f,playerData,healthAttr,player);
       }
    }

    @Override
    public void onPlayerDealDamage(PlayerEntity player, LivingHurtEvent event)
    {
        //Multiplies the damage output of the player
        IGaurdianPlayerDataHolderCapability playerData = grabPlayerCapability(player);
        if(playerData.getSacrficeSigilStage() != 0) {
            float damageBeingDelt = event.getAmount();
            if (playerData.getSacrficeSigilStage() == 1) {
                damageBeingDelt = damageBeingDelt * 1.5f;
            } else if (playerData.getSacrficeSigilStage() == 2) {
                damageBeingDelt = damageBeingDelt * 2f;
            } else if (playerData.getSacrficeSigilStage() == 3) {
                damageBeingDelt = damageBeingDelt * 3f;
            }
            event.setAmount(damageBeingDelt);
        }
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
        }
    }
}
