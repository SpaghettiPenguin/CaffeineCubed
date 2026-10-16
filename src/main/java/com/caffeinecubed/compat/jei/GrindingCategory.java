package com.caffeinecubed.compat.jei;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.item.CoffeeGrinderItem;
import com.caffeinecubed.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
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

/**
 * JEI's "Coffee Grinding" page: roasted beans and the grinder on the left, grounds on the right, in the same café style
 * as the roasting page. The slots' tooltips say which hand each goes in.
 */
public class GrindingCategory implements IRecipeCategory<BeanRecipe> {
    public static final RecipeType<BeanRecipe> TYPE = RecipeType.create(CaffeineCubed.MODID, "grinding", BeanRecipe.class);
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/jei/coffee_grinding.png");
    private static final ResourceLocation ROASTER_GUI = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/container/coffee_roaster.png");
    // Where things sit on the panel; tools/generate_assets.py draws it with the same numbers
    private static final int WIDTH = 88;
    private static final int HEIGHT = 50;
    private static final int ARROW_X = 26;
    private static final int ARROW_Y = 14;
    private static final int TEXT_COLOUR = 0x5A3A22;

    private final IDrawable panel;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public GrindingCategory(IGuiHelper gui) {
        // The panel texture is exactly its own size, not the usual 256x256 sheet
        panel = gui.drawableBuilder(PANEL, 0, 0, WIDTH, HEIGHT).setTextureSize(WIDTH, HEIGHT).build();
        icon = gui.createDrawableItemLike(ModItems.COFFEE_GRINDER.get());
        // The roaster's arrow, filling over the time one bean takes to grind
        arrow = gui.createAnimatedDrawable(gui.createDrawable(ROASTER_GUI, 176, 14, 24, 16),
                CoffeeGrinderItem.GRIND_TIME, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<BeanRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.caffeinecubed.grinding");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BeanRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder beans = builder.addInputSlot(3, 3).addItemStacks(recipe.inputs())
                .addRichTooltipCallback((slot, tooltip) -> tooltip.add(
                        Component.translatable("jei.caffeinecubed.grinding.off_hand").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.CATALYST, 3, 27).addItemStack(new ItemStack(ModItems.COFFEE_GRINDER.get()))
                .addRichTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.translatable("jei.caffeinecubed.grinding.main_hand").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.translatable("jei.caffeinecubed.grinding.durability").withStyle(ChatFormatting.GRAY));
                });
        IRecipeSlotBuilder grounds = builder.addOutputSlot(63, 15).addItemStacks(recipe.outputs());
        // Both slots cycle through the qualities together, so Great beans are shown grinding into Great grounds
        builder.createFocusLink(beans, grounds);
    }

    @Override
    public void draw(BeanRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        panel.draw(graphics);
        arrow.draw(graphics, ARROW_X, ARROW_Y);
        Font font = Minecraft.getInstance().font;
        Component time = Component.translatable("jei.caffeinecubed.grinding.time", CoffeeGrinderItem.GRIND_TIME / 20);
        graphics.drawString(font, time, ARROW_X + 12 - font.width(time) / 2, ARROW_Y + 18, TEXT_COLOUR, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BeanRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= ARROW_X && mouseX < ARROW_X + 24 && mouseY >= ARROW_Y && mouseY < ARROW_Y + 16) {
            tooltip.add(Component.translatable("jei.caffeinecubed.grinding.how"));
            tooltip.add(Component.translatable("jei.caffeinecubed.roasting.quality").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getRegistryName(BeanRecipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "grinding/" + BuiltInRegistries.ITEM.getKey(recipe.input()).getPath());
    }
}
