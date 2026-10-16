package com.caffeinecubed.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Hitboxes for blocks that face the player who placed them, like the Coffee Roaster and Coffee Machine. Their models
 * are drawn facing one way and the blockstate turns them; this turns the hitbox the same way.
 */
public final class HorizontalShapes {
    private HorizontalShapes() {
    }

    /**
     * The shape facing each horizontal direction.
     *
     * @param shape  the hitbox as drawn in the model
     * @param facing the way the front of the model faces, as drawn
     */
    public static Map<Direction, VoxelShape> turned(VoxelShape shape, Direction facing) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        // Each quarter turn clockwise turns north to east, east to south and so on
        for (int i = 0; i < 4; i++) {
            shapes.put(facing, shape);
            shape = turnClockwise(shape);
            facing = facing.getClockWise();
        }
        return shapes;
    }

    private static VoxelShape turnClockwise(VoxelShape shape) {
        VoxelShape[] turned = {Shapes.empty()};
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                turned[0] = Shapes.or(turned[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
        return turned[0];
    }
}
