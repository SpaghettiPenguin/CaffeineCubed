package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.item.CupOfCoffeeItem;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class CoffeeDrinkingTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final int MINUTE = 60 * 20;

    @GameTest(template = EMPTY)
    public static void mediumRoastDecentCoffeeGivesTwoMinutesOfSpeedAndOneOfHaste(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        drink(helper, player, coffee(Roast.MEDIUM, BeanQuality.DECENT, 1));
        assertEffect(helper, player, MobEffects.MOVEMENT_SPEED, 2 * MINUTE);
        assertEffect(helper, player, MobEffects.DIG_SPEED, MINUTE);
        helper.assertTrue(player.getActiveEffects().size() == 2, "Expected only Speed and Haste, but got " + player.getActiveEffects());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void betterBeansGiveLongerSpeed(GameTestHelper helper) {
        Map<BeanQuality, Integer> minutes = Map.of(BeanQuality.POOR, 1, BeanQuality.DECENT, 2, BeanQuality.GOOD, 3, BeanQuality.GREAT, 4);
        for (BeanQuality quality : BeanQuality.values()) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            drink(helper, player, coffee(Roast.DARK, quality, 1));
            assertEffect(helper, player, MobEffects.MOVEMENT_SPEED, minutes.get(quality) * MINUTE);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void eachRoastAddsAMinuteOfItsOwnEffect(GameTestHelper helper) {
        Map<Roast, Holder<MobEffect>> effects = Map.of(
                Roast.LIGHT, MobEffects.SLOW_FALLING, Roast.MEDIUM, MobEffects.DIG_SPEED, Roast.DARK, MobEffects.DAMAGE_BOOST);
        for (Roast roast : Roast.values()) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            drink(helper, player, coffee(roast, BeanQuality.GREAT, 1));
            assertEffect(helper, player, effects.get(roast), MINUTE);
            for (Map.Entry<Roast, Holder<MobEffect>> other : effects.entrySet()) {
                if (other.getKey() != roast) {
                    helper.assertFalse(player.hasEffect(other.getValue()), roast + " roast shouldn't give " + other.getValue().getRegisteredName());
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drinkingUsesUpTheCup(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack cups = coffee(Roast.LIGHT, BeanQuality.GOOD, 2);
        ItemStack left = drink(helper, player, cups);
        helper.assertTrue(left.is(ModItems.COFFEE.get()) && left.getCount() == 1, "One of the two cups should be left, but have " + left);
        ItemStack last = drink(helper, player, left);
        helper.assertTrue(last.isEmpty(), "Drinking the last cup should leave nothing in hand, but left " + last);
        helper.assertTrue(player.getInventory().isEmpty(), "No empty cup or anything else should be handed back");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drinkingTakesAsLongAsAPotion(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack coffee = coffee(Roast.MEDIUM, BeanQuality.DECENT, 1);
        helper.assertTrue(coffee.getUseDuration(player) == 32, "Drinking should take 32 ticks, like a potion");
        helper.assertTrue(coffee.getUseAnimation() == UseAnim.DRINK, "Coffee should be drunk, not eaten");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theMachineBrewsCoffeeOfTheGroundsRoast(GameTestHelper helper) {
        Map<Item, Roast> grounds = Map.of(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), Roast.LIGHT,
                ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), Roast.MEDIUM, ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), Roast.DARK);
        for (Map.Entry<Item, Roast> entry : grounds.entrySet()) {
            ItemStack ground = new ItemStack(entry.getKey());
            ground.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.POOR);
            ItemStack coffee = CoffeeMachineBlockEntity.brew(helper.getLevel(), ground, false, false, null);
            helper.assertTrue(CupOfCoffeeItem.getRoast(coffee) == entry.getValue() && BeanQuality.of(coffee) == BeanQuality.POOR,
                    entry.getKey() + " should brew poor " + entry.getValue() + " roast coffee, but brewed " + coffee);
        }
        helper.assertFalse(ItemStack.isSameItemSameComponents(coffee(Roast.LIGHT, BeanQuality.GOOD, 1), coffee(Roast.DARK, BeanQuality.GOOD, 1)),
                "Coffee of different roasts shouldn't stack");
        helper.succeed();
    }

    private static ItemStack coffee(Roast roast, BeanQuality quality, int count) {
        ItemStack coffee = new ItemStack(ModItems.COFFEE.get(), count);
        coffee.set(ModDataComponents.ROAST.get(), roast);
        coffee.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return coffee;
    }

    private static ItemStack drink(GameTestHelper helper, Player player, ItemStack coffee) {
        return coffee.finishUsingItem(helper.getLevel(), player);
    }

    private static void assertEffect(GameTestHelper helper, Player player, Holder<MobEffect> effect, int ticks) {
        MobEffectInstance active = player.getEffect(effect);
        helper.assertTrue(active != null && active.getAmplifier() == 0 && active.getDuration() == ticks,
                "Expected " + effect.getRegisteredName() + " I for " + ticks + " ticks, but got " + active);
    }
}
