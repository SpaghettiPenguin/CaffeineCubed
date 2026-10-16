package com.caffeinecubed.client;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.menu.CoffeeMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Draws the Coffee Machine's interface in the same café style as the roaster's. There is a background for each mix of
 * attachments: the Grinder Attachment's bean slot, the Milk Frother's milk slot and the Syrup Compactor's syrup slot,
 * in any combination. Each is a 256x256 texture made by tools/generate_assets.py, with the flame and the arrows that
 * fill up beside it in the same places.
 */
public class CoffeeMachineScreen extends AbstractContainerScreen<CoffeeMachineMenu> {
    /**
     * The backgrounds, picked by which attachments are shown: 1 for the grinder, 2 for the frother, 4 for the compactor.
     */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[8];

    static {
        for (int attachments = 0; attachments < TEXTURES.length; attachments++) {
            TEXTURES[attachments] = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/container/coffee_machine"
                    + ((attachments & 1) != 0 ? "_grinder" : "") + ((attachments & 2) != 0 ? "_frother" : "")
                    + ((attachments & 4) != 0 ? "_compactor" : "") + ".png");
        }
    }
    // Cream, to read well on the green board and the wood
    private static final int LABEL_COLOUR = 0xF4EAD8;
    public static final int FLAME_X = 42;
    public static final int FLAME_Y = 36;
    public static final int ARROW_X = 110;
    public static final int ARROW_Y = 36;
    public static final int ARROW_WIDTH = 16;
    public static final int ARROW_HEIGHT = 17;
    // The Grinder Attachment's arrow from the bean slot into the grounds
    public static final int GRIND_ARROW_X = 87;
    public static final int GRIND_ARROW_Y = 20;
    public static final int GRIND_ARROW_WIDTH = 16;
    public static final int GRIND_ARROW_HEIGHT = 10;

    public CoffeeMachineScreen(CoffeeMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = CoffeeMachineMenu.HEIGHT;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        boolean grinder = menu.showsGrinder();
        ResourceLocation texture = TEXTURES[(grinder ? 1 : 0) | (menu.showsFrother() ? 2 : 0) | (menu.showsCompactor() ? 4 : 0)];
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isBurning()) {
            // The flame burns down from the top as the fuel item runs out
            int flame = Mth.ceil(menu.getFuelLeft() * 13) + 1;
            graphics.blit(texture, leftPos + FLAME_X, topPos + FLAME_Y + 14 - flame, 176, 14 - flame, 14, flame);
        }
        // The arrow fills from the grounds down to the cup, darkening like coffee as it brews
        int arrow = Mth.ceil(menu.getProgress() * ARROW_HEIGHT);
        graphics.blit(texture, leftPos + ARROW_X, topPos + ARROW_Y, 176, 14, ARROW_WIDTH, arrow);
        if (grinder) {
            // The arrow from the beans fills as each one is ground
            int grind = Mth.ceil(menu.getGrindProgress() * GRIND_ARROW_WIDTH);
            graphics.blit(texture, leftPos + GRIND_ARROW_X, topPos + GRIND_ARROW_Y, 194, 42, grind, GRIND_ARROW_HEIGHT);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOUR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOUR, false);
    }
}
