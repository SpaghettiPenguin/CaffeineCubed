package com.caffeinecubed.coffee;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The syrups a Syrup Compactor makes, one from each ingredient it takes. A cup of coffee with a syrup gives a minute of
 * its effect, at level I.
 */
public enum Syrup implements StringRepresentable {
    PUFFERFISH("pufferfish", Items.PUFFERFISH, MobEffects.WATER_BREATHING),
    FERMENTED_SPIDER_EYE("fermented_spider_eye", Items.FERMENTED_SPIDER_EYE, MobEffects.INVISIBILITY),
    GOLDEN_CARROT("golden_carrot", Items.GOLDEN_CARROT, MobEffects.NIGHT_VISION),
    RABBIT_FOOT("rabbit_foot", Items.RABBIT_FOOT, MobEffects.JUMP);

    public static final Codec<Syrup> CODEC = StringRepresentable.fromEnum(Syrup::values);
    public static final StreamCodec<ByteBuf, Syrup> STREAM_CODEC = ByteBufCodecs.idMapper(id -> values()[id], Syrup::ordinal);

    private final String name;
    private final Item ingredient;
    private final Holder<MobEffect> effect;

    Syrup(String name, Item ingredient, Holder<MobEffect> effect) {
        this.name = name;
        this.ingredient = ingredient;
        this.effect = effect;
    }

    /**
     * The syrup made from this ingredient, or null if it isn't one.
     */
    @Nullable
    public static Syrup of(ItemStack ingredient) {
        for (Syrup syrup : values()) {
            if (ingredient.is(syrup.ingredient)) {
                return syrup;
            }
        }
        return null;
    }

    public Item ingredient() {
        return ingredient;
    }

    public Holder<MobEffect> effect() {
        return effect;
    }

    public Component displayName() {
        return Component.translatable("syrup.caffeinecubed." + name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
