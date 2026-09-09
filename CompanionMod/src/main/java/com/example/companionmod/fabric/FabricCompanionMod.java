package com.example.companionmod.fabric;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.example.companionmod.registry.ItemRegistry;
import com.example.companionmod.registry.EntityTypeRegistry;

public class FabricCompanionMod implements ModInitializer {
    public static final String MOD_ID = "companionmod";
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("CompanionMod (Fabric) initializing");
        ItemRegistry.init();
        EntityTypeRegistry.init();
    }
}
