package com.caffeinecubed.registry;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.caffeinecubed.block.CoffeeMachineSideAttachmentBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class ModCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        // Lets item pipes from other mods use the Coffee Roaster, Coffee Machine and Triple Axis Siphon the way hoppers do
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COFFEE_ROASTER.get(),
                (roaster, side) -> side == null ? new InvWrapper(roaster) : new SidedInvWrapper(roaster, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COFFEE_MACHINE.get(),
                (machine, side) -> side == null ? new InvWrapper(machine) : new SidedInvWrapper(machine, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TRIPLE_AXIS_SIPHON.get(),
                (siphon, side) -> side == null ? new InvWrapper(siphon) : new SidedInvWrapper(siphon, side));
        // A hopper or pipe feeding a Grinder Attachment fills the bean slot of the machine underneath
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) ->
                level.getBlockEntity(pos.below()) instanceof CoffeeMachineBlockEntity machine
                        ? new RangedWrapper(new InvWrapper(machine), CoffeeMachineBlockEntity.BEANS, CoffeeMachineBlockEntity.BEANS + 1)
                        : null, ModBlocks.GRINDER_ATTACHMENT.get());
        // A hopper or pipe feeding a Milk Frother or Syrup Compactor fills its slot in the machine beside it
        registerSideAttachment(event, ModBlocks.MILK_FROTHER.get(), CoffeeMachineBlockEntity.MILK);
        registerSideAttachment(event, ModBlocks.SYRUP_COMPACTOR.get(), CoffeeMachineBlockEntity.SYRUP);
    }

    private static void registerSideAttachment(RegisterCapabilitiesEvent event, CoffeeMachineSideAttachmentBlock attachment, int slot) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) ->
                level.getBlockEntity(pos.relative(attachment.towardsMachine(state.getValue(CoffeeMachineSideAttachmentBlock.FACING))))
                        instanceof CoffeeMachineBlockEntity machine
                        ? new RangedWrapper(new InvWrapper(machine), slot, slot + 1)
                        : null, attachment);
    }

    private ModCapabilities() {
    }
}
