package com.example.companionmod.common.item;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.EntityTypeRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CompanionSpawnerItem extends Item {
    public CompanionSpawnerItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        CompanionEntity companion = new CompanionEntity(EntityTypeRegistry.COMPANION, level);
        companion.setPos(player.getX() + 1.0D, player.getY(), player.getZ());
        companion.setOwner(player);
        level.addFreshEntity(companion);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}
