package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianPlayerDataHolderCapabilityProvider implements ICapabilitySerializable<INBT> {

    private IGaurdianPlayerDataHolderCapability capabilityInstance = new GuardianPlayerDataHolderCapability();


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction direction) {
        return GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.orEmpty(capability, LazyOptional.of(()-> this.capabilityInstance));
    }

    @Override
    public net.minecraft.nbt.INBT serializeNBT() {
        System.out.println("GUN MAGIC");
        System.out.println(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER);
        System.out.println(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage());
        return GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage().writeNBT(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER, capabilityInstance, null);    }

    @Override
    public void deserializeNBT(net.minecraft.nbt.INBT nbt) {
        GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage().readNBT(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER, capabilityInstance, null, nbt);
    }
}
