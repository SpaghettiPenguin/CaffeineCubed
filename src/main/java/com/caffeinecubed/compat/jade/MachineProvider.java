package com.caffeinecubed.compat.jade;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.SyrupCompactorBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.Syrup;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/**
 * Shows what a machine is doing in Jade, the way Jade shows a furnace: what's in it and an arrow filling up as it works
 * (see {@link MachineSteps}). The server reads the machine and sends the row to the player looking at it. Each can be
 * turned off in Jade's settings.
 */
public enum MachineProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, List<MachineSteps.Step>> {
    COFFEE_ROASTER("coffee_roaster", accessor -> accessor.getBlockEntity() instanceof CoffeeRoasterBlockEntity roaster ? MachineSteps.of(roaster) : List.of()),
    COFFEE_MACHINE("coffee_machine", accessor -> accessor.getBlockEntity() instanceof CoffeeMachineBlockEntity machine ? MachineSteps.of(machine) : List.of()),
    TRIPLE_AXIS_SIPHON("triple_axis_siphon",
            accessor -> accessor.getBlockEntity() instanceof TripleAxisSiphonBlockEntity siphon ? MachineSteps.of(siphon) : List.of()),
    COFFEE_MACHINE_ATTACHMENTS("coffee_machine_attachments",
            accessor -> MachineSteps.ofAttachment(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState()));

    private final ResourceLocation uid;
    private final Function<BlockAccessor, List<MachineSteps.Step>> steps;

    MachineProvider(String name, Function<BlockAccessor, List<MachineSteps.Step>> steps) {
        uid = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, name);
        this.steps = steps;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        List<MachineSteps.Step> row = decodeFromData(accessor).orElse(List.of());
        if (row.isEmpty()) {
            return;
        }
        IElementHelper helper = IElementHelper.get();
        boolean lineStarted = false;
        for (MachineSteps.Step step : row) {
            for (ItemStack item : step.items()) {
                lineStarted = add(tooltip, helper.item(item), lineStarted);
            }
            if (step.hasArrow()) {
                // Spaced like Jade's furnace arrow
                lineStarted = add(tooltip, helper.spacer(4, 0), lineStarted);
                tooltip.append(helper.progress(step.progress()).translate(new Vec2(-2, 0)));
            }
        }
        // A syrup ingredient on its own doesn't say which syrup it makes
        if (accessor.getBlock() instanceof SyrupCompactorBlock && !row.getFirst().items().isEmpty()) {
            Syrup syrup = Syrup.of(row.getFirst().items().getFirst());
            if (syrup != null) {
                tooltip.append(helper.spacer(2, 0));
                tooltip.append(helper.text(syrup.displayName()).translate(new Vec2(0, 5)));
            }
        }
    }

    /**
     * Starts the row on a new line with the first element, and adds the rest after it.
     */
    private static boolean add(ITooltip tooltip, IElement element, boolean lineStarted) {
        if (lineStarted) {
            tooltip.append(element);
        } else {
            tooltip.add(element);
        }
        return true;
    }

    @Override
    public List<MachineSteps.Step> streamData(BlockAccessor accessor) {
        List<MachineSteps.Step> row = steps.apply(accessor);
        // Nothing to send for an empty machine
        return row.isEmpty() ? null : row;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, List<MachineSteps.Step>> streamCodec() {
        return MachineSteps.Step.LIST_STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }
}
