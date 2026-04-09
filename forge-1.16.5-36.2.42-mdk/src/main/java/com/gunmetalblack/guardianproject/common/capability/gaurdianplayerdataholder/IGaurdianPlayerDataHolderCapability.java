package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface IGaurdianPlayerDataHolderCapability {
    int getSacrficeSigilStage();
    void setSacrficeSigilStage(int sigilStage);
    void setSacrificeSigilTDuration(float tDuration);
    float getSacrificeSigilTDuration();
    Set<AbstractSigilItem> getActiveSigils();
    float getCurrentlyAppliedMaxHealthOffset();
    void setCurrentlyAppliedMaxHealthOffset(float maxHealthOffset);
    void printCurrentPlayerData();
}
