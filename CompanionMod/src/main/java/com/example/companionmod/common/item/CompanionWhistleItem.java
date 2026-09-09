package com.example.companionmod.common.item;

import com.example.companionmod.common.entity.CompanionEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CompanionWhistleItem extends Item {
    public CompanionWhistleItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompanionEntity nearest = null;
            double best = Double.MAX_VALUE;
            for (CompanionEntity companion : level.getEntitiesOfClass(
                    CompanionEntity.class, player.getBoundingBox().inflate(256.0D))) {
                if (!companion.isOwnedBy(player)) continue;
                double distance = player.distanceToSqr(companion);
                if (distance < best) {
                    best = distance;
                    nearest = companion;
                }
            }
            if (nearest != null && nearest.returnToOwner()) {
                player.getCooldowns().addCooldown(this, 40);
                player.playSound(SoundEvents.NOTE_BLOCK_BELL, 0.8F, 1.4F);
                player.displayClientMessage(Component.translatable("message.companionmod.whistle.success"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.companionmod.whistle.none"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
