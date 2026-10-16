package com.caffeinecubed.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An attachment for the Coffee Machine that froths milk into its coffee. It goes on the machine's right side, as seen
 * from the front. The model is drawn facing north, with the connectors reaching east into the machine.
 */
public class MilkFrotherBlock extends CoffeeMachineSideAttachmentBlock {
    public static final MapCodec<MilkFrotherBlock> CODEC = simpleCodec(MilkFrotherBlock::new);
    /**
     * The outline of the model with its front to the north: the base, the back, the milk jug and the steam wand.
     * The connectors that reach into the machine are left out, since they stick out of the block.
     */
    private static final Map<Direction, VoxelShape> SHAPES = HorizontalShapes.turned(Shapes.or(
            Block.box(1, 0, 1, 15, 2, 15),
            Block.box(1, 2, 11, 15, 14, 15),
            Block.box(5, 2, 4, 11, 11, 9),
            Block.box(5, 11, 5, 11, 13, 8),
            Block.box(6, 14, 11, 10, 15, 15),
            Block.box(7, 13, 6, 9, 16, 14)), Direction.NORTH);

    public MilkFrotherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<MilkFrotherBlock> codec() {
        return CODEC;
    }

    /**
     * The machine is on the frother's left, since the frother is on the machine's right.
     */
    @Override
    public Direction towardsMachine(Direction facing) {
        return facing.getClockWise();
    }

    @Override
    protected Map<Direction, VoxelShape> shapes() {
        return SHAPES;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.caffeinecubed.milk_frother").withStyle(ChatFormatting.GRAY));
    }
}
