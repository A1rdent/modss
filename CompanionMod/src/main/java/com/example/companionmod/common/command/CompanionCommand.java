package com.example.companionmod.common.command;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.EntityTypeRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class CompanionCommand {
    private CompanionCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("companion")
                        .then(Commands.literal("summon").executes(CompanionCommand::summon))
                        .then(Commands.literal("stop").executes(CompanionCommand::stop))
        );
    }

    private static int summon(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        CompanionEntity companion = new CompanionEntity(EntityTypeRegistry.COMPANION, level);
        companion.setPos(player.getX() + 1.0D, player.getY(), player.getZ());
        companion.setOwner(player);
        level.addFreshEntity(companion);

        source.sendSuccess(() -> Component.literal("Summoned a companion!"), true);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        int stopped = 0;
        for (CompanionEntity companion : source.getLevel().getEntitiesOfClass(
                CompanionEntity.class, player.getBoundingBox().inflate(50))) {
            if (companion.getOwner() == player) {
                companion.stopAll();
                stopped++;
            }
        }

        final int count = stopped;
        if (count > 0) {
            source.sendSuccess(() -> Component.literal("Stopped " + count + " companion(s)"), true);
        } else {
            source.sendFailure(Component.literal("No companions found"));
        }
        return count;
    }
}
