package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class GuardianPlayerDataHolderCapabilityStorage implements Capability.IStorage<IGaurdianPlayerDataHolderCapability>{

    @Nullable
    @Override
    public INBT writeNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("sacrficeSigilStage", instance.getSacrficeSigilStage());
        return nbt;
    }

    @Override
    public void readNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction, INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        instance.setSacrficeSigilStage(nbt.getInt("sacrficeSigilStage"));
    }
}
