package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import com.caffeinecubed.coffee.BeanQuality;
import com.caffeinecubed.coffee.Roast;
import com.caffeinecubed.coffee.Syrup;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CaffeineCubed.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.caffeinecubed"))
            .icon(() -> new ItemStack(ModItems.COFFEE.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.GUIDE_BOOK.get());
                output.accept(ModItems.COFFEE_ROASTER.get());
                output.accept(ModItems.COFFEE_GRINDER.get());
                output.accept(ModItems.COFFEE_MACHINE.get());
                output.accept(ModItems.GRINDER_ATTACHMENT.get());
                output.accept(ModItems.MILK_FROTHER.get());
                output.accept(ModItems.SYRUP_COMPACTOR.get());
                output.accept(ModItems.TRIPLE_AXIS_SIPHON.get());
                // Every roast of beans, lightest first, then the grounds and the Rainbow Blend, each in every quality
                // from worst to best
                for (ItemLike roast : List.of(ModItems.RAW_COFFEE_BEANS, ModItems.LIGHT_ROAST_COFFEE_BEANS,
                        ModItems.MEDIUM_ROAST_COFFEE_BEANS, ModItems.DARK_ROAST_COFFEE_BEANS, ModItems.LIGHT_ROAST_COFFEE_GROUNDS,
                        ModItems.MEDIUM_ROAST_COFFEE_GROUNDS, ModItems.DARK_ROAST_COFFEE_GROUNDS, ModItems.RAINBOW_BLEND)) {
                    for (BeanQuality quality : BeanQuality.values()) {
                        ItemStack beans = new ItemStack(roast);
                        beans.set(ModDataComponents.BEAN_QUALITY.get(), quality);
                        output.accept(beans);
                    }
                }
                // A cup of coffee of every roast and quality, without milk and then with it, without a syrup and then with
                // each one; then all of those again as Ice Coffee
                List<Syrup> syrups = new ArrayList<>();
                syrups.add(null);
                syrups.addAll(List.of(Syrup.values()));
                for (ItemLike cup : List.of(ModItems.COFFEE, ModItems.ICE_COFFEE)) {
                    for (Syrup syrup : syrups) {
                        for (boolean milk : new boolean[]{false, true}) {
                            for (Roast roast : Roast.values()) {
                                for (BeanQuality quality : BeanQuality.values()) {
                                    ItemStack coffee = new ItemStack(cup);
                                    coffee.set(ModDataComponents.ROAST.get(), roast);
                                    coffee.set(ModDataComponents.BEAN_QUALITY.get(), quality);
                                    if (milk) {
                                        coffee.set(ModDataComponents.MILK.get(), Unit.INSTANCE);
                                    }
                                    if (syrup != null) {
                                        coffee.set(ModDataComponents.SYRUP.get(), syrup);
                                    }
                                    output.accept(coffee);
                                }
                            }
                        }
                    }
                }
                // God's Coffee and God's Ice Coffee in every quality
                for (ItemLike cup : List.of(ModItems.GODS_COFFEE, ModItems.GODS_ICE_COFFEE)) {
                    for (BeanQuality quality : BeanQuality.values()) {
                        ItemStack coffee = new ItemStack(cup);
                        coffee.set(ModDataComponents.BEAN_QUALITY.get(), quality);
                        output.accept(coffee);
                    }
                }
            })
            .build());

    private ModCreativeTabs() {
    }
}
