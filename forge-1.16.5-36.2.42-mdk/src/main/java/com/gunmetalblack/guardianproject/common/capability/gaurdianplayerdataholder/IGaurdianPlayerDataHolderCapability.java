package com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder;

import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;

public interface IGaurdianPlayerDataHolderCapability {
    int getSacrficeSigilStage();
    int setSacrficeSigilStage(int sigilStage);
    float setSacrificeSigilTDuration(float tDuration);
    float getSacrificeSigilTDuration();
    ArrayList<RegistryObject<AbstractSigilItem>> getActiveSigils();
}
