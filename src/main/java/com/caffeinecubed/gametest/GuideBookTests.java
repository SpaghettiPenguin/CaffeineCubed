package com.caffeinecubed.gametest;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.item.GuideBookItem;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class GuideBookTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void aBookAndARawBeanMakeTheGuide(GameTestHelper helper) {
        CraftingInput grid = CraftingInput.of(2, 1, List.of(new ItemStack(Items.BOOK), new ItemStack(ModItems.RAW_COFFEE_BEANS.get())));
        ItemStack made = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel())
                .map(recipe -> recipe.value().assemble(grid, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(made.is(ModItems.GUIDE_BOOK.get()), "Expected the guide but crafted " + made);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void patchouliKnowsTheGuide(GameTestHelper helper) {
        // Patchouli read the book from data/caffeinecubed/patchouli_books/guide, and knows our item is that book
        Book book = BookRegistry.INSTANCE.books.get(GuideBookItem.BOOK);
        helper.assertTrue(book != null, "Patchouli should have loaded " + GuideBookItem.BOOK);
        helper.assertTrue(book.getBookItem().is(ModItems.GUIDE_BOOK.get()), "The guide's item should be ours, but is " + book.getBookItem());
        helper.succeed();
    }
}
