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
    public AscendedSigilOfSacrifice(Properties properties, Effect effect, int baseDuration) {
        super(properties, effect, baseDuration);
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
           playerData.setSacrificeSigilTDuration(6000);
           setPlayerHeathOffset(3,playerData,healthAttr,player);
       } else if (playerData.getSacrficeSigilStage() == 1) {
           playerData.setSacrficeSigilStage(2);
           playerData.setSacrificeSigilTDuration(6000);
           setPlayerHeathOffset(6,playerData,healthAttr,player);
       }else if (playerData.getSacrficeSigilStage() == 2) {
           playerData.setSacrificeSigilTDuration(6000);
           setPlayerHeathOffset(9,playerData,healthAttr,player);
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

    void setPlayerHeathOffset(float reduceHearts,IGaurdianPlayerDataHolderCapability playerData , ModifiableAttributeInstance healthAttr, PlayerEntity player)
    {
        //0 - 3
        playerData.setCurrentlyAppliedMaxHealthOffset(playerData.getCurrentlyAppliedMaxHealthOffset()-reduceHearts);

        //20-3 = 17
        healthAttr.setBaseValue(healthAttr.getBaseValue()+playerData.getCurrentlyAppliedMaxHealthOffset());

        //player.setHealth(Math.min(player.getHealth(), (float) healthAttr.getBaseValue()));
    }

    @Override
    public void onPlayerTick(PlayerEntity player) {
        IGaurdianPlayerDataHolderCapability playerData = grabPlayerCapability(player);
        //Get the player health attribute
        ModifiableAttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);

        playerData.printCurrentPlayerData();
        if(playerData.getSacrficeSigilStage() == 0) {return;}

        if(playerData.getSacrificeSigilTDuration() > 0)
        {
            playerData.setSacrificeSigilTDuration(playerData.getSacrificeSigilTDuration() - 1);
        }
        else
        {
            playerData.setSacrficeSigilStage(0);
            playerData.getActiveSigils().remove(this);
            //FIX THIS SHIT
            healthAttr.setBaseValue(healthAttr.getBaseValue()+playerData.getCurrentlyAppliedMaxHealthOffset());
            playerData.setCurrentlyAppliedMaxHealthOffset(0);
        }
    }
}
