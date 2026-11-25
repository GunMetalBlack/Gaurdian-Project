package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuardianPlayerDataHolderCapabilityProvider implements ICapabilitySerializable<INBT> {

    private final IGaurdianPlayerDataHolderCapability capabilityInstance = new GuardianPlayerDataHolderCapability();
    private final LazyOptional<IGaurdianPlayerDataHolderCapability> capabilityOptional = LazyOptional.of(() -> capabilityInstance);


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction direction) {
        return GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.orEmpty(capability, capabilityOptional);
    }

    public void invalidate() { capabilityOptional.invalidate(); }

    @Override
    public net.minecraft.nbt.INBT serializeNBT() {
        return GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage().writeNBT(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER, capabilityInstance, null);
    }

    @Override
    public void deserializeNBT(net.minecraft.nbt.INBT nbt) {
        GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage().readNBT(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER, capabilityInstance, null, nbt);
    }
}
