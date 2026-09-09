package com.example.companionmod.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import com.example.companionmod.client.gui.CompanionScreenHandler;
import com.example.companionmod.fabric.FabricCompanionMod;

public class ScreenHandlerRegistry {
    public static final MenuType<CompanionScreenHandler> COMPANION = register("companion", new MenuType<>(CompanionScreenHandler::new));

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> register(String id, MenuType<T> type) {
        return Registry.register(Registry.MENU, new ResourceLocation(FabricCompanionMod.MOD_ID, id), type);
    }

    public static void init() {
        // ensure static init
    }
}
