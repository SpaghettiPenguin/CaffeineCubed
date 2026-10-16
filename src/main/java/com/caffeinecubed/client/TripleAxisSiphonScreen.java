package com.caffeinecubed.client;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.menu.TripleAxisSiphonMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Draws the Triple Axis Siphon's interface in the same café style as the other machines: three slots around a
 * gold-framed one in the middle, with an arrow from each. The arrows fill towards the middle together, in the colours
 * of the rainbow, as the blend comes together. The texture is made by tools/generate_assets.py, with the filled arrows
 * beside it.
 */
public class TripleAxisSiphonScreen extends AbstractContainerScreen<TripleAxisSiphonMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID,
            "textures/gui/container/triple_axis_siphon.png");
    // Cream, to read well on the green board and the wood
    private static final int LABEL_COLOUR = 0xF4EAD8;
    // The arrow from the top slot, pointing down into the middle
    public static final int TOP_ARROW_X = 80;
    public static final int TOP_ARROW_Y = 37;
    public static final int TOP_ARROW_WIDTH = 16;
    public static final int TOP_ARROW_HEIGHT = 17;
    // The arrows from the side slots, pointing in from the left and from the right
    public static final int LEFT_ARROW_X = 48;
    public static final int RIGHT_ARROW_X = 104;
    public static final int SIDE_ARROW_Y = 57;
    public static final int SIDE_ARROW_WIDTH = 24;
    public static final int SIDE_ARROW_HEIGHT = 16;

    public TripleAxisSiphonScreen(TripleAxisSiphonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = TripleAxisSiphonMenu.HEIGHT;
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        float progress = menu.getProgress();
        // Down from the top
        int down = Mth.ceil(progress * TOP_ARROW_HEIGHT);
        graphics.blit(TEXTURE, leftPos + TOP_ARROW_X, topPos + TOP_ARROW_Y, 176, 0, TOP_ARROW_WIDTH, down);
        // In from the left
        int across = Mth.ceil(progress * SIDE_ARROW_WIDTH);
        graphics.blit(TEXTURE, leftPos + LEFT_ARROW_X, topPos + SIDE_ARROW_Y, 176, 17, across, SIDE_ARROW_HEIGHT);
        // In from the right, so its filled part grows leftwards from the right-hand end
        int skip = SIDE_ARROW_WIDTH - across;
        graphics.blit(TEXTURE, leftPos + RIGHT_ARROW_X + skip, topPos + SIDE_ARROW_Y, 176 + skip, 33, across, SIDE_ARROW_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOUR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOUR, false);
    }
}
