package com.example.companionmod.common.command;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.EntityTypeRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
    private static int summon(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source=context.getSource(); Player player=source.getPlayerOrException(); ServerLevel level=source.getLevel();
        CompanionEntity companion=EntityTypeRegistry.COMPANION.create(level);
        if(companion==null){source.sendFailure(Component.literal("Could not create companion entity.")); return 0;}
        companion.moveTo(player.getX()+1.5D,player.getY(),player.getZ()+1.5D,player.getYRot(),0.0F);
        companion.setOwner(player); level.addFreshEntity(companion);
        source.sendSuccess(Component.literal("Companion summoned. Use /companion help."), false); return 1;
    }
    private enum Mode{FOLLOW,MINE,GATHER,DEPOSIT}
    private static int setMode(CommandContext<CommandSourceStack> context,Mode mode) throws CommandSyntaxException {
        Player player=context.getSource().getPlayerOrException(); CompanionEntity companion=findNearestOwned(player);
        if(companion==null){context.getSource().sendFailure(Component.literal("No owned companion within 64 blocks.")); return 0;}
        companion.stopAll();
        switch(mode){case FOLLOW->companion.setFollowing(true);case MINE->companion.setMining(true);case GATHER->companion.setGathering(true);case DEPOSIT->companion.setDepositing(true);}
        context.getSource().sendSuccess(Component.literal("Companion mode: "+mode.name().toLowerCase()),false); return 1;
    }
    private static int stop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player=context.getSource().getPlayerOrException(); int stopped=0;
        for(CompanionEntity c:player.getLevel().getEntitiesOfClass(CompanionEntity.class,player.getBoundingBox().inflate(64.0D))) if(c.isOwnedBy(player)){c.stopAll(); stopped++;}
        context.getSource().sendSuccess(Component.literal("Stopped "+stopped+" companion(s)."),false); return stopped;
    }
    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player=context.getSource().getPlayerOrException(); CompanionEntity c=findNearestOwned(player);
        if(c==null){context.getSource().sendFailure(Component.literal("No owned companion within 64 blocks."));return 0;}
        context.getSource().sendSuccess(Component.literal("Companion: "+c.getModeName()+" | HP "+(int)c.getHealth()+"/"+(int)c.getMaxHealth()),false);return 1;
    }
    private static int help(CommandContext<CommandSourceStack> context){
        context.getSource().sendSuccess(Component.literal("/companion summon | follow | mine | gather | deposit | stop | status"),false);return 1;
    }
    private static CompanionEntity findNearestOwned(Player player){
        CompanionEntity nearest=null; double best=Double.MAX_VALUE;
        for(CompanionEntity c:player.getLevel().getEntitiesOfClass(CompanionEntity.class,player.getBoundingBox().inflate(64.0D))){
            if(c.isOwnedBy(player)){double d=player.distanceToSqr(c);if(d<best){best=d;nearest=c;}}
        } return nearest;
    }
}
