package com.example.companionmod.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import com.example.companionmod.registry.EntityTypeRegistry;
import com.example.companionmod.common.entity.CompanionEntity;

public class CompanionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("companion")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("summon")
                                .executes(CompanionCommand::summon)
                        )
                        .then(Commands.literal("stop")
                                .executes(CompanionCommand::stop)
                        )
        );
    }

    private static int summon(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            Player player = source.getPlayerOrException();
            ServerLevel level = source.getLevel();

            CompanionEntity companion = new CompanionEntity(EntityTypeRegistry.COMPANION, level);
            companion.setPos(player.getX(), player.getY(), player.getZ());
            companion.setOwner(player);
            companion.setHealth(20.0f);
            level.addFreshEntity(companion);

            source.sendSuccess(() -> Component.literal("Summoned a companion!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to summon companion: " + e.getMessage()));
            return 0;
        }
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            Player player = source.getPlayerOrException();

            // Find nearby companions and stop them
            int stopped = 0;
            for (CompanionEntity companion : source.getLevel()
                    .getEntitiesOfClass(CompanionEntity.class, 
                    player.getBoundingBox().inflate(50))) {
                if (companion.getOwner() == player) {
                    companion.stopAll();
                    stopped++;
                }
            }

            if (stopped > 0) {
                source.sendSuccess(() -> Component.literal("Stopped " + stopped + " companion(s)"), true);
            } else {
                source.sendFailure(Component.literal("No companions found"));
            }
            return stopped;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
