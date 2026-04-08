package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;

import java.util.ArrayList;

public class GuardianPlayerDataHolderCapability implements IGaurdianPlayerDataHolderCapability{

    int sacrficeSigilStage = 0;
    float sacrficeSigilDuration = 0;
    ArrayList<AbstractSigilItem> activeSigils = new ArrayList<>();

    @Override
    public int getSacrficeSigilStage() {
        if (sacrficeSigilStage < 0) {sacrficeSigilStage = 0;}
        if (sacrficeSigilStage > 3) {sacrficeSigilStage = 3;}
        return sacrficeSigilStage;
    }

    @Override
    public int setSacrficeSigilStage(int sigilStage) {
        if (sacrficeSigilStage < 0) {sacrficeSigilStage = 0;}
        if (sacrficeSigilStage > 3) {sacrficeSigilStage = 3;}
        return sigilStage;
    }

    @Override
    public float setSacrificeSigilTDuration(float tDuration) {
        sacrficeSigilDuration = tDuration;
        return sacrficeSigilDuration;
    }

    @Override
    public float getSacrificeSigilTDuration() {
        return sacrficeSigilDuration;
    }

    @Override
    public ArrayList<AbstractSigilItem> getActiveSigils() {
        return activeSigils;
    }
}
