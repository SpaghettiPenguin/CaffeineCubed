package com.caffeinecubed.gametest;

import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.LEFT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.OUTPUT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.RIGHT;
import static com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity.TOP;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.block.TripleAxisSiphonBlock;
import com.caffeinecubed.block.entity.TripleAxisSiphonBlockEntity;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.registry.ModBlocks;
import com.caffeinecubed.registry.ModDataComponents;
import com.caffeinecubed.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}, or with {@code /test runall} in a dev client.
 */
@GameTestHolder(CaffeineCubed.MODID)
@PrefixGameTestTemplate(false)
public final class GodsCoffeeTests {
    /**
     * The template every test uses: a 3x3x3 space of air.
     */
    private static final String EMPTY = "empty";
    private static final BlockPos SIPHON = new BlockPos(1, 1, 1);
    private static final int TIME = TripleAxisSiphonBlockEntity.SIPHONING_TIME;
    private static final List<Holder<MobEffect>> OTHER_EFFECTS = List.of(MobEffects.REGENERATION, MobEffects.NIGHT_VISION,
            MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED);

    @GameTest(template = EMPTY)
    public static void blendsWaterAndAnAppleMakeGodsCoffee(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        siphon.setItem(LEFT, blends(BeanQuality.GREAT, 4));
        siphon.setItem(TOP, new ItemStack(Items.WATER_BUCKET));
        siphon.setItem(RIGHT, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2));
        tick(helper, siphon, TIME - 1);
        helper.assertTrue(siphon.getItem(OUTPUT).isEmpty(), "God's Coffee should take the siphon's full time");
        tick(helper, siphon, 1);
        ItemStack coffee = siphon.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.GODS_COFFEE.get()) && coffee.getCount() == 1 && BeanQuality.of(coffee) == BeanQuality.GREAT,
                "Great Rainbow Blends should make one Great God's Coffee, but made " + coffee);
        helper.assertTrue(siphon.getItem(LEFT).getCount() == 1, "Three Rainbow Blends should be used up");
        helper.assertTrue(siphon.getItem(TOP).is(Items.BUCKET), "The water should leave its empty bucket in the slot");
        helper.assertTrue(siphon.getItem(RIGHT).getCount() == 1, "One enchanted golden apple should be used up");
        helper.assertTrue(siphon.canTakeItemThroughFace(TOP, siphon.getItem(TOP), Direction.DOWN), "A hopper below should take the empty bucket");
        helper.assertFalse(siphon.canTakeItemThroughFace(RIGHT, siphon.getItem(RIGHT), Direction.DOWN), "A hopper below shouldn't take the apple");
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).getCount() == 1, "Without water nothing more should be made");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceMakesGodsIceCoffeeInAnyOrder(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        siphon.setItem(LEFT, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        siphon.setItem(TOP, new ItemStack(Items.ICE, 2));
        siphon.setItem(RIGHT, blends(BeanQuality.POOR, 3));
        tick(helper, siphon, TIME);
        ItemStack coffee = siphon.getItem(OUTPUT);
        helper.assertTrue(coffee.is(ModItems.GODS_ICE_COFFEE.get()) && BeanQuality.of(coffee) == BeanQuality.POOR,
                "Ice should make a Poor God's Ice Coffee, but made " + coffee);
        helper.assertTrue(siphon.getItem(TOP).is(Items.ICE) && siphon.getItem(TOP).getCount() == 1, "One block of ice should be used up");
        helper.assertTrue(siphon.getItem(LEFT).isEmpty() && siphon.getItem(RIGHT).isEmpty(), "The apple and the blends should be used up");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void itTakesThreeRainbowBlends(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        siphon.setItem(LEFT, blends(BeanQuality.GOOD, 2));
        siphon.setItem(TOP, new ItemStack(Items.WATER_BUCKET));
        siphon.setItem(RIGHT, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).isEmpty() && siphon.getProgress() == 0, "Two Rainbow Blends shouldn't be enough");
        siphon.setItem(LEFT, blends(BeanQuality.GOOD, 3));
        tick(helper, siphon, TIME);
        helper.assertTrue(siphon.getItem(OUTPUT).is(ModItems.GODS_COFFEE.get()), "Three Rainbow Blends should be enough");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theSlotsOnlyTakeWhatGoesTogether(GameTestHelper helper) {
        TripleAxisSiphonBlockEntity siphon = place(helper);
        siphon.setItem(TOP, new ItemStack(Items.WATER_BUCKET));
        helper.assertFalse(siphon.canPlaceItem(RIGHT, new ItemStack(Items.ICE)), "Ice shouldn't go in with water");
        helper.assertFalse(siphon.canPlaceItem(RIGHT, new ItemStack(Items.GOLDEN_APPLE)), "A plain golden apple won't do");
        helper.assertFalse(siphon.canPlaceItem(RIGHT, new ItemStack(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get())),
                "Grounds shouldn't go in with water");
        helper.assertTrue(siphon.canPlaceItem(RIGHT, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)), "The apple should go in with water");
        helper.assertTrue(siphon.canPlaceItem(RIGHT, blends(BeanQuality.DECENT, 3)), "Rainbow Blends should go in with water");
        // The empty bucket left behind doesn't count as an ingredient, so the other slots can be filled around it
        siphon.setItem(TOP, new ItemStack(Items.BUCKET));
        helper.assertTrue(siphon.canPlaceItem(LEFT, new ItemStack(ModItems.LIGHT_ROAST_COFFEE_GROUNDS.get())),
                "An empty bucket shouldn't stop grounds going in");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void godsCoffeeGivesSixEffectsForLongerTheBetterTheQuality(GameTestHelper helper) {
        int[] seconds = {30, 60, 90, 120};
        for (BeanQuality quality : BeanQuality.values()) {
            int ticks = seconds[quality.ordinal()] * 20;
            for (Item cup : List.of(ModItems.GODS_COFFEE.get(), ModItems.GODS_ICE_COFFEE.get())) {
                boolean iced = cup == ModItems.GODS_ICE_COFFEE.get();
                ItemStack coffee = new ItemStack(cup);
                coffee.set(ModDataComponents.BEAN_QUALITY.get(), quality);
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                coffee.finishUsingItem(helper.getLevel(), player);
                helper.assertTrue(coffee.isEmpty(), "The cup should be used up");
                helper.assertTrue(player.getActiveEffects().size() == 6, "Expected six effects, but got " + player.getActiveEffects());
                MobEffectInstance base = player.getEffect(iced ? MobEffects.FIRE_RESISTANCE : MobEffects.MOVEMENT_SPEED);
                helper.assertTrue(base != null && base.getAmplifier() == (iced ? 0 : 1) && base.getDuration() == ticks,
                        quality + " " + cup + " should give " + seconds[quality.ordinal()] + "s of " + (iced ? "Fire Resistance I" : "Speed II")
                                + ", but gave " + base);
                if (iced) {
                    helper.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "God's Ice Coffee shouldn't give Speed");
                }
                for (Holder<MobEffect> effect : OTHER_EFFECTS) {
                    MobEffectInstance given = player.getEffect(effect);
                    helper.assertTrue(given != null && given.getAmplifier() == 0 && given.getDuration() == ticks,
                            quality + " " + cup + " should give " + seconds[quality.ordinal()] + "s of " + effect.getRegisteredName()
                                    + " I, but gave " + given);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void godsCoffeeLooksLikeAnEnchantedGoldenApple(GameTestHelper helper) {
        for (Item cup : List.of(ModItems.GODS_COFFEE.get(), ModItems.GODS_ICE_COFFEE.get())) {
            ItemStack coffee = new ItemStack(cup);
            helper.assertTrue(coffee.getRarity() == Rarity.EPIC && coffee.hasFoil(), cup + " should have an epic name and the glint");
        }
        helper.succeed();
    }

    private static TripleAxisSiphonBlockEntity place(GameTestHelper helper) {
        helper.setBlock(SIPHON, ModBlocks.TRIPLE_AXIS_SIPHON.get().defaultBlockState().setValue(TripleAxisSiphonBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(SIPHON);
    }

    private static ItemStack blends(BeanQuality quality, int count) {
        ItemStack blends = new ItemStack(ModItems.RAINBOW_BLEND.get(), count);
        blends.set(ModDataComponents.BEAN_QUALITY.get(), quality);
        return blends;
    }

    /**
     * Runs the siphon for this many ticks straight away, instead of waiting for the game to.
     */
    private static void tick(GameTestHelper helper, TripleAxisSiphonBlockEntity siphon, int ticks) {
        BlockPos pos = helper.absolutePos(SIPHON);
        for (int i = 0; i < ticks; i++) {
            TripleAxisSiphonBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), siphon);
        }
    }
}
