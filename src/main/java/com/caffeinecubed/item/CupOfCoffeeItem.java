package com.caffeinecubed.item;

import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.CoffeeEffects;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.coffee.Syrup;
import com.caffeinecubed.registry.ModDataComponents;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * A cup of Coffee or Ice Coffee from the Coffee Machine. Drinking it uses up the cup and gives its base effect, Speed or
 * Fire Resistance, for as long as the beans' quality allows, plus an effect from the roast, plus Regeneration if it was
 * brewed with milk and the effect of its syrup if it has one (see {@link CoffeeEffects}).
 * It can be drunk at any time, like a potion.
 */
public class CupOfCoffeeItem extends Item {
    // As long as a potion takes to drink
    private static final int DRINK_DURATION = 32;

    private final Holder<MobEffect> baseEffect;

    public CupOfCoffeeItem(Holder<MobEffect> baseEffect, Properties properties) {
        super(properties);
        this.baseEffect = baseEffect;
    }

    public static Roast getRoast(ItemStack coffee) {
        return coffee.getOrDefault(ModDataComponents.ROAST.get(), Roast.MEDIUM);
    }

    public static boolean hasMilk(ItemStack coffee) {
        return coffee.has(ModDataComponents.MILK.get());
    }

    @Nullable
    public static Syrup getSyrup(ItemStack coffee) {
        return coffee.get(ModDataComponents.SYRUP.get());
    }

    public Holder<MobEffect> getBaseEffect() {
        return baseEffect;
    }

    public List<MobEffectInstance> getEffects(ItemStack coffee) {
        return CoffeeEffects.of(baseEffect, BeanQuality.of(coffee), getRoast(coffee), hasMilk(coffee), getSyrup(coffee));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coffee, Level level, LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, coffee);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide) {
            for (MobEffectInstance effect : getEffects(coffee)) {
                drinker.addEffect(effect);
            }
        }
        // The cup goes with the coffee; nothing is handed back
        coffee.consume(1, drinker);
        drinker.gameEvent(GameEvent.DRINK);
        return coffee;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack coffee, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(getRoast(coffee).displayName());
        if (hasMilk(coffee)) {
            tooltip.add(Component.translatable("tooltip.caffeinecubed.coffee.milk").withStyle(ChatFormatting.GRAY));
        }
        Syrup syrup = getSyrup(coffee);
        if (syrup != null) {
            tooltip.add(Component.translatable("tooltip.caffeinecubed.coffee.syrup", syrup.displayName()).withStyle(ChatFormatting.GRAY));
        }
        BeanQuality.addTooltip(coffee, tooltip);
        // The effects, listed the way a potion lists them
        PotionContents.addPotionTooltip(getEffects(coffee), tooltip::add, 1.0F, context.tickRate());
    }
}
