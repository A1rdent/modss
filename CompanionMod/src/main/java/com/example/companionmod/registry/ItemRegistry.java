package com.example.companionmod.registry;

import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.object.builder.v1.item.FabricItemSettings;
import com.example.companionmod.common.item.CompanionSpawnerItem;
import com.example.companionmod.fabric.FabricCompanionMod;
npublic class ItemRegistry {
    public static final Item COMPANION_SPAWNER = register("companion_spawner", new CompanionSpawnerItem(new FabricItemSettings()));
n    private static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, new ResourceLocation(FabricCompanionMod.MOD_ID, id), item);
    }
n    public static void init() {
        // called from FabricCompanionMod onInitialize to ensure class loads and items register
    }
}
