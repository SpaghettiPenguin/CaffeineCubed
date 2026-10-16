package com.caffeinecubed.coffee;

import com.caffeinecubed.registry.ModItems;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * How dark coffee was roasted. Beans and grounds are a different item for each roast; a cup of Coffee is one item that
 * remembers the roast of the grounds it was brewed from.
 */
public enum Roast implements StringRepresentable {
    LIGHT("light"),
    MEDIUM("medium"),
    DARK("dark");

    public static final Codec<Roast> CODEC = StringRepresentable.fromEnum(Roast::values);
    public static final StreamCodec<ByteBuf, Roast> STREAM_CODEC = ByteBufCodecs.idMapper(id -> values()[id], Roast::ordinal);

    private final String name;

    Roast(String name) {
        this.name = name;
    }

    /**
     * The roast of these roasted beans or grounds, or null for anything else (raw beans aren't roasted at all).
     */
    @Nullable
    public static Roast of(ItemStack coffee) {
        if (coffee.is(ModItems.LIGHT_ROAST_COFFEE_BEANS) || coffee.is(ModItems.LIGHT_ROAST_COFFEE_GROUNDS)) {
            return LIGHT;
        }
        if (coffee.is(ModItems.MEDIUM_ROAST_COFFEE_BEANS) || coffee.is(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS)) {
            return MEDIUM;
        }
        if (coffee.is(ModItems.DARK_ROAST_COFFEE_BEANS) || coffee.is(ModItems.DARK_ROAST_COFFEE_GROUNDS)) {
            return DARK;
        }
        return null;
    }

    public Component displayName() {
        return Component.translatable("roast.caffeinecubed." + name).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
