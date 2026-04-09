package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GuardianPlayerDataHolderCapability implements IGaurdianPlayerDataHolderCapability{

    int sacrficeSigilStage = 0;
    float sacrficeSigilDuration = 0;
    float maxHealthOffset = 0;
    Set<AbstractSigilItem> activeSigils = new HashSet<>();

    @Override
    public void printCurrentPlayerData()
    {
        System.out.println("====PLAYER DATA====");
        System.out.println("Sacrfice Sigil Stage: " + sacrficeSigilStage);
        System.out.println("Sacrfice Sigil Duration: " + sacrficeSigilDuration);
        System.out.println("Max Health Offset: " + maxHealthOffset);
        System.out.println("Active Sigils: " + activeSigils);
        System.out.println("===================");
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
        if (sacrficeSigilStage > 2) {sacrficeSigilStage = 2;}
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
        System.out.println("GET MAX HEALTH OFFSET:" + maxHealthOffset);
        return maxHealthOffset;
    }

    @Override
    public void setCurrentlyAppliedMaxHealthOffset(float maxHealthOffset) {
        System.out.println("SET MAX HEALTH OFFSET:" + maxHealthOffset);
        this.maxHealthOffset = maxHealthOffset;
    }
}
