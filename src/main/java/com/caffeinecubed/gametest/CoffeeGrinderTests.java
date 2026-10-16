package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.item.CoffeeGrinderItem;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class CoffeeGrinderTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void eachRoastGrindsIntoItsGroundsAndKeepsItsQuality(GameTestHelper helper) {
        List<Item> beans = List.of(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), ModItems.DARK_ROAST_COFFEE_BEANS.get());
        List<Item> grounds = List.of(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), ModItems.DARK_ROAST_COFFEE_GROUNDS.get());
        for (int i = 0; i < beans.size(); i++) {
            for (BeanQuality quality : BeanQuality.values()) {
                ItemStack ground = CoffeeGrinderItem.grind(helper.getLevel(), coffee(beans.get(i), quality, 1));
                helper.assertTrue(ground.is(grounds.get(i)) && ground.getCount() == 1, beans.get(i) + " should grind into " + grounds.get(i) + " but made " + ground);
                helper.assertTrue(BeanQuality.of(ground) == quality, quality + " beans ground into " + BeanQuality.of(ground) + " grounds");
            }
        }
        helper.assertTrue(CoffeeGrinderItem.grind(helper.getLevel(), coffee(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.GREAT, 1)).isEmpty(), "Raw beans have to be roasted first");
        helper.assertTrue(CoffeeGrinderItem.grind(helper.getLevel(), coffee(ModItems.DARK_ROAST_COFFEE_GROUNDS.get(), BeanQuality.GREAT, 1)).isEmpty(), "Grounds can't be ground again");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void holdingRightClickGrindsABeanEverySecond(GameTestHelper helper) {
        Player player = player(helper, coffee(ModItems.MEDIUM_ROAST_COFFEE_BEANS.get(), BeanQuality.GOOD, 5));
        ItemStack grinder = player.getMainHandItem();
        Grinding grinding = new Grinding(helper, player);
        helper.assertTrue(grinding.start() == InteractionResult.CONSUME, "Right-clicking with beans in the off hand should start grinding");

        grinding.hold(CoffeeGrinderItem.GRIND_TIME - 1);
        helper.assertTrue(countGrounds(player) == 0, "Nothing should be ground before a second has passed");
        grinding.hold(1);
        helper.assertTrue(countGrounds(player) == 1 && player.getOffhandItem().getCount() == 4, "One bean should be ground after a second");

        grinding.hold(4 * CoffeeGrinderItem.GRIND_TIME);
        helper.assertTrue(countGrounds(player) == 5 && player.getOffhandItem().isEmpty(), "All five beans should be ground after five seconds");
        ItemStack grounds = findGrounds(player);
        helper.assertTrue(grounds.is(ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get()) && BeanQuality.of(grounds) == BeanQuality.GOOD,
                "Good medium roast beans should make good medium roast grounds, but made " + grounds);
        helper.assertTrue(grinder.getDamageValue() == 5, "Five beans should cost 5 durability, but cost " + grinder.getDamageValue());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aGrinderGrinds32BeansThenBreaks(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.COFFEE_GRINDER.get()).getMaxDamage() == 32, "A grinder should have 32 uses");
        Player player = player(helper, coffee(ModItems.DARK_ROAST_COFFEE_BEANS.get(), BeanQuality.DECENT, 40));
        Grinding grinding = new Grinding(helper, player);
        grinding.start();
        grinding.hold(40 * CoffeeGrinderItem.GRIND_TIME);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "The grinder should have broken");
        helper.assertTrue(countGrounds(player) == 32, "A grinder should make 32 grounds, but made " + countGrounds(player));
        helper.assertTrue(player.getOffhandItem().getCount() == 8, "8 of the 40 beans should be left");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void stopsWhenTheBeansRunOut(GameTestHelper helper) {
        Player player = player(helper, coffee(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), BeanQuality.POOR, 1));
        ItemStack grinder = player.getMainHandItem();
        Grinding grinding = new Grinding(helper, player);
        grinding.start();
        grinding.hold(3 * CoffeeGrinderItem.GRIND_TIME);
        helper.assertTrue(countGrounds(player) == 1 && grinder.getDamageValue() == 1, "Only the one bean should be ground");
        helper.assertFalse(player.isUsingItem(), "Grinding should stop once the off hand is empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void needsRoastedBeansInTheOffHandAndTheGrinderInTheMainHand(GameTestHelper helper) {
        Player player = player(helper, coffee(ModItems.RAW_COFFEE_BEANS.get(), BeanQuality.GREAT, 3));
        ItemStack grinder = player.getMainHandItem();
        helper.assertTrue(grinder.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL,
                "Raw beans can't be ground");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.assertTrue(grinder.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL,
                "With nothing in the off hand, there is nothing to grind");
        helper.assertFalse(player.isUsingItem(), "The grinder shouldn't start without beans");

        // The other way round: beans in the main hand, grinder in the off hand
        player.setItemInHand(InteractionHand.OFF_HAND, grinder);
        player.setItemInHand(InteractionHand.MAIN_HAND, coffee(ModItems.LIGHT_ROAST_COFFEE_BEANS.get(), BeanQuality.GREAT, 3));
        helper.assertTrue(grinder.use(helper.getLevel(), player, InteractionHand.OFF_HAND).getResult() == InteractionResult.PASS,
                "The grinder only works from the main hand");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void grinderRecipe(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY,
                iron, new ItemStack(Items.FLINT), iron, ItemStack.EMPTY, iron, ItemStack.EMPTY));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.COFFEE_GRINDER.get()), "Expected a Coffee Grinder but crafted " + made);
        helper.succeed();
    }

    /**
     * A survival player with a new grinder in the main hand and these beans in the off hand.
     */
    private static Player player(GameTestHelper helper, ItemStack offHand) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COFFEE_GRINDER.get()));
        player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        return player;
    }

    /**
     * Right-clicking and holding the button down, ticked by the test the way the game ticks a player using an item.
     */
    private static final class Grinding {
        private final GameTestHelper helper;
        private final Player player;
        private final ItemStack grinder;
        private int ticks;

        Grinding(GameTestHelper helper, Player player) {
            this.helper = helper;
            this.player = player;
            this.grinder = player.getMainHandItem();
        }

        InteractionResult start() {
            return grinder.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult();
        }

        /**
         * Keeps holding the button for this many more ticks, or until grinding stops.
         */
        void hold(int moreTicks) {
            for (int i = 0; i < moreTicks && player.isUsingItem() && !grinder.isEmpty(); i++) {
                ticks++;
                grinder.onUseTick(helper.getLevel(), player, grinder.getUseDuration(player) - ticks);
            }
        }
    }

    private static ItemStack coffee(Item item, BeanQuality quality, int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return stack;
    }

    private static List<Item> grounds() {
        return List.of(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get(), ModItems.MEDIUM_ROAST_COFFEE_GROUNDS.get(), ModItems.DARK_ROAST_COFFEE_GROUNDS.get());
    }

    private static int countGrounds(Player player) {
        int count = 0;
        for (Item grounds : grounds()) {
            count += player.getInventory().countItem(grounds);
        }
        return count;
    }

    private static ItemStack findGrounds(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (grounds().stream().anyMatch(stack::is)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
