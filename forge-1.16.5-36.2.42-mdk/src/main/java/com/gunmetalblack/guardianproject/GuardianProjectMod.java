package com.gunmetalblack.guardianproject;

import com.gunmetalblack.guardianproject.common.capability.GuardianProjectCapabilities;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.GuardianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.GuardianPlayerDataHolderCapabilityProvider;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.GuardianPlayerDataHolderCapabilityStorage;
import com.gunmetalblack.guardianproject.common.capability.gaurdianplayerdataholder.IGaurdianPlayerDataHolderCapability;
import com.gunmetalblack.guardianproject.item.ModItems;
import com.gunmetalblack.guardianproject.item.custom.sigil.tools.AbstractSigilItem;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
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

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("guardian_project")
public class GuardianProjectMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "guardian_project";

    public GuardianProjectMod() {
        // Register the setup method for modloading
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(eventBus);
        com.gunmetalblack.guardianproject.common.ModEntities.ENTITIES.register(eventBus);
        com.gunmetalblack.guardianproject.common.MartyrdomJump.register();
        com.gunmetalblack.guardianproject.common.ModEffects.EFFECTS.register(eventBus);
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
        MinecraftForge.EVENT_BUS.addListener(GuardianProjectMod::onPlayerAttacked);
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
    public static void onPlayerAttacked(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (com.gunmetalblack.guardianproject.common.EywaDamageLedger.isInternalDamage()) return;
        if (event.getEntityLiving().level.isClientSide || !(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(player);
        if (data == null) return;
        for (AbstractSigilItem sigil : data.getActiveSigils().toArray(new AbstractSigilItem[0])) {
            sigil.onPlayerAttacked(player, event);
            if (event.isCanceled()) break;
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event)
    {
        if (com.gunmetalblack.guardianproject.common.EywaDamageLedger.isInternalDamage()) return;
        if (event.getEntityLiving().level.isClientSide) return;
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity victim = (PlayerEntity) event.getEntityLiving();
            IGaurdianPlayerDataHolderCapability data = GuardianProjectCapabilities.grabPlayerCapability(victim);
            if (data != null) for (AbstractSigilItem sigil : data.getActiveSigils().toArray(new AbstractSigilItem[0]))
                sigil.onPlayerTakeDamage(victim, event);
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof PlayerEntity)
        {
            //Grab Player then Cap ability
            IGaurdianPlayerDataHolderCapability playerdata = GuardianProjectCapabilities.grabPlayerCapability((PlayerEntity) attacker);
            if (playerdata == null) return;
            for(AbstractSigilItem sigil : playerdata.getActiveSigils().toArray(new AbstractSigilItem[0]))
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
                LOGGER.warn("Could not transfer guardian player data across death because a capability was missing.");
                return;
            }
            target.clearActiveEffects();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        //Grab Player then Cap ability
        IGaurdianPlayerDataHolderCapability playerdata = GuardianProjectCapabilities.grabPlayerCapability(event.player);
        if(playerdata == null)
        {
            LOGGER.warn("Player capability missing during tick for {}", event.player.getGameProfile().getName());
            return;
        }
        for (AbstractSigilItem sigil : playerdata.getActiveSigils().toArray(new AbstractSigilItem[0])) {
               sigil.onPlayerTick(event.player);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEventEntity(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof PlayerEntity) {
            GuardianPlayerDataHolderCapabilityProvider provider = new GuardianPlayerDataHolderCapabilityProvider();
            event.addCapability(new ResourceLocation(GuardianProjectMod.MOD_ID, "guardian_player_data_holder"), provider);
            event.addListener(provider::invalidate);
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
