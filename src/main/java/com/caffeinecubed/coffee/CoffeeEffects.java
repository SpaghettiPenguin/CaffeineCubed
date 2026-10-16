package com.caffeinecubed.coffee;

import com.caffeinecubed.CaffeineCubedConfig;
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
 * its own effect. Those are the defaults: every duration can be changed in the config (see CaffeineCubedConfig).
 */
public final class CoffeeEffects {
    private static final int TICKS_PER_SECOND = 20;

    private CoffeeEffects() {
    }

    /**
     * How long all of God's Coffee's effects last, by the quality of its Rainbow Blend: 30 seconds for poor, 1 minute
     * for decent, 1:30 for good and 2 minutes for great.
     */
    public static int godsCoffeeSeconds(BeanQuality quality) {
        return CaffeineCubedConfig.get(switch (quality) {
            case POOR -> CaffeineCubedConfig.GODS_COFFEE_SECONDS_POOR;
            case DECENT -> CaffeineCubedConfig.GODS_COFFEE_SECONDS_DECENT;
            case GOOD -> CaffeineCubedConfig.GODS_COFFEE_SECONDS_GOOD;
            case GREAT -> CaffeineCubedConfig.GODS_COFFEE_SECONDS_GREAT;
        });
    }

    /**
     * The effects of God's Coffee: its base effect at the given amplifier (Speed II for God's Coffee, Fire Resistance I
     * for God's Ice Coffee), and Regeneration, Night Vision, Resistance, Strength and Haste at level I, all for as long
     * as the quality allows.
     */
    public static List<MobEffectInstance> godsCoffee(Holder<MobEffect> base, int baseAmplifier, BeanQuality quality) {
        int seconds = godsCoffeeSeconds(quality);
        List<MobEffectInstance> effects = new ArrayList<>();
        add(effects, base, seconds, baseAmplifier);
        for (Holder<MobEffect> effect : List.of(MobEffects.REGENERATION, MobEffects.NIGHT_VISION, MobEffects.DAMAGE_RESISTANCE,
                MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED)) {
            add(effects, effect, seconds, 0);
        }
        return effects;
    }

    /**
     * How many seconds the base effect lasts, by the beans' quality.
     */
    public static int baseEffectSeconds(BeanQuality quality) {
        return CaffeineCubedConfig.get(switch (quality) {
            case POOR -> CaffeineCubedConfig.BASE_EFFECT_SECONDS_POOR;
            case DECENT -> CaffeineCubedConfig.BASE_EFFECT_SECONDS_DECENT;
            case GOOD -> CaffeineCubedConfig.BASE_EFFECT_SECONDS_GOOD;
            case GREAT -> CaffeineCubedConfig.BASE_EFFECT_SECONDS_GREAT;
        });
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
        add(effects, base, baseEffectSeconds(quality), 0);
        add(effects, roastEffect(roast), CaffeineCubedConfig.get(CaffeineCubedConfig.ROAST_EFFECT_SECONDS), 0);
        if (milk) {
            add(effects, MobEffects.REGENERATION, CaffeineCubedConfig.get(CaffeineCubedConfig.MILK_REGENERATION_SECONDS), 0);
        }
        if (syrup != null) {
            add(effects, syrup.effect(), CaffeineCubedConfig.get(CaffeineCubedConfig.SYRUP_EFFECT_SECONDS), 0);
        }
        return effects;
    }

    /**
     * Adds an effect lasting this many seconds; none at all if it has been set to 0 in the config.
     */
    private static void add(List<MobEffectInstance> effects, Holder<MobEffect> effect, int seconds, int amplifier) {
        if (seconds > 0) {
            effects.add(new MobEffectInstance(effect, seconds * TICKS_PER_SECOND, amplifier));
        }
    }
}
