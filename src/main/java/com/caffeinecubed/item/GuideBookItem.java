package com.caffeinecubed.item;

import com.caffeinecubed.CaffeineCubed;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

/**
 * The Caffeine Cubed guide: right-click to open it. The book itself is a Patchouli book, written in
 * data/caffeinecubed/patchouli_books/guide and assets/caffeinecubed/patchouli_books/guide.
 */
public class GuideBookItem extends Item {
    public static final ResourceLocation BOOK = ResourceLocation.fromNamespaceAndPath(CaffeineCubed.MODID, "guide");

    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliAPI.get().openBookGUI(serverPlayer, BOOK);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(book, level.isClientSide);
    }
}
