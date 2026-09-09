package com.example.companionmod.common.item;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

import com.example.companionmod.registry.EntityTypeRegistry;
import com.example.companionmod.common.entity.CompanionEntity;

public class CompanionSpawnerItem extends Item {
    public CompanionSpawnerItem(Item.Properties properties) {
        super(properties
                .stacksTo(1)
                .tab(null)); // Creative tab, null for now
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && level.random.nextFloat() < 1.0f) {
            CompanionEntity companion = new CompanionEntity(EntityTypeRegistry.COMPANION.get(), level);
            companion.setPos(player.getX(), player.getY(), player.getZ());
            companion.setOwner(player);
            companion.setHealth(20.0f);
            level.addFreshEntity(companion);

            if (!player.isCreative()) {
                itemStack.shrink(1);
            }

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
