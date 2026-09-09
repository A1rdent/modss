package com.example.companionmod.registry;

import com.example.companionmod.client.gui.CompanionScreenHandler;
import com.example.companionmod.fabric.FabricCompanionMod;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public final class ScreenHandlerRegistry {
    private ScreenHandlerRegistry() {}

    public static final ExtendedScreenHandlerType<CompanionScreenHandler> COMPANION = Registry.register(
            Registry.MENU,
            new ResourceLocation(FabricCompanionMod.MOD_ID, "companion"),
            new ExtendedScreenHandlerType<>(CompanionScreenHandler::new)
    );

    public static void init() {}
}
