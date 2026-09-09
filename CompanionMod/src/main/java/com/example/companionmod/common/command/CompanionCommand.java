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
        dispatcher.register(Commands.literal("companion")
                .requires(source -> source.getEntity() instanceof Player)
                .then(Commands.literal("summon").executes(CompanionCommand::summon))
                .then(Commands.literal("follow").executes(ctx -> setMode(ctx, Mode.FOLLOW)))
                .then(Commands.literal("mine").executes(ctx -> setMode(ctx, Mode.MINE)))
                .then(Commands.literal("gather").executes(ctx -> setMode(ctx, Mode.GATHER)))
                .then(Commands.literal("deposit").executes(ctx -> setMode(ctx, Mode.DEPOSIT)))
                .then(Commands.literal("stop").executes(CompanionCommand::stop))
                .then(Commands.literal("status").executes(CompanionCommand::status))
                .then(Commands.literal("help").executes(CompanionCommand::help)));
    }

    private static int summon(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();

        CompanionEntity companion = new CompanionEntity(EntityTypeRegistry.COMPANION, level);
        companion.moveTo(player.getX() + 1.5D, player.getY(), player.getZ() + 1.5D, player.getYRot(), 0.0F);
        companion.setOwner(player);
        level.addFreshEntity(companion);

        context.getSource().sendSuccess(() -> Component.literal("Companion summoned. Use /companion help for commands."), false);
        return 1;
    }

    private enum Mode { FOLLOW, MINE, GATHER, DEPOSIT }

    private static int setMode(CommandContext<CommandSourceStack> context, Mode mode) {
        Player player = context.getSource().getPlayerOrException();
        CompanionEntity companion = findNearestOwned(player);
        if (companion == null) {
            context.getSource().sendFailure(Component.literal("No owned companion found within 64 blocks."));
            return 0;
        }

        companion.stopAll();
        switch (mode) {
            case FOLLOW -> companion.setFollowing(true);
            case MINE -> companion.setMining(true);
            case GATHER -> companion.setGathering(true);
            case DEPOSIT -> companion.setDepositing(true);
        }

        String name = mode.name().toLowerCase();
        context.getSource().sendSuccess(() -> Component.literal("Companion mode: " + name), false);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayerOrException();
        int stopped = 0;
        for (CompanionEntity companion : player.level().getEntitiesOfClass(
                CompanionEntity.class, player.getBoundingBox().inflate(64.0D))) {
            if (companion.isOwnedBy(player)) {
                companion.stopAll();
                stopped++;
            }
        }

        final int count = stopped;
        context.getSource().sendSuccess(() -> Component.literal(
                count == 0 ? "No owned companions found." : "Stopped " + count + " companion(s)."), false);
        return stopped;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayerOrException();
        CompanionEntity companion = findNearestOwned(player);
        if (companion == null) {
            context.getSource().sendFailure(Component.literal("No owned companion found within 64 blocks."));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal(
                "Companion: " + companion.getModeName()
                        + " | HP " + (int) companion.getHealth() + "/" + (int) companion.getMaxHealth()
                        + " | inventory " + companion.getInventory().getContainerSize() + " slots"), false);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(
                "/companion summon | follow | mine | gather | deposit | stop | status"), false);
        return 1;
    }

    private static CompanionEntity findNearestOwned(Player player) {
        CompanionEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (CompanionEntity companion : player.level().getEntitiesOfClass(
                CompanionEntity.class, player.getBoundingBox().inflate(64.0D))) {
            if (!companion.isOwnedBy(player)) continue;
            double distance = player.distanceToSqr(companion);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = companion;
            }
        }
        return nearest;
    }
}
