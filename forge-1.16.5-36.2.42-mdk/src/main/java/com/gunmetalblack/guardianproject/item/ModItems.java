package com.gunmetalblack.guardianproject.item;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.item.custom.SingleUseSigil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GuardianProjectMod.MOD_ID);

    public static final RegistryObject<Item> BLANK_SIGIL = ITEMS.register("blank_sigil",
            ()-> new SingleUseSigil(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
