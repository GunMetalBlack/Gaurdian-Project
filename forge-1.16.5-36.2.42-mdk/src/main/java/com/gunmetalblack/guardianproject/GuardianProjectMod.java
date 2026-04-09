package com.gunmetalblack.guardianproject;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.GuardianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.GuardianPlayerDataHolderCapabilityProvider;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.GuardianPlayerDataHolderCapabilityStorage;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.ModItems;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("guardian_project")
public class GuardianProjectMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static String MOD_ID = "guardian_project";

    public GuardianProjectMod() {
        // Register the setup method for modloading
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(eventBus);
        eventBus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        eventBus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        eventBus.addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        eventBus.addListener(this::doClientStuff);
        eventBus.addListener(GuardianProjectMod::onCommonSetupEvent);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class,GuardianProjectMod::onAttachCapabilitiesEventEntity);
        MinecraftForge.EVENT_BUS.addListener(GuardianProjectMod::onPlayerAttack);
        MinecraftForge.EVENT_BUS.addListener(GuardianProjectMod::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(GuardianProjectMod::onPlayerClone);

    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public static void onCommonSetupEvent(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(IGaurdianPlayerDataHolderCapability.class, new GuardianPlayerDataHolderCapabilityStorage(), GuardianPlayerDataHolderCapability::new);
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event)
    {
        Entity attacker = event.getSource().getDirectEntity();
        if (attacker instanceof PlayerEntity)
        {
            //Grab Player then Cap ability
            IGaurdianPlayerDataHolderCapability playerdata = GuardianProjectCapabilities.grabPlayerCapability((PlayerEntity) attacker);
            for(AbstractSigilItem sigil : playerdata.getActiveSigils())
            {
                //Call all the Sigil Abilities
                sigil.onPlayerDealDamage((PlayerEntity) attacker, event);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.isWasDeath()) {
            IGaurdianPlayerDataHolderCapability original = event.getOriginal().getCapability(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER).orElse(null);
            IGaurdianPlayerDataHolderCapability target = event.getPlayer().getCapability(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER).orElse(null);
            if(original == null || target == null) {
                System.out.println("Oopsie doopsie, I made a fuckie wucky! Got null capability when attempting to persist Phlax player data across death.");
                return;
            }
            INBT originalNBT = GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage().writeNBT(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER, original, null);
            GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER.getStorage().readNBT(GuardianProjectCapabilities.GUARDIAN_PLAYER_DATA_HELPER, target, null, originalNBT);
            target.setCurrentlyAppliedMaxHealthOffset(0);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        //Grab Player then Cap ability
        IGaurdianPlayerDataHolderCapability playerdata = GuardianProjectCapabilities.grabPlayerCapability(event.player);
        if(playerdata == null)
        {
            System.out.println("YOUR PLAYER DATA IS FUCKING NULL");
            return;
        }
        for (AbstractSigilItem sigil : playerdata.getActiveSigils().toArray(new AbstractSigilItem[0])) {
               sigil.onPlayerTick(event.player);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEventEntity(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof PlayerEntity) {
            event.addCapability(new ResourceLocation(GuardianProjectMod.MOD_ID, "guardian_player_data_holder"), new GuardianPlayerDataHolderCapabilityProvider());
        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
