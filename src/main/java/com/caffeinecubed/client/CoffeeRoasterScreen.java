package com.caffeinecubed.client;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.menu.CoffeeRoasterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Draws the Coffee Roaster's interface, a café counter in warm wood under a green menu board. The background, and the
 * flame and arrow that fill up, all come from one 256x256 texture made by tools/generate_assets.py.
 */
public class CoffeeRoasterScreen extends AbstractContainerScreen<CoffeeRoasterMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "textures/gui/container/coffee_roaster.png");
    // Cream, to read well on the green board and the wood
    private static final int LABEL_COLOUR = 0xF4EAD8;

    public CoffeeRoasterScreen(CoffeeRoasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isBurning()) {
            // The flame burns down from the top as the fuel item runs out
            int flame = Mth.ceil(menu.getFuelLeft() * 13) + 1;
            graphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 14 - flame, 176, 14 - flame, 14, flame);
        }
        // The arrow fills from left to right, darkening like a bean as it roasts
        int arrow = Mth.ceil(menu.getProgress() * 24);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, arrow, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOUR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOUR, false);
    }
}
