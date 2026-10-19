package com.caffeinecubed.compat.jade;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.BeanQuality;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows in Jade the quality of the beans a Coffee Bush grows, which comes from its biome, under Jade's own "Growth"
 * line. The player's game knows the biome, so this needs nothing from the server.
 */
public enum CoffeeBushProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "coffee_bush_quality");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BeanQuality quality = BeanQuality.at(accessor.getLevel(), accessor.getPosition());
        tooltip.add(Component.translatable("tooltip.caffeinecubed.bean_quality", quality.displayName()));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
