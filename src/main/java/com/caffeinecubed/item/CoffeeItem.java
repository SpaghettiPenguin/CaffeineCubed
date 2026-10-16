package com.caffeinecubed.item;

import com.caffeinecubed.coffee.BeanQuality;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Coffee made from beans: the roasted beans from a Coffee Roaster and the grounds from a Coffee Grinder. Each keeps
 * the {@link BeanQuality} of the beans it was made from, shown in the tooltip.
 */
public class CoffeeItem extends Item {
    public CoffeeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BeanQuality.addTooltip(stack, tooltip);
    }
}
