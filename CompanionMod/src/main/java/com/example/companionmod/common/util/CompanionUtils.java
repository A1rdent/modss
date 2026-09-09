package com.example.companionmod.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public final class CompanionUtils {
    public static final int ORE_SEARCH_RANGE = 14;
    public static final int NORMAL_SEARCH_RANGE = 7;
    public static final int CHEST_RANGE = 8;

    private CompanionUtils() {}

    public static boolean isOre(BlockState state) {
        var block = state.getBlock();
        return block == Blocks.COAL_ORE || block == Blocks.IRON_ORE || block == Blocks.COPPER_ORE
                || block == Blocks.GOLD_ORE || block == Blocks.REDSTONE_ORE || block == Blocks.LAPIS_ORE
                || block == Blocks.DIAMOND_ORE || block == Blocks.EMERALD_ORE
                || block == Blocks.DEEPSLATE_COAL_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.DEEPSLATE_COPPER_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.DEEPSLATE_REDSTONE_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE
                || block == Blocks.DEEPSLATE_DIAMOND_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE;
    }

    /**
     * Blocks the companion is allowed to remove while making a mine.
     * It deliberately excludes bedrock, liquids, chests/utility blocks and obsidian so
     * the worker cannot destroy important bases by accident.
     */
    public static boolean isMineableBlock(BlockState state) {
        var block = state.getBlock();

        if (state.isAir() || !state.getFluidState().isEmpty()) return false;
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block == Blocks.END_PORTAL
                || block == Blocks.END_PORTAL_FRAME || block == Blocks.OBSIDIAN
                || block == Blocks.CRYING_OBSIDIAN || block == Blocks.CHEST
                || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST
                || block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE
                || block == Blocks.SMOKER) return false;

        return state.getDestroySpeed(null, BlockPos.ZERO) >= 0.0F;
    }

    public static BlockPos findNearestBlock(Level level, BlockPos center, int range, Predicate<BlockState> predicate) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    int distanceSq = x * x + y * y + z * z;
                    if (distanceSq > range * range) continue;

                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!predicate.test(state)) continue;

                    if (distanceSq < nearestDistance) {
                        nearestDistance = distanceSq;
                        nearest = pos.immutable();
                    }
                }
            }
        }

        return nearest;
    }

    public static BlockPos findNearestOre(Level level, BlockPos center, int range) {
        return findNearestBlock(level, center, range, CompanionUtils::isOre);
    }

    public static boolean canDepositToChest(ChestBlockEntity chest, ItemStack item) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slot = chest.getItem(i);
            if (slot.isEmpty()) return true;
            if (ItemStack.isSameItemSameTags(slot, item)
                    && slot.getCount() < slot.getMaxStackSize()) return true;
        }
        return false;
    }

    public static void depositToChest(ChestBlockEntity chest, ItemStack item) {
        for (int i = 0; i < chest.getContainerSize() && !item.isEmpty(); i++) {
            ItemStack slot = chest.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, item)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, item.getCount());
                    slot.grow(transfer);
                    item.shrink(transfer);
                }
            }
        }

        for (int i = 0; i < chest.getContainerSize() && !item.isEmpty(); i++) {
            if (chest.getItem(i).isEmpty()) {
                chest.setItem(i, item.copy());
                item.setCount(0);
                return;
            }
        }
    }

    public static boolean hasSolidFloor(Level level, BlockPos pos) {
        BlockState floor = level.getBlockState(pos.below());
        return !floor.isAir() && floor.getFluidState().isEmpty() && floor.isSolidRender(level, pos.below());
    }

    public static boolean canClearForStair(Level level, BlockPos foot, Direction direction) {
        BlockPos body = foot.relative(direction);
        BlockPos head = body.above();
        if (!isMineableBlock(level.getBlockState(body)) && !level.getBlockState(body).isAir()) return false;
        if (!isMineableBlock(level.getBlockState(head)) && !level.getBlockState(head).isAir()) return false;
        return hasSolidFloor(level, body);
    }
}
