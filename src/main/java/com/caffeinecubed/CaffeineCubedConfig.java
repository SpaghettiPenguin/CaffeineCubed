package com.caffeinecubed;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's settings, a server config: each world (or server) has its own copy, in serverconfig/caffeinecubed-server.toml,
 * and a server sends its settings to the players who join, so their machine screens, JEI pages and tooltips match.
 * Times are in ticks (20 ticks = 1 second); effect durations are in seconds, and 0 turns an effect off. The defaults are
 * the mod's own numbers.
 */
public final class CaffeineCubedConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue JUNGLE_GRASS_BEAN_CHANCE;

    public static final ModConfigSpec.IntValue ROASTING_TIME;
    public static final ModConfigSpec.IntValue HAND_GRINDING_TIME;
    public static final ModConfigSpec.IntValue BREWING_TIME;
    public static final ModConfigSpec.IntValue ATTACHMENT_GRINDING_TIME;
    public static final ModConfigSpec.IntValue SIPHONING_TIME;

    public static final ModConfigSpec.IntValue BASE_EFFECT_SECONDS_POOR;
    public static final ModConfigSpec.IntValue BASE_EFFECT_SECONDS_DECENT;
    public static final ModConfigSpec.IntValue BASE_EFFECT_SECONDS_GOOD;
    public static final ModConfigSpec.IntValue BASE_EFFECT_SECONDS_GREAT;
    public static final ModConfigSpec.IntValue ROAST_EFFECT_SECONDS;
    public static final ModConfigSpec.IntValue MILK_REGENERATION_SECONDS;
    public static final ModConfigSpec.IntValue SYRUP_EFFECT_SECONDS;
    public static final ModConfigSpec.IntValue GODS_COFFEE_SECONDS_POOR;
    public static final ModConfigSpec.IntValue GODS_COFFEE_SECONDS_DECENT;
    public static final ModConfigSpec.IntValue GODS_COFFEE_SECONDS_GOOD;
    public static final ModConfigSpec.IntValue GODS_COFFEE_SECONDS_GREAT;

    // The longest any time can be set to: an hour
    private static final int MAX_TICKS = 72000;
    private static final int MAX_SECONDS = 3600;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation(key("growing")).push("growing");
        JUNGLE_GRASS_BEAN_CHANCE = builder
                .comment("Chance that breaking short grass or a fern in a jungle drops a Raw Coffee Bean (0.03125 is 1 in 32)")
                .translation(key("jungleGrassBeanChance"))
                .defineInRange("jungleGrassBeanChance", 0.03125, 0.0, 1.0);
        builder.pop();

        builder.comment("How long the machines take, in ticks (20 ticks = 1 second)").translation(key("machines")).push("machines");
        ROASTING_TIME = time(builder, "roastingTime", 200,
                "Ticks the Coffee Roaster takes to roast one bean (a furnace takes 200). It only burns fuel while roasting, so one coal roasts 1600 divided by this many beans");
        HAND_GRINDING_TIME = time(builder, "handGrindingTime", 20, "Ticks the Coffee Grinder takes to grind one bean while right-click is held");
        BREWING_TIME = time(builder, "brewingTime", 400,
                "Ticks the Coffee Machine takes to brew one cup. It only burns fuel while brewing, so one coal brews 1600 divided by this many cups");
        ATTACHMENT_GRINDING_TIME = time(builder, "attachmentGrindingTime", 100, "Ticks the Grinder Attachment takes to grind one bean");
        SIPHONING_TIME = time(builder, "siphoningTime", 800, "Ticks the Triple Axis Siphon takes to make one thing");
        builder.pop();

        builder.comment("How long each drink's effects last, in seconds. 0 turns that effect off").translation(key("drinks")).push("drinks");
        BASE_EFFECT_SECONDS_POOR = seconds(builder, "baseEffectSecondsPoor", 60,
                "Seconds of Speed (or Fire Resistance, for Ice Coffee) a cup of Poor coffee gives");
        BASE_EFFECT_SECONDS_DECENT = seconds(builder, "baseEffectSecondsDecent", 120, "The same, for Decent coffee");
        BASE_EFFECT_SECONDS_GOOD = seconds(builder, "baseEffectSecondsGood", 180, "The same, for Good coffee");
        BASE_EFFECT_SECONDS_GREAT = seconds(builder, "baseEffectSecondsGreat", 240, "The same, for Great coffee");
        ROAST_EFFECT_SECONDS = seconds(builder, "roastEffectSeconds", 60,
                "Seconds of the roast's effect (Slow Falling, Haste or Strength) a cup gives");
        MILK_REGENERATION_SECONDS = seconds(builder, "milkRegenerationSeconds", 30, "Seconds of Regeneration a cup brewed with milk gives");
        SYRUP_EFFECT_SECONDS = seconds(builder, "syrupEffectSeconds", 60, "Seconds of its syrup's effect a cup brewed with syrup gives");
        GODS_COFFEE_SECONDS_POOR = seconds(builder, "godsCoffeeSecondsPoor", 30,
                "Seconds every effect of a Poor God's Coffee (or God's Ice Coffee) lasts");
        GODS_COFFEE_SECONDS_DECENT = seconds(builder, "godsCoffeeSecondsDecent", 60, "The same, for a Decent one");
        GODS_COFFEE_SECONDS_GOOD = seconds(builder, "godsCoffeeSecondsGood", 90, "The same, for a Good one");
        GODS_COFFEE_SECONDS_GREAT = seconds(builder, "godsCoffeeSecondsGreat", 120, "The same, for a Great one");
        builder.pop();

        SPEC = builder.build();
    }

    private CaffeineCubedConfig() {
    }

    /**
     * A setting's value, or its default before the config is loaded (such as before a world is opened).
     */
    public static int get(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.getAsInt() : value.getDefault();
    }

    public static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.getAsDouble() : value.getDefault();
    }

    private static ModConfigSpec.IntValue time(ModConfigSpec.Builder builder, String name, int ticks, String comment) {
        return builder.comment(comment).translation(key(name)).defineInRange(name, ticks, 1, MAX_TICKS);
    }

    private static ModConfigSpec.IntValue seconds(ModConfigSpec.Builder builder, String name, int seconds, String comment) {
        return builder.comment(comment).translation(key(name)).defineInRange(name, seconds, 0, MAX_SECONDS);
    }

    private static String key(String name) {
        return CaffeineCubed.MODID + ".configuration." + name;
    }
}
