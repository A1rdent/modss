package com.example.companionmod.common.entity;

import com.example.companionmod.common.util.CompanionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;

public class CompanionAI {
    private final CompanionEntity companion;
    private int tickCounter;
    private BlockPos targetBlock;
    private Direction mineDirection;

    public CompanionAI(CompanionEntity companion) {
        this.companion = companion;
    }

    public void tick() {
        Player owner = companion.getOwner();
        if (owner == null) return;

        tickCounter++;

        if (companion.isMining()) {
            if (tickCounter % 4 == 0) handleMining();
        } else if (companion.isGathering()) {
            if (tickCounter % 4 == 0) handleGathering();
        } else if (companion.isDepositing()) {
            if (tickCounter % 10 == 0) handleAutoDeposit();
        } else if (companion.isFollowing()) {
            handleFollowing(owner);
        }
    }

    private void handleFollowing(Player owner) {
        double distance = companion.distanceToSqr(owner);

        if (distance > 2500.0D) {
            companion.teleportTo(owner.getX() + 1.0D, owner.getY(), owner.getZ() + 1.0D);
            companion.getNavigation().stop();
        } else if (distance > 16.0D) {
            companion.getNavigation().moveTo(owner, 1.0D);
            companion.getLookControl().setLookAt(owner);
        } else {
            companion.getNavigation().stop();
        }
    }

    private void handleMining() {
        // Mining mode automatically collects the drops it creates.
        collectNearbyDrops();

        if (companion.getInventory().isFull()) {
            companion.returnToOwner();
            return;
        }

        // Ores always win over ordinary blocks.
        BlockPos ore = CompanionUtils.findNearestOre(
                companion.getLevel(),
                companion.blockPosition(),
                CompanionUtils.ORE_SEARCH_RANGE
        );

        if (ore != null) {
            targetBlock = ore;
            mineTarget(ore);
            return;
        }

        // No ore nearby: make a safe 2-block-high staircase one level down.
        targetBlock = null;
        mineStairStep();
    }

    private void mineTarget(BlockPos target) {
        if (!CompanionUtils.isMineableBlock(companion.getLevel().getBlockState(target))) {
            targetBlock = null;
            return;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(target));

        if (distance > 9.0D) {
            companion.getNavigation().moveTo(
                    target.getX() + 0.5D,
                    target.getY(),
                    target.getZ() + 0.5D,
                    1.0D
            );
            return;
        }

        companion.getNavigation().stop();
        if (companion.getLevel().destroyBlock(target, true, companion)) {
            targetBlock = null;
        }
    }

    private void mineStairStep() {
        if (mineDirection == null) {
            mineDirection = companion.getDirection();
        }

        Direction[] candidates = new Direction[] {
                mineDirection,
                mineDirection.getClockWise(),
                mineDirection.getCounterClockWise(),
                mineDirection.getOpposite()
        };

        for (Direction direction : candidates) {
            BlockPos foot = companion.blockPosition().relative(direction).below();

            if (!CompanionUtils.canClearForStair(companion.getLevel(), foot, direction)) {
                continue;
            }

            mineDirection = direction;

            // Clear only the next stair step. We never destroy the block beneath ourselves.
            BlockPos body = foot;
            BlockPos head = foot.above();

            if (!companion.getLevel().getBlockState(body).isAir()) {
                companion.getLevel().destroyBlock(body, true, companion);
            }

            if (!companion.getLevel().getBlockState(head).isAir()
                    && CompanionUtils.isMineableBlock(companion.getLevel().getBlockState(head))) {
                companion.getLevel().destroyBlock(head, true, companion);
            }

            companion.getNavigation().moveTo(
                    foot.getX() + 0.5D,
                    foot.getY(),
                    foot.getZ() + 0.5D,
                    1.0D
            );

            return;
        }

        // No safe staircase direction available. Do not dig under the companion.
        companion.getNavigation().stop();
    }

    private void collectNearbyDrops() {
        for (ItemEntity item : companion.getLevel().getEntitiesOfClass(
                ItemEntity.class,
                companion.getBoundingBox().inflate(4.0D)
        )) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty() || !companion.getInventory().canAddItem(stack)) continue;

            companion.getInventory().addItem(stack);

            if (stack.isEmpty()) {
                item.discard();
            } else {
                item.setItem(stack);
            }

            if (companion.getInventory().isFull()) return;
        }
    }

    private void handleGathering() {
        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ItemEntity item : companion.getLevel().getEntitiesOfClass(
                ItemEntity.class, companion.getBoundingBox().inflate(12.0D)
        )) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty() || !companion.getInventory().canAddItem(stack)) continue;

            double distance = companion.distanceToSqr(item);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = item;
            }
        }

        if (nearest == null) {
            companion.getNavigation().stop();
            return;
        }

        if (nearestDistance > 2.25D) {
            companion.getNavigation().moveTo(nearest, 1.1D);
            return;
        }

        ItemStack stack = nearest.getItem();
        companion.getInventory().addItem(stack);

        if (stack.isEmpty()) nearest.discard();
        else nearest.setItem(stack);
    }

    private void handleAutoDeposit() {
        BlockPos chestPos = CompanionUtils.findNearestBlock(
                companion.getLevel(),
                companion.blockPosition(),
                CompanionUtils.CHEST_RANGE,
                state -> state.getBlock() == Blocks.CHEST
        );

        if (chestPos == null) {
            companion.getNavigation().stop();
            return;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(chestPos));

        if (distance > 9.0D) {
            companion.getNavigation().moveTo(
                    chestPos.getX() + 0.5D,
                    chestPos.getY(),
                    chestPos.getZ() + 0.5D,
                    1.0D
            );
            return;
        }

        BlockEntity entity = companion.getLevel().getBlockEntity(chestPos);
        if (entity instanceof ChestBlockEntity chest) {
            depositToChest(chest);
        }
    }

    private void depositToChest(ChestBlockEntity chest) {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            int before = stack.getCount();
            CompanionUtils.depositToChest(chest, stack);

            if (stack.getCount() != before) {
                companion.getInventory().setChanged();
            }
        }
    }
}
