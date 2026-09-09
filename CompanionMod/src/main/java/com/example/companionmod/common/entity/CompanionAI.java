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

        if (this.companion.isFollowing()) {
            this.handleFollowing(owner);
        }

        if (this.companion.isMining() && this.tickCounter % 10 == 0) {
            this.handleMining();
        }

        if (this.companion.isGathering() && this.tickCounter % 5 == 0) {
            this.handleGathering();
        }

        if (this.companion.isDepositing() && this.tickCounter % 20 == 0) {
            this.handleAutoDeposit();
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
        }
    }

    private void handleMining() {
        if (this.targetBlock == null || !CompanionUtils.isMinableBlock(this.companion.level().getBlockState(this.targetBlock))) {
            this.targetBlock = CompanionUtils.findNearestBlock(
                    this.companion.level(),
                    this.companion.blockPosition(),
                    CompanionUtils.MINING_RANGE,
                    CompanionUtils::isMinableBlock
            );
        }

        if (this.targetBlock == null) return;

        double distance = this.companion.distanceToSqr(Vec3.atCenterOf(this.targetBlock));
        if (distance < 64) {
            this.companion.getNavigation().moveTo(
                    this.targetBlock.getX(), this.targetBlock.getY(), this.targetBlock.getZ(), 1.0D);

            if (distance < 9) {
                boolean destroyed = this.companion.level().destroyBlock(this.targetBlock, true, this.companion);
                if (destroyed) {
                    this.targetBlock = null;
                }
            }
        }
    }

    private void handleGathering() {
        for (ItemEntity itemEntity : this.companion.level().getEntitiesOfClass(
                ItemEntity.class, this.companion.getBoundingBox().inflate(4.0D))) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty() || !this.companion.getInventory().canAddItem(stack)) continue;

            ItemStack before = stack.copy();
            this.companion.getInventory().addItem(stack);

            if (stack.isEmpty()) {
                itemEntity.discard();
            } else if (stack.getCount() != before.getCount()) {
                itemEntity.setItem(stack);
            }
        }
    }

    private void handleAutoDeposit() {
        BlockPos chestPos = CompanionUtils.findNearestBlock(
                this.companion.level(),
                this.companion.blockPosition(),
                CompanionUtils.CHEST_RANGE,
                state -> state.getBlock() == Blocks.CHEST
        );

        if (chestPos == null) return;
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
