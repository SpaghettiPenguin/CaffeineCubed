package com.caffeinecubed.compat.jei;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
import com.caffeinecubed.client.CoffeeMachineScreen;
import com.caffeinecubed.client.CoffeeRoasterScreen;
import com.caffeinecubed.client.TripleAxisSiphonScreen;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.menu.CoffeeRoasterMenu;
import com.caffeinecubed.recipe.SingleItemCoffeeRecipe;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import com.caffeinecubed.registry.ModMenus;
import com.caffeinecubed.registry.ModRecipes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Adds Caffeine Cubed to JEI, when it is installed: the Coffee Roasting, Grinding and Brewing pages, the Triple Axis
 * Siphon's page, and each quality of beans, grounds, blends and coffee as its own entry.
 * JEI finds this class by its annotation; nothing else in the mod refers to it, so the mod runs fine without JEI.
 */
@JeiPlugin
public class CaffeineCubedJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "jei_plugin");

    /**
     * Tells JEI that beans, grounds and coffee of different qualities are different things, and coffee of different
     * roasts, with and without milk, or with different syrups, too, so it lists each one.
     */
    private static final ISubtypeInterpreter<ItemStack> BY_QUALITY = new ISubtypeInterpreter<>() {
        @Override
        public Object getSubtypeData(ItemStack coffee, UidContext context) {
            return getLegacyStringSubtypeInfo(coffee, context);
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack coffee, UidContext context) {
            Roast roast = coffee.get(ModDataComponents.ROAST.get());
            return BeanQuality.of(coffee).getSerializedName() + (roast == null ? "" : "/" + roast.getSerializedName())
                    + (coffee.has(ModDataComponents.MILK.get()) ? "/milk" : "")
                    + (coffee.has(ModDataComponents.SYRUP.get()) ? "/" + coffee.get(ModDataComponents.SYRUP.get()).getSerializedName() : "");
        }
    };

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Every item that carries a quality
        for (var item : ModItems.ITEMS.getEntries()) {
            if (item.get().components().has(ModDataComponents.BEAN_QUALITY.get())) {
                registration.registerSubtypeInterpreter(item.get(), BY_QUALITY);
            }
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RoastingCategory(registration.getJeiHelpers().getGuiHelper()),
                new GrindingCategory(registration.getJeiHelpers().getGuiHelper()),
                new BrewingCategory(registration.getJeiHelpers().getGuiHelper()),
                new SiphoningCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * The pages come from the recipes the server sent, so a data pack's changes show up too.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        RecipeManager recipes = connection.getRecipeManager();
        registration.addRecipes(RoastingCategory.TYPE, beanRecipes(recipes.getAllRecipesFor(ModRecipes.ROASTING.get())));
        registration.addRecipes(GrindingCategory.TYPE, beanRecipes(recipes.getAllRecipesFor(ModRecipes.GRINDING.get())));
        registration.addRecipes(BrewingCategory.TYPE, BrewingRecipe.all(inItemOrder(recipes.getAllRecipesFor(ModRecipes.BREWING.get()),
                recipe -> recipe.grounds().getItems())));
        registration.addRecipes(SiphoningCategory.TYPE, SiphoningRecipe.all(inItemOrder(recipes.getAllRecipesFor(ModRecipes.SIPHONING.get()),
                recipe -> new ItemStack[]{recipe.result()})));
    }

    private static <T extends SingleItemCoffeeRecipe> List<BeanRecipe> beanRecipes(List<RecipeHolder<T>> recipes) {
        return inItemOrder(recipes, recipe -> recipe.ingredient().getItems()).stream()
                .map(holder -> BeanRecipe.of(holder.id(), holder.value().ingredient(), holder.value()::make))
                .filter(Objects::nonNull).toList();
    }

    /**
     * The recipes in the order their items were added to the game (beans and grounds lightest roast first, then the
     * siphon's blends), then by id, rather than the order they happened to load in.
     */
    private static <T extends Recipe<?>> List<RecipeHolder<T>> inItemOrder(List<RecipeHolder<T>> recipes, Function<T, ItemStack[]> items) {
        ToIntFunction<RecipeHolder<T>> firstItem = holder -> {
            ItemStack[] stacks = items.apply(holder.value());
            return stacks.length == 0 ? Integer.MAX_VALUE : BuiltInRegistries.ITEM.getId(stacks[0].getItem());
        };
        return recipes.stream().sorted(Comparator.comparingInt(firstItem).thenComparing(holder -> holder.id().toString())).toList();
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // The roaster also shows up on JEI's fuel page, since it burns the same fuels as a furnace
        registration.addRecipeCatalyst(ModItems.COFFEE_ROASTER.get(), RoastingCategory.TYPE, RecipeTypes.FUELING);
        registration.addRecipeCatalyst(ModItems.COFFEE_GRINDER.get(), GrindingCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.GRINDER_ATTACHMENT.get(), GrindingCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.COFFEE_MACHINE.get(), BrewingCategory.TYPE, RecipeTypes.FUELING);
        registration.addRecipeCatalyst(ModItems.MILK_FROTHER.get(), BrewingCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.SYRUP_COMPACTOR.get(), BrewingCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.TRIPLE_AXIS_SIPHON.get(), SiphoningCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Clicking the roaster's arrow opens its recipes, like a furnace's
        registration.addRecipeClickArea(CoffeeRoasterScreen.class, 78, 32, 28, 23, RoastingCategory.TYPE, RecipeTypes.FUELING);
        registration.addRecipeClickArea(CoffeeMachineScreen.class, CoffeeMachineScreen.ARROW_X - 2, CoffeeMachineScreen.ARROW_Y - 1,
                CoffeeMachineScreen.ARROW_WIDTH + 4, CoffeeMachineScreen.ARROW_HEIGHT + 2, BrewingCategory.TYPE, RecipeTypes.FUELING);
        // The Grinder Attachment's little arrow, from the beans into the grounds
        registration.addRecipeClickArea(CoffeeMachineScreen.class, CoffeeMachineScreen.GRIND_ARROW_X, CoffeeMachineScreen.GRIND_ARROW_Y,
                CoffeeMachineScreen.GRIND_ARROW_WIDTH, CoffeeMachineScreen.GRIND_ARROW_HEIGHT, GrindingCategory.TYPE);
        // Any of the siphon's three arrows
        registration.addRecipeClickArea(TripleAxisSiphonScreen.class, TripleAxisSiphonScreen.TOP_ARROW_X, TripleAxisSiphonScreen.TOP_ARROW_Y,
                TripleAxisSiphonScreen.TOP_ARROW_WIDTH, TripleAxisSiphonScreen.TOP_ARROW_HEIGHT, SiphoningCategory.TYPE);
        for (int x : new int[]{TripleAxisSiphonScreen.LEFT_ARROW_X, TripleAxisSiphonScreen.RIGHT_ARROW_X}) {
            registration.addRecipeClickArea(TripleAxisSiphonScreen.class, x, TripleAxisSiphonScreen.SIDE_ARROW_Y,
                    TripleAxisSiphonScreen.SIDE_ARROW_WIDTH, TripleAxisSiphonScreen.SIDE_ARROW_HEIGHT, SiphoningCategory.TYPE);
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // JEI's [+] button moves beans from the inventory into the bean slot
        registration.addRecipeTransferHandler(CoffeeRoasterMenu.class, ModMenus.COFFEE_ROASTER.get(), RoastingCategory.TYPE,
                CoffeeRoasterBlockEntity.INPUT, 1, 3, 36);
    }
}
