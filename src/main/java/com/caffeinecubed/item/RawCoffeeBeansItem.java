package com.caffeinecubed.item;

import com.caffeinecubed.coffee.BeanQuality;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * Raw Coffee Beans: plant them to grow a Coffee Bush. Each stack carries a {@link BeanQuality}, shown in its tooltip;
 * beans of different qualities don't stack together.
 */
public class RawCoffeeBeansItem extends ItemNameBlockItem {
    public RawCoffeeBeansItem(Block bush, Properties properties) {
        super(bush, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BeanQuality.addTooltip(stack, tooltip);
    }
}
