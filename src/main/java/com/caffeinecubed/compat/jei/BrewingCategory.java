package com.caffeinecubed.compat.jei;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.coffee.Syrup;
import com.caffeinecubed.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI's "Coffee Brewing" page: the Coffee Machine's two columns in miniature. Water or ice and the flame on the left; three
 * grounds, the arrow and the cup on the right; for coffee with milk, the milk beside the grounds; and for coffee with a
 * syrup, its ingredient in the bottom left corner, where the machine has its syrup slot.
 */
public class BrewingCategory implements IRecipeCategory<BrewingRecipe> {
    public static final RecipeType<BrewingRecipe> TYPE = RecipeType.create(CaffeineCubed.MODID, "brewing", BrewingRecipe.class);
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/jei/coffee_brewing.png");
    private static final ResourceLocation MACHINE_GUI = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/container/coffee_machine.png");
    // Where things sit on the panel; tools/generate_assets.py draws it with the same numbers
    private static final int WIDTH = 84;
    private static final int HEIGHT = 68;
    private static final int FLAME_X = 4;
    private static final int FLAME_Y = 22;
    private static final int ARROW_X = 41;
    private static final int ARROW_Y = 21;
    private static final int TEXT_COLOUR = 0x5A3A22;
    // The slot for milk or a syrup is drawn beside the panel in its texture, and only shown on recipes that use them
    private static final int TEXTURE_WIDTH = WIDTH + 18;
    private static final int MILK_X = 61;
    private static final int MILK_Y = 2;
    private static final int SYRUP_X = 2;
    private static final int SYRUP_Y = 42;

    private final IDrawable panel;
    private final IDrawable extraSlot;
    private final IDrawable icon;
    private final IDrawableAnimated flame;
    private final IDrawableAnimated arrow;

    public BrewingCategory(IGuiHelper gui) {
        // The panel texture is exactly its own size, not the usual 256x256 sheet
        panel = gui.drawableBuilder(PANEL, 0, 0, WIDTH, HEIGHT).setTextureSize(TEXTURE_WIDTH, HEIGHT).build();
        extraSlot = gui.drawableBuilder(PANEL, WIDTH, 0, 18, 18).setTextureSize(TEXTURE_WIDTH, HEIGHT).build();
        icon = gui.createDrawableItemLike(ModItems.COFFEE_MACHINE.get());
        // The flame burns down and the arrow fills downwards over one cup's brewing time, like in the machine
        flame = gui.createAnimatedDrawable(gui.createDrawable(MACHINE_GUI, 176, 0, 14, 14),
                CoffeeMachineBlockEntity.BREWING_TIME, IDrawableAnimated.StartDirection.TOP, true);
        arrow = gui.createAnimatedDrawable(gui.createDrawable(MACHINE_GUI, 176, 14, 16, 17),
                CoffeeMachineBlockEntity.BREWING_TIME, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    public RecipeType<BrewingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.caffeinecubed.brewing");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BrewingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(3, 3).addItemStack(new ItemStack(recipe.iced() ? Items.ICE : Items.WATER_BUCKET))
                .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable(
                        recipe.iced() ? "jei.caffeinecubed.brewing.ice" : "jei.caffeinecubed.brewing.water").withStyle(ChatFormatting.GRAY)));
        IRecipeSlotBuilder grounds = builder.addInputSlot(41, 3).addItemStacks(recipe.inputs())
                .addRichTooltipCallback((slot, tooltip) -> tooltip.add(
                        Component.translatable("jei.caffeinecubed.brewing.grounds").withStyle(ChatFormatting.GRAY)));
        if (recipe.milk()) {
            builder.addInputSlot(MILK_X + 1, MILK_Y + 1).addItemStack(new ItemStack(Items.MILK_BUCKET))
                    .addRichTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.translatable("jei.caffeinecubed.brewing.milk").withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable("jei.caffeinecubed.brewing.milk.effect").withStyle(ChatFormatting.GRAY));
                    });
        }
        Syrup syrup = recipe.syrup();
        if (syrup != null) {
            builder.addInputSlot(SYRUP_X + 1, SYRUP_Y + 1).addItemStack(new ItemStack(syrup.ingredient()))
                    .addRichTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.translatable("jei.caffeinecubed.brewing.syrup").withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable("jei.caffeinecubed.brewing.syrup.effect", syrup.displayName(),
                                syrup.effect().value().getDisplayName()).withStyle(ChatFormatting.GRAY));
                    });
        }
        IRecipeSlotBuilder coffee = builder.addOutputSlot(41, 43).addItemStacks(recipe.outputs());
        // Both slots cycle through the qualities together, so Great grounds are shown brewing Great coffee
        builder.createFocusLink(grounds, coffee);
    }

    @Override
    public void draw(BrewingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        panel.draw(graphics);
        if (recipe.milk()) {
            extraSlot.draw(graphics, MILK_X, MILK_Y);
        }
        if (recipe.syrup() != null) {
            extraSlot.draw(graphics, SYRUP_X, SYRUP_Y);
        }
        flame.draw(graphics, FLAME_X, FLAME_Y);
        arrow.draw(graphics, ARROW_X, ARROW_Y);
        Font font = Minecraft.getInstance().font;
        Component time = Component.translatable("jei.caffeinecubed.brewing.time", CoffeeMachineBlockEntity.BREWING_TIME / 20);
        graphics.drawString(font, time, ARROW_X + 20, ARROW_Y + 5, TEXT_COLOUR, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BrewingRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isOver(mouseX, mouseY, FLAME_X, FLAME_Y, 14, 14)) {
            tooltip.add(Component.translatable("jei.caffeinecubed.brewing.fuel"));
            tooltip.add(Component.translatable("jei.caffeinecubed.brewing.fuel.coal").withStyle(ChatFormatting.GRAY));
        } else if (isOver(mouseX, mouseY, ARROW_X, ARROW_Y, 16, 17)) {
            tooltip.add(Component.translatable("jei.caffeinecubed.brewing.quality"));
        }
    }

    @Override
    public ResourceLocation getRegistryName(BrewingRecipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "brewing/" + (recipe.iced() ? "iced/" : "")
                + BuiltInRegistries.ITEM.getKey(recipe.grounds()).getPath()
                + (recipe.milk() ? "_with_milk" : "") + (recipe.syrup() == null ? "" : "_with_" + recipe.syrup().getSerializedName() + "_syrup"));
    }

    private static boolean isOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
