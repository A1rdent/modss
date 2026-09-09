package com.example.companionmod.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.ServerStartingEvent;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.common.command.CompanionCommand;

@Mod.EventBusSubscriber(modid = CompanionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CompanionCommand.register(event.getServer().getCommands().getDispatcher());
    }
}
