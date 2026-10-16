package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CaffeineCubed.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoffeeRoasterBlockEntity>> COFFEE_ROASTER =
            BLOCK_ENTITY_TYPES.register("coffee_roaster", () -> BlockEntityType.Builder
                    .of(CoffeeRoasterBlockEntity::new, ModBlocks.COFFEE_ROASTER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoffeeMachineBlockEntity>> COFFEE_MACHINE =
            BLOCK_ENTITY_TYPES.register("coffee_machine", () -> BlockEntityType.Builder
                    .of(CoffeeMachineBlockEntity::new, ModBlocks.COFFEE_MACHINE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TripleAxisSiphonBlockEntity>> TRIPLE_AXIS_SIPHON =
            BLOCK_ENTITY_TYPES.register("triple_axis_siphon", () -> BlockEntityType.Builder
                    .of(TripleAxisSiphonBlockEntity::new, ModBlocks.TRIPLE_AXIS_SIPHON.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
