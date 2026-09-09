package com.example.companionmod.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import com.example.companionmod.common.item.CompanionSpawnerItem;
import com.example.companionmod.fabric.FabricCompanionMod;

public final class ItemRegistry {
    public static final Item COMPANION_SPAWNER = Registry.register(
            Registry.ITEM,
            new ResourceLocation(FabricCompanionMod.MOD_ID, "companion_spawner"),
            new CompanionSpawnerItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC))
    );

    private ItemRegistry() {}

    public static void init() {
        // Forces class initialization and registration.
    }
}
