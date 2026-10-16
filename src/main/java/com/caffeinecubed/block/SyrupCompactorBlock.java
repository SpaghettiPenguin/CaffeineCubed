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
 * An attachment for the Coffee Machine that presses ingredients into syrups for its coffee. It goes on the machine's
 * left side, as seen from the front. The model is drawn facing north, with the connectors reaching west into the machine.
 */
public class SyrupCompactorBlock extends CoffeeMachineSideAttachmentBlock {
    public static final MapCodec<SyrupCompactorBlock> CODEC = simpleCodec(SyrupCompactorBlock::new);
    /**
     * The outline of the model with its front to the north: the base, the back and the four syrup bottles with their caps.
     * The connectors that reach into the machine are left out, since they stick out of the block.
     */
    private static final Map<Direction, VoxelShape> SHAPES = HorizontalShapes.turned(Shapes.or(
            Block.box(1, 0, 1, 15, 2, 15),
            Block.box(1, 2, 11, 15, 14, 15),
            Block.box(3, 2, 6, 7, 11, 10),
            Block.box(4, 11, 7, 6, 12, 9),
            Block.box(9, 2, 6, 13, 11, 10),
            Block.box(10, 11, 7, 12, 12, 9),
            Block.box(3, 2, 2, 7, 7, 6),
            Block.box(4, 7, 3, 6, 8, 5),
            Block.box(9, 2, 2, 13, 7, 6),
            Block.box(10, 7, 3, 12, 8, 5)), Direction.NORTH);

    public SyrupCompactorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SyrupCompactorBlock> codec() {
        return CODEC;
    }

    /**
     * The machine is on the compactor's right, since the compactor is on the machine's left.
     */
    @Override
    public Direction towardsMachine(Direction facing) {
        return facing.getCounterClockWise();
    }

    @Override
    protected Map<Direction, VoxelShape> shapes() {
        return SHAPES;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.caffeinecubed.syrup_compactor").withStyle(ChatFormatting.GRAY));
    }
}
