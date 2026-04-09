package com.gunmetalblack.guardianproject.item;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import com.gunmetalblack.guardianproject.item.custom.sigil.AscendedSigilOfSacrifice;
import com.gunmetalblack.guardianproject.item.custom.sigil.MortalPotionSigil;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GuardianProjectMod.MOD_ID);
    //MORTAL SIGILS
    public static final RegistryObject<AbstractSigilItem> LESSER_STRENGTH_SIGIL = ITEMS.register("lesser_strength_sigil",
            () -> new MortalPotionSigil(
                    new Item.Properties().tab(ItemGroup.TAB_MATERIALS),
                    Effects.DAMAGE_BOOST, // The effect
                    1000                   // The base duration in ticks
            ));
    //Ascended SIGILS
    public static final RegistryObject<AbstractSigilItem> SACRIFICE_SIGIL = ITEMS.register("sacrifice_sigil",
            () -> new AscendedSigilOfSacrifice(
                    new Item.Properties().tab(ItemGroup.TAB_MATERIALS),
                    Effects.DAMAGE_RESISTANCE, // The effect
                    6000                   // The base duration in ticks
            ));


    //CRAFTING ITEMS
    public static final RegistryObject<Item> BLANK_SIGIL = ITEMS.register("blank_sigil",
            () -> new Item(
                    new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
