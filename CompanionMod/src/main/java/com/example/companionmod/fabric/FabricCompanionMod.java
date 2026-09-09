package com.example.companionmod.fabric;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.CommandRegistry;
import com.example.companionmod.registry.EntityTypeRegistry;
import com.example.companionmod.registry.ItemRegistry;
import com.example.companionmod.registry.ScreenHandlerRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricCompanionMod implements ModInitializer {
    public static final String MOD_ID="companionmod";
    private static final Logger LOGGER=LogManager.getLogger();
    @Override public void onInitialize(){
        LOGGER.info("CompanionMod initializing");
        ItemRegistry.init();
        EntityTypeRegistry.init();
        ScreenHandlerRegistry.init();
        FabricDefaultAttributeRegistry.register(EntityTypeRegistry.COMPANION, CompanionEntity.createAttributes());
        CommandRegistry.init();
    }
}
