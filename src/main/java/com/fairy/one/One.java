package com.fairy.one;

import com.fairy.one.block.ModBlocks;
import com.fairy.one.block.entity.ModBlockEntities;
import com.fairy.one.block.menu.ModMenuTypes;
import com.fairy.one.block.menu.OrderOfTheSpaceMachineMenu;
import com.fairy.one.block.recipes.ModRecipes;
import com.fairy.one.block.screen.OrderOfTheSpaceMachineScreen;
import com.fairy.one.enchant.ModEnchantments;
import com.fairy.one.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(One.MOD_ID)
public class One {
    public static final String MOD_ID = "one";
    private static final Logger LOGGER = LogUtils.getLogger();

    public One()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(eventBus);

        ModItems.register(eventBus);

        ModEnchantments.register(eventBus);

        ModMenuTypes.register(eventBus);
        ModBlockEntities.register(eventBus);

        ModRecipes.register(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::ClientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("WELCOME TO ONe !");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void ClientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(ModMenuTypes.ORDER_OF_THE_SPACE_MACHINE_MENU.get(), OrderOfTheSpaceMachineScreen::new);
    }
}
