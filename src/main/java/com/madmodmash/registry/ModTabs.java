package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MadModMash.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EMPIRE = tab("empire", () -> ModItems.EMERALD_GILDAN, "empire");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARSENAL = tab("arsenal", () -> ModItems.DIAMOND_CLAYMORE, "arsenal", "armor");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS = tab("tools", () -> ModItems.GRAPPLE_HOOK, "tools");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARCANA = tab("arcana", () -> ModItems.VISTREN_POWDER, "arcana");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS = tab("materials", () -> ModItems.DRAGON_SCALE, "materials");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS = tab("blocks", () -> ModItems.MYSTERY_ORE, "blocks", "bricks", "statues");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATURES = tab("creatures", () -> ModItems.MONKEY_SPAWN_EGG, "creatures");

    private static DeferredHolder<CreativeModeTab, CreativeModeTab> tab(String name, Supplier<DeferredItem<? extends Item>> icon, String... groups) {
        return TABS.register(name, () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + MadModMash.MODID + "." + name))
                .icon(() -> new ItemStack(icon.get().get()))
                .displayItems((params, out) -> {
                    for (String g : groups) {
                        List<DeferredItem<? extends Item>> items = ModItems.TAB_CONTENTS.get(g);
                        if (items != null) items.forEach(i -> out.accept(i.get()));
                    }
                })
                .build());
    }

    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            List<DeferredItem<? extends Item>> eggs = ModItems.TAB_CONTENTS.get("creatures");
            if (eggs != null) eggs.forEach(e -> event.accept(e.get()));
        }
    }

    private ModTabs() {}
}
