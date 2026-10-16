package com.caffeinecubed.item;

import com.caffeinecubed.CaffeineCubedConfig;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * A hand grinder. Hold it in the main hand with roasted beans in the off hand, then hold right-click: every second it
 * grinds one bean into grounds of the same roast and quality, for 1 durability. It keeps going until the button is
 * let go, the beans run out or the grinder breaks.
 */
public class CoffeeGrinderItem extends Item {
    /**
     * Ticks of grinding per bean.
     */
    public static final int GRIND_TIME = 20;

    /**
     * Ticks to grind one bean, as set in the config (GRIND_TIME unless changed).
     */
    public static int grindTime() {
        return CaffeineCubedConfig.get(CaffeineCubedConfig.HAND_GRINDING_TIME);
    }
    /**
     * Which beans grind into which grounds. Raw beans have to be roasted first.
     */
    public static final Map<DeferredItem<? extends Item>, DeferredItem<? extends Item>> GROUNDS = Map.of(
            ModItems.LIGHT_ROAST_COFFEE_BEANS, ModItems.LIGHT_ROAST_COFFEE_GROUNDS,
            ModItems.MEDIUM_ROAST_COFFEE_BEANS, ModItems.MEDIUM_ROAST_COFFEE_GROUNDS,
            ModItems.DARK_ROAST_COFFEE_BEANS, ModItems.DARK_ROAST_COFFEE_GROUNDS);
    // Long enough to grind any number of beans in one go; letting go of the button stops it
    private static final int USE_DURATION = 72000;

    public CoffeeGrinderItem(Properties properties) {
        super(properties);
    }

    /**
     * What grinding one of these beans makes: grounds of the same roast and quality. Nothing if they can't be ground.
     */
    public static ItemStack grind(ItemStack beans) {
        for (var entry : GROUNDS.entrySet()) {
            if (beans.is(entry.getKey())) {
                ItemStack grounds = new ItemStack(entry.getValue().get());
                grounds.set(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.of(beans));
                return grounds;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack grinder = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(grinder);
        }
        if (grind(player.getOffhandItem()).isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.caffeinecubed.coffee_grinder.no_beans"), true);
            }
            return InteractionResultHolder.fail(grinder);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(grinder);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack grinder, int remainingUseDuration) {
        if (!(user instanceof Player player)) {
            return;
        }
        ItemStack beans = player.getOffhandItem();
        ItemStack grounds = grind(beans);
        if (grounds.isEmpty()) {
            player.releaseUsingItem();
            return;
        }
        int ticks = getUseDuration(grinder, user) - remainingUseDuration;
        if (ticks % 10 == 5) {
            // A turn of the crank: a grinding rasp and a few bits of bean
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS,
                    0.3F, 1.4F + level.random.nextFloat() * 0.2F);
            if (level.isClientSide) {
                spawnBeanBits(level, player, beans);
            }
        }
        if (ticks > 0 && ticks % grindTime() == 0 && !level.isClientSide) {
            beans.consume(1, player);
            if (!player.getInventory().add(grounds)) {
                player.drop(grounds, false);
            }
            grinder.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    private static void spawnBeanBits(Level level, Player player, ItemStack beans) {
        // Just in front of the main hand, where the grinder is held
        Vec3 look = player.getViewVector(1.0F);
        Vec3 side = look.cross(new Vec3(0, 1, 0)).normalize().scale(player.getMainArm() == HumanoidArm.RIGHT ? 0.3 : -0.3);
        Vec3 at = player.getEyePosition().add(look.scale(0.6)).add(side).add(0, -0.35, 0);
        for (int i = 0; i < 3; i++) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, beans), at.x, at.y, at.z,
                    (level.random.nextDouble() - 0.5) * 0.08, 0.05, (level.random.nextDouble() - 0.5) * 0.08);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // The brush's back-and-forth, which looks like turning the crank
        return UseAnim.BRUSH;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.caffeinecubed.coffee_grinder").withStyle(ChatFormatting.GRAY));
    }
}
