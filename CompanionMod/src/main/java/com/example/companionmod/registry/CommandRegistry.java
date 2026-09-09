package com.example.companionmod.registry;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.example.companionmod.common.command.CompanionCommand;

public class CommandRegistry {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CompanionCommand.register(dispatcher);
        });
    }
}
