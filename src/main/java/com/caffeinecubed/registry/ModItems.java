package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.item.CoffeeGrinderItem;
import com.caffeinecubed.item.CoffeeItem;
import com.caffeinecubed.item.CupOfCoffeeItem;
import com.caffeinecubed.item.GodsCoffeeItem;
import com.caffeinecubed.item.GuideBookItem;
import com.caffeinecubed.item.RawCoffeeBeansItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CaffeineCubed.MODID);

    // Plant them on grass or dirt to grow a Coffee Bush, like sweet berries
    public static final DeferredItem<RawCoffeeBeansItem> RAW_COFFEE_BEANS = ITEMS.register("raw_coffee_beans",
            () -> new RawCoffeeBeansItem(ModBlocks.COFFEE_BUSH.get(), withQuality()));
    // Each roast is made by roasting the one before it in a Coffee Roaster
    public static final DeferredItem<CoffeeItem> LIGHT_ROAST_COFFEE_BEANS = ITEMS.register("light_roast_coffee_beans",
            () -> new CoffeeItem(withQuality()));
    public static final DeferredItem<CoffeeItem> MEDIUM_ROAST_COFFEE_BEANS = ITEMS.register("medium_roast_coffee_beans",
            () -> new CoffeeItem(withQuality()));
    public static final DeferredItem<CoffeeItem> DARK_ROAST_COFFEE_BEANS = ITEMS.register("dark_roast_coffee_beans",
            () -> new CoffeeItem(withQuality()));
    // Roasted beans ground in a Coffee Grinder
    public static final DeferredItem<CoffeeItem> LIGHT_ROAST_COFFEE_GROUNDS = ITEMS.register("light_roast_coffee_grounds",
            () -> new CoffeeItem(withQuality()));
    public static final DeferredItem<CoffeeItem> MEDIUM_ROAST_COFFEE_GROUNDS = ITEMS.register("medium_roast_coffee_grounds",
            () -> new CoffeeItem(withQuality()));
    public static final DeferredItem<CoffeeItem> DARK_ROAST_COFFEE_GROUNDS = ITEMS.register("dark_roast_coffee_grounds",
            () -> new CoffeeItem(withQuality()));
    // Light, medium and dark roast grounds drawn together in a Triple Axis Siphon
    public static final DeferredItem<CoffeeItem> RAINBOW_BLEND = ITEMS.register("rainbow_blend",
            () -> new CoffeeItem(withQuality()));

    // A cup of coffee brewed in a Coffee Machine, in a lidded takeaway cup. It remembers the grounds' roast and quality.
    public static final DeferredItem<CupOfCoffeeItem> COFFEE = ITEMS.register("coffee",
            () -> new CupOfCoffeeItem(MobEffects.MOVEMENT_SPEED, cupOfCoffee()));
    // The same, brewed with ice instead of water, in a clear cup with a straw; Fire Resistance instead of Speed
    public static final DeferredItem<CupOfCoffeeItem> ICE_COFFEE = ITEMS.register("ice_coffee",
            () -> new CupOfCoffeeItem(MobEffects.FIRE_RESISTANCE, cupOfCoffee()));
    // Made in a Triple Axis Siphon from Rainbow Blend, water (or ice) and an enchanted golden apple: Speed II (or Fire
    // Resistance) and five more effects
    public static final DeferredItem<GodsCoffeeItem> GODS_COFFEE = ITEMS.register("gods_coffee",
            () -> new GodsCoffeeItem(MobEffects.MOVEMENT_SPEED, 1, godsCoffee()));
    public static final DeferredItem<GodsCoffeeItem> GODS_ICE_COFFEE = ITEMS.register("gods_ice_coffee",
            () -> new GodsCoffeeItem(MobEffects.FIRE_RESISTANCE, 0, godsCoffee()));

    public static final DeferredItem<BlockItem> COFFEE_ROASTER = ITEMS.registerSimpleBlockItem(ModBlocks.COFFEE_ROASTER);
    public static final DeferredItem<BlockItem> COFFEE_MACHINE = ITEMS.registerSimpleBlockItem(ModBlocks.COFFEE_MACHINE);
    public static final DeferredItem<BlockItem> GRINDER_ATTACHMENT = ITEMS.registerSimpleBlockItem(ModBlocks.GRINDER_ATTACHMENT);
    public static final DeferredItem<BlockItem> MILK_FROTHER = ITEMS.registerSimpleBlockItem(ModBlocks.MILK_FROTHER);
    public static final DeferredItem<BlockItem> SYRUP_COMPACTOR = ITEMS.registerSimpleBlockItem(ModBlocks.SYRUP_COMPACTOR);
    public static final DeferredItem<BlockItem> TRIPLE_AXIS_SIPHON = ITEMS.registerSimpleBlockItem(ModBlocks.TRIPLE_AXIS_SIPHON);
    // The guide to the whole mod, a Patchouli book: a book and a raw coffee bean
    public static final DeferredItem<GuideBookItem> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new GuideBookItem(new Item.Properties().stacksTo(1)));
    // 32 uses: each bean ground costs 1
    public static final DeferredItem<CoffeeGrinderItem> COFFEE_GRINDER = ITEMS.register("coffee_grinder",
            () -> new CoffeeGrinderItem(new Item.Properties().durability(32)));

    private ModItems() {
    }

    private static Item.Properties cupOfCoffee() {
        return withQuality().component(ModDataComponents.ROAST.get(), Roast.MEDIUM).stacksTo(16);
    }

    /**
     * Like an enchanted golden apple: an epic (purple) name and the enchantment glint.
     */
    private static Item.Properties godsCoffee() {
        return withQuality().stacksTo(16).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    /**
     * Every kind of bean, and everything made from them, has a quality: decent unless it grew somewhere better or worse.
     */
    private static Item.Properties withQuality() {
        return new Item.Properties().component(ModDataComponents.BEAN_QUALITY.get(), BeanQuality.DECENT);
    }
}
