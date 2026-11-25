package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GuardianPlayerDataHolderCapability implements IGaurdianPlayerDataHolderCapability{

    private int sacrficeSigilStage = 0;
    private float sacrficeSigilDuration = 0;
    private float maxHealthOffset = 0;
    private final Set<AbstractSigilItem> activeSigils = new HashSet<>();
    private final net.minecraft.nbt.CompoundNBT sigilData = new net.minecraft.nbt.CompoundNBT();

    @Override
    public net.minecraft.nbt.CompoundNBT getSigilData() { return sigilData; }

    @Override
    public void printCurrentPlayerData()
    {
        // Kept as a capability API hook; callers may add structured logging when needed.
    }

    @Override
    public int getSacrficeSigilStage() {
        if (sacrficeSigilStage < 0) {sacrficeSigilStage = 0;}
        if (sacrficeSigilStage > 3) {sacrficeSigilStage = 3;}
        return sacrficeSigilStage;
    }

    //If its within the stage range set the sigil stage
    @Override
    public void setSacrficeSigilStage(int sigilStage) {
        this.sacrficeSigilStage = sigilStage;
        if (sacrficeSigilStage < 0) {sacrficeSigilStage = 0;}
        if (sacrficeSigilStage > 3) {sacrficeSigilStage = 3;}
    }

    @Override
    public void setSacrificeSigilTDuration(float tDuration) {
        sacrficeSigilDuration = tDuration;
    }

    @Override
    public float getSacrificeSigilTDuration() {
        return sacrficeSigilDuration;
    }

    @Override
    public Set<AbstractSigilItem> getActiveSigils() {
        return activeSigils;
    }

    @Override
    public float getCurrentlyAppliedMaxHealthOffset() {
        return maxHealthOffset;
    }

    @Override
    public void setCurrentlyAppliedMaxHealthOffset(float maxHealthOffset) {
        this.maxHealthOffset = maxHealthOffset;
    }

    @Override
    public void clearActiveEffects() {
        sacrficeSigilStage = 0;
        sacrficeSigilDuration = 0;
        maxHealthOffset = 0;
        activeSigils.clear();
        for (String key : new java.util.HashSet<>(sigilData.getAllKeys())) sigilData.remove(key);
    }
}
