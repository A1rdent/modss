package com.example.companionmod.common.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockState;

public class CompanionUtils {
    public static final int MINING_RANGE = 16;
    public static final int CHEST_RANGE = 8;
    public static final int FOLLOW_DISTANCE_TELEPORT = 50;
    public static final int FOLLOW_DISTANCE_WALK = 4;

    public static boolean isMinableBlock(BlockState state) {
        var block = state.getBlock();
        
        return block == Blocks.STONE ||
               block == Blocks.COPPER_ORE ||
               block == Blocks.IRON_ORE ||
               block == Blocks.COAL_ORE ||
               block == Blocks.LAPIS_ORE ||
               block == Blocks.GOLD_ORE ||
               block == Blocks.DIAMOND_ORE ||
               block == Blocks.EMERALD_ORE ||
               block == Blocks.REDSTONE_ORE ||
               block == Blocks.DEEPSLATE ||
               block == Blocks.DIRT ||
               block == Blocks.GRAVEL ||
               block == Blocks.SAND ||
               block == Blocks.RED_SAND ||
               block == Blocks.SANDSTONE ||
               block == Blocks.RED_SANDSTONE ||
               block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.DEEPSLATE_EMERALD_ORE ||
               block == Blocks.DEEPSLATE_REDSTONE_ORE ||
               block == Blocks.RAW_COPPER_BLOCK ||
               block == Blocks.RAW_IRON_BLOCK ||
               block == Blocks.RAW_GOLD_BLOCK;
    }

    public static BlockPos findNearestBlock(Level level, BlockPos center, int range, java.util.function.Predicate<BlockState> predicate) {
        BlockPos nearestBlock = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    
                    if (predicate.test(state)) {
                        double distance = center.distSqr(pos);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestBlock = pos;
                        }
                    }
                }
            }
        }

        return nearestBlock;
    }

    public static boolean canDepositToChest(ChestBlockEntity chest, ItemStack item) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slot = chest.getItem(i);
            
            if (slot.isEmpty()) {
                return true;
            }
            
            if (ItemStack.isSameItemSameTags(slot, item)) {
                if (slot.getCount() < slot.getMaxStackSize()) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public static void depositToChest(ChestBlockEntity chest, ItemStack item) {
        // Try to stack with existing items first
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slot = chest.getItem(i);
            
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, item)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, item.getCount());
                    slot.grow(transfer);
                    item.shrink(transfer);
                    
                    if (item.isEmpty()) {
                        return;
                    }
                }
            }
        }
        
        // Try to place in empty slots
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slot = chest.getItem(i);
            
            if (slot.isEmpty()) {
                chest.setItem(i, item.copy());
                item.setCount(0);
                return;
            }
        }
    }
}
