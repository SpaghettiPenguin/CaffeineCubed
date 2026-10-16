package com.caffeinecubed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * How the Coffee Grinder moves in first person while grinding: held where it normally is, rocking gently in a circle
 * once per turn of the crank. The grinder uses the brush's animation for its third-person arm pose, but in first person
 * the brush's sweep would tip it upside down.
 */
public class CoffeeGrinderHandAnimation implements IClientItemExtensions {
    /**
     * Ticks per turn of the crank, matching the grinding sound.
     */
    private static final float TURN_TICKS = 10;

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
                                           float partialTick, float equipProcess, float swingProcess) {
        if (!player.isUsingItem() || player.getUseItem() != itemInHand) {
            return false;
        }
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        // Where a held item normally sits
        poseStack.translate(side * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
        // Then a small circle and a slight tilt as the crank goes round
        float turn = (player.getTicksUsingItem() + partialTick) / TURN_TICKS * Mth.TWO_PI;
        poseStack.translate(side * 0.025F * Mth.cos(turn), 0.025F * Mth.sin(turn), 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 4 * Mth.sin(turn)));
        return true;
    }
}
