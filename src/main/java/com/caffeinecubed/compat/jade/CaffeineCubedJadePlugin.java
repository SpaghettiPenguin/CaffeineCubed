package com.caffeinecubed.compat.jade;

import com.caffeinecubed.block.CoffeeBushBlock;
import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.CoffeeMachineSideAttachmentBlock;
import com.caffeinecubed.block.CoffeeRoasterBlock;
import com.caffeinecubed.block.GrinderAttachmentBlock;
import com.caffeinecubed.block.TripleAxisSiphonBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.HideThingsExtensionProvider;

/**
 * Adds Caffeine Cubed to Jade, when it is installed: looking at a Coffee Roaster, Coffee Machine or Triple Axis Siphon
 * shows what's in it and how far along it is, like a furnace; looking at an attachment shows its part of the machine;
 * and looking at a Coffee Bush shows the quality of its beans (Jade already shows its growth).
 * Jade finds this class by its annotation; nothing else in the mod refers to it, so the mod runs fine without Jade.
 */
@WailaPlugin
public class CaffeineCubedJadePlugin implements IWailaPlugin {
    /**
     * The machines, and the attachments, which hoppers can fill the machine through.
     */
    private static final List<Class<?>> SHOWN_IN_A_ROW = List.of(CoffeeRoasterBlockEntity.class, CoffeeMachineBlockEntity.class,
            TripleAxisSiphonBlockEntity.class, GrinderAttachmentBlock.class, CoffeeMachineSideAttachmentBlock.class);

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MachineProvider.COFFEE_ROASTER, CoffeeRoasterBlockEntity.class);
        registration.registerBlockDataProvider(MachineProvider.COFFEE_MACHINE, CoffeeMachineBlockEntity.class);
        registration.registerBlockDataProvider(MachineProvider.TRIPLE_AXIS_SIPHON, TripleAxisSiphonBlockEntity.class);
        registration.registerBlockDataProvider(MachineProvider.COFFEE_MACHINE_ATTACHMENTS, GrinderAttachmentBlock.class);
        registration.registerBlockDataProvider(MachineProvider.COFFEE_MACHINE_ATTACHMENTS, CoffeeMachineSideAttachmentBlock.class);
        // As with a furnace, the row already shows what's in them, so Jade's usual list of a container's items is left out
        for (Class<?> target : SHOWN_IN_A_ROW) {
            registration.registerItemStorage(HideThingsExtensionProvider.<ItemStack, Object>instance(), target);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineProvider.COFFEE_ROASTER, CoffeeRoasterBlock.class);
        registration.registerBlockComponent(MachineProvider.COFFEE_MACHINE, CoffeeMachineBlock.class);
        registration.registerBlockComponent(MachineProvider.TRIPLE_AXIS_SIPHON, TripleAxisSiphonBlock.class);
        registration.registerBlockComponent(MachineProvider.COFFEE_MACHINE_ATTACHMENTS, GrinderAttachmentBlock.class);
        registration.registerBlockComponent(MachineProvider.COFFEE_MACHINE_ATTACHMENTS, CoffeeMachineSideAttachmentBlock.class);
        registration.registerBlockComponent(CoffeeBushProvider.INSTANCE, CoffeeBushBlock.class);
    }
}
