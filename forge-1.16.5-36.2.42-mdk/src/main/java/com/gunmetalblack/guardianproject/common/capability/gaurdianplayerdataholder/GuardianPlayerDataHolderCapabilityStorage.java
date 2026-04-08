package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;

public class GuardianPlayerDataHolderCapabilityStorage implements Capability.IStorage<IGaurdianPlayerDataHolderCapability>{

    @Nullable
    @Override
    public INBT writeNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("sacrficeSigilStage", instance.getSacrficeSigilStage());
        ListNBT activeSigils = new ListNBT();
        for(AbstractSigilItem sigil : instance.getActiveSigils()) {
            //Adds the active sigil resource location into nbt
            StringNBT sigilResourceLocation = StringNBT.valueOf(ForgeRegistries.ITEMS.getKey(sigil).toString());
            activeSigils.add(sigilResourceLocation);
        }
        nbt.put("activeSigils", activeSigils);
        return nbt;
    }

    @Override
    public void readNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction, INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        instance.setSacrficeSigilStage(nbt.getInt("sacrficeSigilStage"));
        instance.getActiveSigils().clear();
        nbt.getList("activeSigils", 8).forEach(activeSigil -> {
            instance.getActiveSigils().add((AbstractSigilItem) ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(activeSigil.getAsString())));
        });
    }
}
