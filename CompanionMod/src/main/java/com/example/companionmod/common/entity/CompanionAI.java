package com.example.companionmod.common.entity;

import com.example.companionmod.common.util.CompanionUtils;
import net.minecraft.core.BlockPos;
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

    public CompanionAI(CompanionEntity companion) {
        this.companion = companion;
    }

    public void tick() {
        Player owner = this.companion.getOwner();
        if (owner == null) return;

        this.tickCounter++;

        // Only one work mode runs at a time. This prevents following from fighting
        // with mining/gathering/deposit navigation.
        if (this.companion.isMining()) {
            if (this.tickCounter % 5 == 0) this.handleMining();
        } else if (this.companion.isGathering()) {
            if (this.tickCounter % 5 == 0) this.handleGathering();
        } else if (this.companion.isDepositing()) {
            if (this.tickCounter % 10 == 0) this.handleAutoDeposit();
        } else if (this.companion.isFollowing()) {
            this.handleFollowing(owner);
        }
    }

    private void handleFollowing(Player owner) {
        double distance = this.companion.distanceToSqr(owner);

        if (distance > CompanionUtils.FOLLOW_DISTANCE_TELEPORT * CompanionUtils.FOLLOW_DISTANCE_TELEPORT) {
            this.companion.teleportTo(owner.getX() + 1.0D, owner.getY(), owner.getZ() + 1.0D);
            this.companion.getNavigation().stop();
        } else if (distance > CompanionUtils.FOLLOW_DISTANCE_WALK * CompanionUtils.FOLLOW_DISTANCE_WALK) {
            this.companion.getNavigation().moveTo(owner, 1.0D);
            this.companion.getLookControl().setLookAt(owner);
        } else {
            this.companion.getNavigation().stop();
        }
    }

    private void handleMining() {
        if (this.targetBlock == null
                || !CompanionUtils.isMinableBlock(this.companion.level().getBlockState(this.targetBlock))) {
            this.targetBlock = CompanionUtils.findNearestBlock(
                    this.companion.level(),
                    this.companion.blockPosition(),
                    CompanionUtils.MINING_RANGE,
                    CompanionUtils::isMinableBlock
            );
        }

        if (this.targetBlock == null) {
            this.companion.getNavigation().stop();
            return;
        }

        double distance = this.companion.distanceToSqr(Vec3.atCenterOf(this.targetBlock));
        if (distance > 9.0D) {
            this.companion.getNavigation().moveTo(
                    this.targetBlock.getX() + 0.5D,
                    this.targetBlock.getY(),
                    this.targetBlock.getZ() + 0.5D,
                    1.0D);
            return;
        }

        this.companion.getNavigation().stop();
        if (distance < 9.0D) {
            boolean destroyed = this.companion.level().destroyBlock(this.targetBlock, true, this.companion);
            if (destroyed) {
                this.targetBlock = null;
            }
        }
    }

    private void handleGathering() {
        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ItemEntity itemEntity : this.companion.level().getEntitiesOfClass(
                ItemEntity.class, this.companion.getBoundingBox().inflate(12.0D))) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty() || !this.companion.getInventory().canAddItem(stack)) continue;

            double distance = this.companion.distanceToSqr(itemEntity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = itemEntity;
            }
        }

        if (nearest == null) {
            this.companion.getNavigation().stop();
            return;
        }

        if (nearestDistance > 2.25D) {
            this.companion.getNavigation().moveTo(nearest, 1.1D);
            return;
        }

        ItemStack stack = nearest.getItem();
        this.companion.getInventory().addItem(stack);
        if (stack.isEmpty()) {
            nearest.discard();
        } else {
            nearest.setItem(stack);
        }
    }

    private void handleAutoDeposit() {
        BlockPos chestPos = CompanionUtils.findNearestBlock(
                this.companion.level(),
                this.companion.blockPosition(),
                CompanionUtils.CHEST_RANGE,
                state -> state.getBlock() == Blocks.CHEST
        );

        if (chestPos == null) {
            this.companion.getNavigation().stop();
            return;
        }

        double distance = this.companion.distanceToSqr(Vec3.atCenterOf(chestPos));
        if (distance > 9.0D) {
            this.companion.getNavigation().moveTo(
                    chestPos.getX() + 0.5D, chestPos.getY(), chestPos.getZ() + 0.5D, 1.0D);
            return;
        }

        BlockEntity blockEntity = this.companion.level().getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            this.depositToChest(chest);
        }
    }

    private void depositToChest(ChestBlockEntity chest) {
        for (int i = 0; i < this.companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.companion.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            int before = stack.getCount();
            CompanionUtils.depositToChest(chest, stack);
            if (stack.getCount() != before) {
                this.companion.getInventory().setChanged();
            }
        }
    }
}
