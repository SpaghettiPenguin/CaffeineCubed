package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.coffee.Syrup;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CaffeineCubed.MODID);

    /**
     * How good a stack of coffee beans is. Beans start out decent.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BeanQuality>> BEAN_QUALITY =
            DATA_COMPONENTS.registerComponentType("bean_quality", builder -> builder
                    .persistent(BeanQuality.CODEC)
                    .networkSynchronized(BeanQuality.STREAM_CODEC));

    /**
     * The roast of the grounds a cup of Coffee was brewed from. Cups start out medium roast.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Roast>> ROAST =
            DATA_COMPONENTS.registerComponentType("roast", builder -> builder
                    .persistent(Roast.CODEC)
                    .networkSynchronized(Roast.STREAM_CODEC));

    /**
     * Marks a cup of Coffee brewed with milk from a Milk Frother.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> MILK =
            DATA_COMPONENTS.registerComponentType("milk", builder -> builder
                    .persistent(Codec.unit(Unit.INSTANCE))
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    /**
     * The syrup a cup of Coffee was brewed with, from a Syrup Compactor. Cups without one have none.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Syrup>> SYRUP =
            DATA_COMPONENTS.registerComponentType("syrup", builder -> builder
                    .persistent(Syrup.CODEC)
                    .networkSynchronized(Syrup.STREAM_CODEC));

    private ModDataComponents() {
    }
}
