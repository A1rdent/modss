package com.example.companionmod.common;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.registry.EntityTypeRegistry;
import com.example.companionmod.common.entity.CompanionEntity;

@Mod.EventBusSubscriber(modid = CompanionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntitySetupEvents {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(EntityTypeRegistry.COMPANION.get(), CompanionEntity.createAttributes().build());
    }
}
