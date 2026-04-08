package com.gunmetalblack.guardianproject.item.custom.sigil;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.world.World;
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
      playerData.getActiveSigils().add(this);
       if(playerData.getSacrficeSigilStage() == 0) {
           playerData.setSacrficeSigilStage(1);
           playerData.setSacrificeSigilTDuration(6000);
       } else if (playerData.getSacrficeSigilStage() == 1) {
           playerData.setSacrficeSigilStage(2);
           playerData.setSacrificeSigilTDuration(6000);
       }else if (playerData.getSacrficeSigilStage() == 2) {
           playerData.setSacrficeSigilStage(3);
           playerData.setSacrificeSigilTDuration(6000);
       }
    }

    @Override
    public void onPlayerTick(PlayerEntity player) {
        super.onPlayerTick(player);
    }
}
