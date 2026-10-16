package com.caffeinecubed.item;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.CoffeeEffects;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * God's Coffee or God's Ice Coffee, made in a Triple Axis Siphon. Drunk like any other coffee, it gives its base effect
 * (Speed II or Fire Resistance I) and Regeneration, Night Vision, Resistance, Strength and Haste, all for as long as
 * the quality of its Rainbow Blend allows (see {@link CoffeeEffects#godsCoffee}). There is no roast, milk or syrup.
 */
public class GodsCoffeeItem extends CupOfCoffeeItem {
    private final int baseAmplifier;

    public GodsCoffeeItem(Holder<MobEffect> baseEffect, int baseAmplifier, Properties properties) {
        super(baseEffect, properties);
        this.baseAmplifier = baseAmplifier;
    }

    @Override
    public List<MobEffectInstance> getEffects(ItemStack coffee) {
        return CoffeeEffects.godsCoffee(getBaseEffect(), baseAmplifier, BeanQuality.of(coffee));
    }

    @Override
    public void appendHoverText(ItemStack coffee, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BeanQuality.addTooltip(coffee, tooltip);
        // The effects, listed the way a potion lists them
        PotionContents.addPotionTooltip(getEffects(coffee), tooltip::add, 1.0F, context.tickRate());
    }
}
