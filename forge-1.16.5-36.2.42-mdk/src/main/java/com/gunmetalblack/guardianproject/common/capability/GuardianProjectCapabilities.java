package com.gunmetalblack.guardianproject.common.capability;

import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.eventbus.api.IEventBus;

public class GuardianProjectCapabilities {
    private GuardianProjectCapabilities(){}
    @CapabilityInject(IGaurdianPlayerDataHolderCapability.class)
    public static Capability<IGaurdianPlayerDataHolderCapability> GUARDIAN_PLAYER_DATA_HELPER;

    public static IGaurdianPlayerDataHolderCapability grabPlayerCapability(PlayerEntity player)
    {
        return player.getCapability(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER).orElse(null);
    }
}
