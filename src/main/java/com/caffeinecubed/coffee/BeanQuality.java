package com.caffeinecubed.coffee;

import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModTags;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

/**
 * How good a coffee bean is, set by the biome its bush grew in: cold biomes grow poor beans, savannas good ones
 * and jungles great ones. Every other biome grows decent beans. The biome tags in {@link ModTags.Biomes} decide which
 * biome is which, so data packs can sort modded biomes too.
 */
public enum BeanQuality implements StringRepresentable {
    POOR("poor", ChatFormatting.RED),
    DECENT("decent", ChatFormatting.WHITE),
    GOOD("good", ChatFormatting.GREEN),
    GREAT("great", ChatFormatting.GOLD);

    public static final Codec<BeanQuality> CODEC = StringRepresentable.fromEnum(BeanQuality::values);
    public static final StreamCodec<ByteBuf, BeanQuality> STREAM_CODEC = ByteBufCodecs.idMapper(id -> values()[id], BeanQuality::ordinal);

    private final String name;
    private final ChatFormatting color;

    BeanQuality(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }

    /**
     * The quality of beans grown in this biome. A biome in more than one quality tag gets the best of them.
     */
    public static BeanQuality of(Holder<Biome> biome) {
        if (biome.is(ModTags.Biomes.GROWS_GREAT_COFFEE)) {
            return GREAT;
        }
        if (biome.is(ModTags.Biomes.GROWS_GOOD_COFFEE)) {
            return GOOD;
        }
        if (biome.is(ModTags.Biomes.GROWS_POOR_COFFEE)) {
            return POOR;
        }
        return DECENT;
    }

    /**
     * The quality of a stack of beans. Beans without one are decent.
     */
    public static BeanQuality of(ItemStack beans) {
        return beans.getOrDefault(ModDataComponents.BEAN_QUALITY.get(), DECENT);
    }

    /**
     * Adds the "Quality: Great" line to a bean item's tooltip.
     */
    public static void addTooltip(ItemStack beans, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.caffeinecubed.bean_quality", of(beans).displayName()).withStyle(ChatFormatting.GRAY));
    }

    /**
     * The quality of beans grown at this spot.
     */
    public static BeanQuality at(LevelReader level, BlockPos pos) {
        return of(level.getBiome(pos));
    }

    public Component displayName() {
        return Component.translatable("bean_quality.caffeinecubed." + name).withStyle(color);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
