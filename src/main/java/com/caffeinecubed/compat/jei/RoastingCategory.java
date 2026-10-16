package com.caffeinecubed.compat.jei;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.CoffeeRoasterBlockEntity;
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

/**
 * JEI's "Coffee Roasting" page: beans on the left, roasted beans on the right, drawn like a little piece of the
 * roaster's own interface. Hovering the flame explains the fuel, and the arrow that quality is kept.
 */
public class RoastingCategory implements IRecipeCategory<BeanRecipe> {
    public static final RecipeType<BeanRecipe> TYPE = RecipeType.create(CaffeineCubed.MODID, "roasting", BeanRecipe.class);
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/jei/coffee_roasting.png");
    private static final ResourceLocation ROASTER_GUI = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/container/coffee_roaster.png");
    // Where things sit on the panel; tools/generate_assets.py draws it with the same numbers
    private static final int WIDTH = 88;
    private static final int HEIGHT = 44;
    private static final int FLAME_X = 4;
    private static final int FLAME_Y = 23;
    private static final int ARROW_X = 26;
    private static final int ARROW_Y = 12;
    private static final int TEXT_COLOUR = 0x5A3A22;

    private final IDrawable panel;
    private final IDrawable icon;
    private final IDrawableAnimated flame;
    private final IDrawableAnimated arrow;

    public RoastingCategory(IGuiHelper gui) {
        // The panel texture is exactly its own size, not the usual 256x256 sheet
        panel = gui.drawableBuilder(PANEL, 0, 0, WIDTH, HEIGHT).setTextureSize(WIDTH, HEIGHT).build();
        icon = gui.createDrawableItemLike(ModItems.COFFEE_ROASTER.get());
        // The flame burns down and the arrow fills over one bean's roasting time, like in the roaster
        flame = gui.createAnimatedDrawable(gui.createDrawable(ROASTER_GUI, 176, 0, 14, 14),
                CoffeeRoasterBlockEntity.roastingTime(), IDrawableAnimated.StartDirection.TOP, true);
        arrow = gui.createAnimatedDrawable(gui.createDrawable(ROASTER_GUI, 176, 14, 24, 16),
                CoffeeRoasterBlockEntity.roastingTime(), IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<BeanRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.caffeinecubed.roasting");
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
        IRecipeSlotBuilder beans = builder.addInputSlot(3, 3).addItemStacks(recipe.inputs());
        IRecipeSlotBuilder roasted = builder.addOutputSlot(63, 13).addItemStacks(recipe.outputs());
        // Both slots cycle through the qualities together, so Great beans are shown roasting into Great beans
        builder.createFocusLink(beans, roasted);
    }

    @Override
    public void draw(BeanRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        panel.draw(graphics);
        flame.draw(graphics, FLAME_X, FLAME_Y);
        arrow.draw(graphics, ARROW_X, ARROW_Y);
        Font font = Minecraft.getInstance().font;
        Component time = Component.translatable("jei.caffeinecubed.roasting.time", CoffeeRoasterBlockEntity.roastingTime() / 20);
        graphics.drawString(font, time, ARROW_X + 12 - font.width(time) / 2, ARROW_Y + 18, TEXT_COLOUR, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BeanRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isOver(mouseX, mouseY, FLAME_X, FLAME_Y, 14, 14)) {
            tooltip.add(Component.translatable("jei.caffeinecubed.roasting.fuel"));
            tooltip.add(Component.translatable("jei.caffeinecubed.roasting.fuel.coal").withStyle(ChatFormatting.GRAY));
        } else if (isOver(mouseX, mouseY, ARROW_X, ARROW_Y, 24, 16)) {
            tooltip.add(Component.translatable("jei.caffeinecubed.roasting.quality"));
        }
    }

    @Override
    public ResourceLocation getRegistryName(BeanRecipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "roasting/" + BuiltInRegistries.ITEM.getKey(recipe.input()).getPath());
    }

    private static boolean isOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
