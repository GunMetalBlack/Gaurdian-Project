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

    public static final ItemGroup SIGILS_TAB = new ItemGroup("guardian_project_sigils") {
        @Override
        public net.minecraft.item.ItemStack makeIcon() {
            return new net.minecraft.item.ItemStack(ModItems.SACRIFICE_SIGIL.get());
        }
    };

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GuardianProjectMod.MOD_ID);
    //MORTAL SIGILS
    public static final RegistryObject<AbstractSigilItem> DESTRUCTION_SIGIL = ITEMS.register("destruction_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.SigilOfDestruction(
                    new Item.Properties().tab(SIGILS_TAB)));
    public static final RegistryObject<AbstractSigilItem> VIGOR_SIGIL = ITEMS.register("vigor_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.SigilOfVigor(
                    new Item.Properties().tab(SIGILS_TAB)));
    public static final RegistryObject<AbstractSigilItem> HEALING_SIGIL = ITEMS.register("healing_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.MortalHealingSigil(
                    new Item.Properties().tab(SIGILS_TAB)));

    public static final RegistryObject<AbstractSigilItem> LESSER_STRENGTH_SIGIL = ITEMS.register("lesser_strength_sigil",
            () -> new MortalPotionSigil(
                    new Item.Properties().tab(SIGILS_TAB),
                    Effects.DAMAGE_BOOST, // The effect
                    1000                   // The base duration in ticks
            ));
    //Ascended SIGILS
    public static final RegistryObject<AbstractSigilItem> DIVINE_EYWA_SIGIL = ITEMS.register("divine_eywa_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.SigilOfDivineEywa(
                    new Item.Properties().tab(SIGILS_TAB)));
    public static final RegistryObject<AbstractSigilItem> CALAMITY_SIGIL = ITEMS.register("calamity_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.SigilOfCalamity(
                    new Item.Properties().tab(SIGILS_TAB)));
    public static final RegistryObject<AbstractSigilItem> TRAGEDY_SIGIL = ITEMS.register("tragedy_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.SigilOfTragedy(
                    new Item.Properties().tab(SIGILS_TAB)));
    public static final RegistryObject<AbstractSigilItem> SACRIFICE_SIGIL = ITEMS.register("sacrifice_sigil",
            () -> new AscendedSigilOfSacrifice(
                    new Item.Properties().tab(SIGILS_TAB),
                    Effects.DAMAGE_RESISTANCE, // The effect
                    3000                   // The base duration in ticks
            ));


    //CRAFTING ITEMS
    public static final RegistryObject<AbstractSigilItem> MARTYRDOM_SIGIL = ITEMS.register("martyrdom_sigil",
            () -> new com.gunmetalblack.guardianproject.item.custom.sigil.SigilOfMartyrdom(
                    new Item.Properties().tab(SIGILS_TAB)));

    public static final RegistryObject<Item> BLANK_SIGIL = ITEMS.register("blank_sigil",
            () -> new Item(
                    new Item.Properties().tab(SIGILS_TAB)));

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
