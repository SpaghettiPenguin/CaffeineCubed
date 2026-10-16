package com.caffeinecubed.coffee;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * What drinking a cup of Coffee or Ice Coffee does. Every cup gives its base effect at level I, Speed for Coffee and
 * Fire Resistance for Ice Coffee, for longer the better the beans were: 1 minute for poor, 2 for decent, 3 for good and
 * 4 for great. The roast adds a minute of a second effect: Slow Falling for light, Haste for medium and Strength for
 * dark. Milk from a Milk Frother adds 30 seconds of Regeneration I, and a syrup from a Syrup Compactor adds a minute of
 * its own effect.
 */
public final class CoffeeEffects {
    private static final int TICKS_PER_MINUTE = 60 * 20;
    private static final int ROAST_EFFECT_MINUTES = 1;
    private static final int MILK_REGENERATION_SECONDS = 30;
    private static final int SYRUP_EFFECT_MINUTES = 1;

    private CoffeeEffects() {
    }

    /**
     * How long all of God's Coffee's effects last, by the quality of its Rainbow Blend: 30 seconds for poor, 1 minute
     * for decent, 1:30 for good and 2 minutes for great.
     */
    public static int godsCoffeeSeconds(BeanQuality quality) {
        return switch (quality) {
            case POOR -> 30;
            case DECENT -> 60;
            case GOOD -> 90;
            case GREAT -> 120;
        };
    }

    /**
     * The effects of God's Coffee: its base effect at the given amplifier (Speed II for God's Coffee, Fire Resistance I
     * for God's Ice Coffee), and Regeneration, Night Vision, Resistance, Strength and Haste at level I, all for as long
     * as the quality allows.
     */
    public static List<MobEffectInstance> godsCoffee(Holder<MobEffect> base, int baseAmplifier, BeanQuality quality) {
        int ticks = godsCoffeeSeconds(quality) * 20;
        List<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(base, ticks, baseAmplifier));
        for (Holder<MobEffect> effect : List.of(MobEffects.REGENERATION, MobEffects.NIGHT_VISION, MobEffects.DAMAGE_RESISTANCE,
                MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED)) {
            effects.add(new MobEffectInstance(effect, ticks));
        }
        return effects;
    }

    /**
     * How long the base effect lasts, by the beans' quality.
     */
    public static int baseEffectMinutes(BeanQuality quality) {
        return switch (quality) {
            case POOR -> 1;
            case DECENT -> 2;
            case GOOD -> 3;
            case GREAT -> 4;
        };
    }

    public static Holder<MobEffect> roastEffect(Roast roast) {
        return switch (roast) {
            case LIGHT -> MobEffects.SLOW_FALLING;
            case MEDIUM -> MobEffects.DIG_SPEED;
            case DARK -> MobEffects.DAMAGE_BOOST;
        };
    }

    /**
     * The effects of a cup with this base effect, brewed from beans of this quality and roast, with or without milk and a
     * syrup, all at level I.
     */
    public static List<MobEffectInstance> of(Holder<MobEffect> base, BeanQuality quality, Roast roast, boolean milk, @Nullable Syrup syrup) {
        List<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(base, baseEffectMinutes(quality) * TICKS_PER_MINUTE));
        effects.add(new MobEffectInstance(roastEffect(roast), ROAST_EFFECT_MINUTES * TICKS_PER_MINUTE));
        if (milk) {
            effects.add(new MobEffectInstance(MobEffects.REGENERATION, MILK_REGENERATION_SECONDS * 20));
        }
        if (syrup != null) {
            effects.add(new MobEffectInstance(syrup.effect(), SYRUP_EFFECT_MINUTES * TICKS_PER_MINUTE));
        }
        return effects;
    }
}
