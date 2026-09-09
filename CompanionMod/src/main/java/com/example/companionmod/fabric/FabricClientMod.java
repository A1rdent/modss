package com.example.companionmod.fabric;

import com.example.companionmod.client.gui.CompanionScreen;
import com.example.companionmod.client.render.CompanionEntityRenderer;
import com.example.companionmod.registry.EntityTypeRegistry;
import com.example.companionmod.registry.ScreenHandlerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FabricClientMod implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("CompanionMod client initializing");
        ScreenRegistry.register(ScreenHandlerRegistry.COMPANION,
                (ScreenRegistry.Factory<net.minecraft.world.inventory.AbstractContainerMenu, CompanionScreen>) (handler, inventory, title) -> new CompanionScreen((com.example.companionmod.client.gui.CompanionScreenHandler) handler, inventory, title));
        EntityRendererRegistry.register(EntityTypeRegistry.COMPANION, CompanionEntityRenderer::new);
    }
}
