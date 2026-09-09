package com.example.companionmod.common.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class CompanionAI {
    private final CompanionEntity companion;
    private int tickCounter = 0;
    private BlockPos targetBlock = null;

    public CompanionAI(CompanionEntity companion) {
        this.companion = companion;
    }

    public void tick() {
        Player owner = this.companion.getOwner();
        if (owner == null) return;

        this.tickCounter++;

        // Handle following
        if (this.companion.isFollowing()) {
            this.handleFollowing(owner);
        }

        // Handle mining
        if (this.companion.isMining() && this.tickCounter % 10 == 0) {
            this.handleMining();
        }

        // Handle gathering
        if (this.companion.isGathering()) {
            this.handleGathering();
        }

        // Handle auto-deposit every 40 ticks
        if (this.tickCounter % 40 == 0) {
            this.handleAutoDeposit();
        }
    }

    private void handleFollowing(Player owner) {
        double distance = this.companion.distanceToSqr(owner);
        
        if (distance > 2500) { // Distance > 50 blocks, teleport
            this.companion.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        } else if (distance > 16) { // Distance > 4 blocks, walk towards
            this.companion.getNavigation().moveTo(owner, 1.0D);
            this.companion.getLookControl().setLookAt(owner);
        }
    }

    private void handleMining() {
        if (this.targetBlock == null || !this.canMineBlock(this.targetBlock)) {
            this.targetBlock = this.findNearestMinableBlock();
        }

        if (this.targetBlock != null) {
            this.mineBlock(this.targetBlock);
        }
    }

    private void handleGathering() {
        // Logic for gathering items from ground
        // Will be implemented in the full version
    }

    private void handleAutoDeposit() {
        BlockPos chestPos = this.findNearestChest();
        if (chestPos != null) {
            this.depositToChest(chestPos);
        }
    }

    private BlockPos findNearestMinableBlock() {
        int range = 16;
        BlockPos companionPos = this.companion.blockPosition();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = companionPos.offset(x, y, z);
                    if (this.canMineBlock(pos)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private boolean canMineBlock(BlockPos pos) {
        if (pos == null) return false;
        
        var block = this.companion.level().getBlockState(pos).getBlock();
        
        // Whitelist of minable blocks
        return block == Blocks.STONE ||
               block == Blocks.COPPER_ORE ||
               block == Blocks.IRON_ORE ||
               block == Blocks.COAL_ORE ||
               block == Blocks.LAPIS_ORE ||
               block == Blocks.GOLD_ORE ||
               block == Blocks.DIAMOND_ORE ||
               block == Blocks.DEEPSLATE ||
               block == Blocks.DIRT ||
               block == Blocks.GRAVEL ||
               block == Blocks.SAND ||
               block == Blocks.SANDSTONE ||
               block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.DEEPSLATE_DIAMOND_ORE;
    }

    private void mineBlock(BlockPos pos) {
        double distance = this.companion.distanceToSqr(Vec3.atCenterOf(pos));
        
        if (distance < 64) { // Within 8 blocks
            this.companion.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0D);
            
            if (distance < 9) { // Close enough to break
                this.companion.level().destroyBlock(pos, true);
            }
        }
    }

    private BlockPos findNearestChest() {
        int range = 8;
        BlockPos companionPos = this.companion.blockPosition();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = companionPos.offset(x, y, z);
                    if (this.companion.level().getBlockState(pos).getBlock() == Blocks.CHEST) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private void depositToChest(BlockPos chestPos) {
        // Implementation for depositing items to chest
        // Will be expanded in the full version
    }
}
