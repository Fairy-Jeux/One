package com.fairy.one.block.entity;

import com.fairy.one.One;
import com.fairy.one.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, One.MOD_ID);

    public static final RegistryObject<BlockEntityType<OrderOfTheSpaceMachineBlockEntity>> ORDER_OF_THE_SPACE_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("order_of_the_space_machine_block_entity", () ->
                    BlockEntityType.Builder.of(OrderOfTheSpaceMachineBlockEntity::new,
                            ModBlocks.ORDER_OF_THE_SPACE_MACHINE.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
