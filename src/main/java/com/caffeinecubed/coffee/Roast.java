package com.caffeinecubed.coffee;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * How dark coffee was roasted. Beans and grounds are a different item for each roast; a cup of Coffee is one item that
 * remembers the roast of the grounds it was brewed from, which their brewing recipe names.
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

    public Component displayName() {
        return Component.translatable("roast.caffeinecubed." + name).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
