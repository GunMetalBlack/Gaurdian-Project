package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

public class GuardianPlayerDataHolderCapability implements IGaurdianPlayerDataHolderCapability{

    int sacrficeSigilStage = 0;


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
}
