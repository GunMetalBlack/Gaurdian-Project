package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
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
        nbt.put("sigilData", instance.getSigilData().copy());
        nbt.putInt("sacrficeSigilStage", instance.getSacrficeSigilStage());
        nbt.putFloat("sacrificeSigilDuration", instance.getSacrificeSigilTDuration());
        nbt.putFloat("maxHealthOffset", instance.getCurrentlyAppliedMaxHealthOffset());
        ListNBT activeSigils = new ListNBT();
        for(AbstractSigilItem sigil : instance.getActiveSigils()) {
            //Adds the active sigil resource location into nbt
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(sigil);
            if (key == null) continue;
            StringNBT sigilResourceLocation = StringNBT.valueOf(key.toString());
            activeSigils.add(sigilResourceLocation);
        }
        nbt.put("activeSigils", activeSigils);
        return nbt;
    }

    @Override
    public void readNBT(Capability<IGaurdianPlayerDataHolderCapability> capability, IGaurdianPlayerDataHolderCapability instance, Direction direction, INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        for (String key : new java.util.HashSet<>(instance.getSigilData().getAllKeys())) instance.getSigilData().remove(key);
        instance.getSigilData().merge(nbt.getCompound("sigilData"));
        instance.setSacrficeSigilStage(nbt.getInt("sacrficeSigilStage"));
        instance.setSacrificeSigilTDuration(nbt.getFloat("sacrificeSigilDuration"));
        instance.setCurrentlyAppliedMaxHealthOffset(nbt.getFloat("maxHealthOffset"));
        instance.getActiveSigils().clear();
        nbt.getList("activeSigils", 8).forEach(activeSigil -> {
            ResourceLocation id = ResourceLocation.tryParse(activeSigil.getAsString());
            if (id == null) return;
            net.minecraft.item.Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item instanceof AbstractSigilItem) instance.getActiveSigils().add((AbstractSigilItem) item);
        });
    }
}
