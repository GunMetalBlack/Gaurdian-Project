package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;

public class GuardianPlayerDataHolderCapabilityStorage implements Capability.IStorage<IGaurdianPlayerDataHolderCapability>{

    @Nullable
    @Override
    public INBT writeNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("sacrficeSigilStage", instance.getSacrficeSigilStage());
        ListNBT activeSigils = new ListNBT();
        for(RegistryObject<AbstractSigilItem> activeSigilsItem : instance.getActiveSigils()) {

        }
        return nbt;
    }

    @Override
    public void readNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction, INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        instance.setSacrficeSigilStage(nbt.getInt("sacrficeSigilStage"));
    }
}
