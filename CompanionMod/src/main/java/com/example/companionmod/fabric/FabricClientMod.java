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

@SuppressWarnings({"rawtypes","unchecked"})
public class FabricClientMod implements ClientModInitializer {
    private static final Logger LOGGER=LogManager.getLogger();
    @Override public void onInitializeClient(){
        LOGGER.info("CompanionMod client initializing");
        ScreenRegistry.register(ScreenHandlerRegistry.COMPANION, (handler, inventory, title) -> new CompanionScreen(handler, inventory, title));
        EntityRendererRegistry.register(EntityTypeRegistry.COMPANION, CompanionEntityRenderer::new);
    }
}
