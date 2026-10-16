package com.caffeinecubed.compat.jei;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
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

/**
 * JEI's "Triple Axis Siphon" page: the siphon's interface in miniature, with an ingredient on the left, on top and on
 * the right, and the arrows filling towards the result in the middle. Hovering an arrow explains the rules.
 */
public class SiphoningCategory implements IRecipeCategory<SiphoningRecipe> {
    public static final RecipeType<SiphoningRecipe> TYPE = RecipeType.create(CaffeineCubed.MODID, "siphoning", SiphoningRecipe.class);
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/jei/siphoning.png");
    private static final ResourceLocation SIPHON_GUI = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID,
            "textures/gui/container/triple_axis_siphon.png");
    // Where things sit on the panel; tools/generate_assets.py draws it with the same numbers
    private static final int WIDTH = 118;
    private static final int HEIGHT = 66;
    private static final int TOP_ARROW_X = 51;
    private static final int TOP_ARROW_Y = 19;
    private static final int LEFT_ARROW_X = 21;
    private static final int RIGHT_ARROW_X = 73;
    private static final int SIDE_ARROW_Y = 41;
    private static final int TEXT_COLOUR = 0x5A3A22;
    private static final int[][] INPUT_SLOTS = {{1, 41}, {51, 1}, {101, 41}};

    private final IDrawable panel;
    private final IDrawable icon;
    private final IDrawableAnimated topArrow;
    private final IDrawableAnimated leftArrow;
    private final IDrawableAnimated rightArrow;

    public SiphoningCategory(IGuiHelper gui) {
        // The panel texture is exactly its own size, not the usual 256x256 sheet
        panel = gui.drawableBuilder(PANEL, 0, 0, WIDTH, HEIGHT).setTextureSize(WIDTH, HEIGHT).build();
        icon = gui.createDrawableItemLike(ModItems.TRIPLE_AXIS_SIPHON.get());
        // The three arrows fill towards the middle together over one blend's time, like in the siphon
        int time = TripleAxisSiphonBlockEntity.siphoningTime();
        topArrow = gui.createAnimatedDrawable(gui.createDrawable(SIPHON_GUI, 176, 0, 16, 17), time, IDrawableAnimated.StartDirection.TOP, false);
        leftArrow = gui.createAnimatedDrawable(gui.createDrawable(SIPHON_GUI, 176, 17, 24, 16), time, IDrawableAnimated.StartDirection.LEFT, false);
        rightArrow = gui.createAnimatedDrawable(gui.createDrawable(SIPHON_GUI, 176, 33, 24, 16), time, IDrawableAnimated.StartDirection.RIGHT, false);
    }

    @Override
    public RecipeType<SiphoningRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.caffeinecubed.siphoning");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SiphoningRecipe recipe, IFocusGroup focuses) {
        List<IRecipeSlotBuilder> qualitySlots = new ArrayList<>();
        for (int i = 0; i < INPUT_SLOTS.length; i++) {
            List<ItemStack> stacks = recipe.inputs().get(i);
            ItemStack remainder = stacks.getFirst().getCraftingRemainingItem();
            IRecipeSlotBuilder input = builder.addInputSlot(INPUT_SLOTS[i][0], INPUT_SLOTS[i][1]).addItemStacks(stacks)
                    .addRichTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.translatable("jei.caffeinecubed.siphoning.any_slot").withStyle(ChatFormatting.GRAY));
                        if (!remainder.isEmpty()) {
                            tooltip.add(Component.translatable("jei.caffeinecubed.siphoning.remainder", remainder.getHoverName())
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    });
            if (stacks.size() == recipe.outputs().size()) {
                qualitySlots.add(input);
            }
        }
        IRecipeSlotBuilder result = builder.addOutputSlot(51, 41).addItemStacks(recipe.outputs());
        if (recipe.outputs().size() > 1) {
            // The ingredients with a quality cycle through the qualities together with the result, so Great grounds are
            // shown making a Great blend
            qualitySlots.add(result);
            builder.createFocusLink(qualitySlots.toArray(IRecipeSlotBuilder[]::new));
        }
    }

    @Override
    public void draw(SiphoningRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        panel.draw(graphics);
        topArrow.draw(graphics, TOP_ARROW_X, TOP_ARROW_Y);
        leftArrow.draw(graphics, LEFT_ARROW_X, SIDE_ARROW_Y);
        rightArrow.draw(graphics, RIGHT_ARROW_X, SIDE_ARROW_Y);
        Font font = Minecraft.getInstance().font;
        Component time = Component.translatable("jei.caffeinecubed.siphoning.time", TripleAxisSiphonBlockEntity.siphoningTime() / 20);
        graphics.drawString(font, time, TOP_ARROW_X + 20, TOP_ARROW_Y + 5, TEXT_COLOUR, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, SiphoningRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isOver(mouseX, mouseY, TOP_ARROW_X, TOP_ARROW_Y, 16, 17) || isOver(mouseX, mouseY, LEFT_ARROW_X, SIDE_ARROW_Y, 24, 16)
                || isOver(mouseX, mouseY, RIGHT_ARROW_X, SIDE_ARROW_Y, 24, 16)) {
            tooltip.add(Component.translatable("jei.caffeinecubed.siphoning.no_fuel"));
            tooltip.add(Component.translatable("jei.caffeinecubed.siphoning.quality").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getRegistryName(SiphoningRecipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "siphoning/"
                + BuiltInRegistries.ITEM.getKey(recipe.recipe().result().asItem()).getPath());
    }

    private static boolean isOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
