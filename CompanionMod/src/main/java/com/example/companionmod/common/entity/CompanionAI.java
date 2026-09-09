package com.example.companionmod.common.entity;

import com.example.companionmod.common.util.CompanionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CompanionAI {
    private final CompanionEntity companion;
    private int tickCounter;
    private BlockPos targetBlock;
    private Direction mineDirection;
    private int workCooldown;

    public CompanionAI(CompanionEntity companion) {
        this.companion = companion;
    }

    public void tick() {
        Player owner = companion.getOwner();
        if (owner == null) return;

        tickCounter++;
        if (workCooldown > 0) workCooldown--;

        if (companion.isMining()) {
            if (tickCounter % 2 == 0) handleMining();
        } else if (companion.isWoodcutting()) {
            if (tickCounter % 2 == 0) handleWoodcutting();
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
        collectNearbyDrops();

        int toolSlot = findPickaxeSlot();
        if (toolSlot < 0) {
            stopForMissingTool("Для добычи нужна кирка в инвентаре.");
            return;
        }

        if (companion.getInventory().isFull()) {
            companion.returnToOwner();
            return;
        }

        // Ores have absolute priority, wherever possible in the search radius.
        BlockPos ore = CompanionUtils.findNearestOre(
                companion.getLevel(), companion.blockPosition(), CompanionUtils.ORE_SEARCH_RANGE);

        if (ore != null) {
            targetBlock = ore;
            if (mineTargetWithPickaxe(ore, toolSlot)) return;
        }

        // No ore or ore is not currently reachable: excavate a safe 2x2 staircase.
        mineStairStep(toolSlot);
    }

    private void handleWoodcutting() {
        collectNearbyDrops();

        int toolSlot = findAxeSlot();
        if (toolSlot < 0) {
            stopForMissingTool("Для рубки леса нужен топор в инвентаре.");
            return;
        }

        BlockPos log = CompanionUtils.findNearestBlock(
                companion.getLevel(), companion.blockPosition(), 12,
                CompanionUtils::isWoodBlock);

        if (log == null) {
            companion.getNavigation().stop();
            return;
        }

        mineTargetWithAxe(log, toolSlot);
    }

    private boolean mineTargetWithPickaxe(BlockPos target, int toolSlot) {
        return mineTargetWithTool(target, toolSlot, false);
    }

    private boolean mineTargetWithAxe(BlockPos target, int toolSlot) {
        return mineTargetWithTool(target, toolSlot, true);
    }

    private boolean mineTargetWithTool(BlockPos target, int toolSlot, boolean woodOnly) {
        BlockState state = companion.getLevel().getBlockState(target);

        if (state.isAir() || (woodOnly && !CompanionUtils.isWoodBlock(state))
                || (!woodOnly && CompanionUtils.isWoodBlock(state))
                || (!woodOnly && !CompanionUtils.isMineableBlock(state))) {
            targetBlock = null;
            return false;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(target));
        if (distance > 7.0D) {
            companion.getNavigation().moveTo(
                    target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.0D);
            return true;
        }

        if (workCooldown > 0) return true;
        companion.getNavigation().stop();

        if (breakBlock(target, toolSlot)) {
            targetBlock = null;
            workCooldown = 7;
            return true;
        }

        targetBlock = null;
        return false;
    }

    private void mineStairStep(int pickaxeSlot) {
        if (mineDirection == null) mineDirection = companion.getDirection();

        Direction[] candidates = new Direction[] {
                mineDirection,
                mineDirection.getClockWise(),
                mineDirection.getCounterClockWise(),
                mineDirection.getOpposite()
        };

        for (Direction direction : candidates) {
            Direction width = direction.getClockWise();
            BlockPos step = companion.blockPosition().relative(direction).below();

            // A 2x2 corridor: two blocks wide, two blocks tall.
            BlockPos a = step;
            BlockPos b = step.relative(width);
            BlockPos aHead = a.above();
            BlockPos bHead = b.above();

            if (!canDigCorridorBlock(a) || !canDigCorridorBlock(b)
                    || !canDigCorridorBlock(aHead) || !canDigCorridorBlock(bHead)) {
                continue;
            }

            if (!CompanionUtils.hasSolidFloor(companion.getLevel(), a)
                    || !CompanionUtils.hasSolidFloor(companion.getLevel(), b)) {
                continue;
            }

            mineDirection = direction;

            if (workCooldown > 0) return;

            // Never remove the block under the companion. Only excavate the next lower step.
            if (!a.equals(b) && !companion.getLevel().getBlockState(a).isAir()) {
                if (!breakBlock(a, pickaxeSlot)) return;
                workCooldown = 7;
                return;
            }
            if (!companion.getLevel().getBlockState(b).isAir()) {
                if (!breakBlock(b, pickaxeSlot)) return;
                workCooldown = 7;
                return;
            }
            if (!companion.getLevel().getBlockState(aHead).isAir()) {
                if (!breakBlock(aHead, pickaxeSlot)) return;
                workCooldown = 7;
                return;
            }
            if (!companion.getLevel().getBlockState(bHead).isAir()) {
                if (!breakBlock(bHead, pickaxeSlot)) return;
                workCooldown = 7;
                return;
            }

            companion.getNavigation().moveTo(
                    a.getX() + 0.5D, a.getY(), a.getZ() + 0.5D, 1.0D);
            return;
        }

        companion.getNavigation().stop();
    }

    private boolean canDigCorridorBlock(BlockPos pos) {
        BlockState state = companion.getLevel().getBlockState(pos);
        return state.isAir() || CompanionUtils.isMineableBlock(state);
    }

    private boolean breakBlock(BlockPos pos, int toolSlot) {
        BlockState state = companion.getLevel().getBlockState(pos);
        if (state.isAir() || !CompanionUtils.isMineableBlock(state)) return false;

        ItemStack tool = companion.getInventory().getItem(toolSlot);
        if (tool.isEmpty() || !(tool.getItem() instanceof PickaxeItem || tool.getItem() instanceof AxeItem)) {
            return false;
        }

        if ((CompanionUtils.isWoodBlock(state) && !(tool.getItem() instanceof AxeItem))
                || (!CompanionUtils.isWoodBlock(state) && !(tool.getItem() instanceof PickaxeItem))) {
            return false;
        }

        if (!(companion.getLevel() instanceof ServerLevel serverLevel)) return false;

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        for (ItemStack drop : Block.getDrops(state, serverLevel, pos, blockEntity, companion, tool.copy())) {
            addDropToInventory(drop);
        }

        serverLevel.levelEvent(2001, pos, Block.getId(state));
        serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        tool.hurtAndBreak(1, companion, entity -> {});
        companion.getInventory().setItem(toolSlot, tool);
        return true;
    }

    private void addDropToInventory(ItemStack drop) {
        if (drop.isEmpty()) return;
        ItemStack remaining = drop.copy();
        companion.getInventory().addItem(remaining);

        if (!remaining.isEmpty()) {
            companion.getLevel().addFreshEntity(
                    new ItemEntity(companion.getLevel(),
                            companion.getX(), companion.getY() + 0.4D, companion.getZ(), remaining));
        }
    }

    private void collectNearbyDrops() {
        for (ItemEntity item : companion.getLevel().getEntitiesOfClass(
                ItemEntity.class, companion.getBoundingBox().inflate(4.0D))) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty() || !companion.getInventory().canAddItem(stack)) continue;

            companion.getInventory().addItem(stack);

            if (stack.isEmpty()) item.discard();
            else item.setItem(stack);

            if (companion.getInventory().isFull()) return;
        }
    }

    private int findPickaxeSlot() {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.getItem() instanceof PickaxeItem && stack.getDamageValue() < stack.getMaxDamage() - 1) {
                return i;
            }
        }
        return -1;
    }

    private int findAxeSlot() {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.getItem() instanceof AxeItem && stack.getDamageValue() < stack.getMaxDamage() - 1) {
                return i;
            }
        }
        return -1;
    }

    private void stopForMissingTool(String message) {
        companion.stopAll();
        Player owner = companion.getOwner();
        if (owner != null) {
            owner.displayClientMessage(net.minecraft.network.chat.Component.literal(message), true);
        }
    }

    private void handleGathering() {
        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ItemEntity item : companion.getLevel().getEntitiesOfClass(
                ItemEntity.class, companion.getBoundingBox().inflate(12.0D))) {
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
                companion.getLevel(), companion.blockPosition(), CompanionUtils.CHEST_RANGE,
                state -> state.getBlock() == Blocks.CHEST);

        if (chestPos == null) {
            companion.getNavigation().stop();
            return;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(chestPos));

        if (distance > 9.0D) {
            companion.getNavigation().moveTo(
                    chestPos.getX() + 0.5D, chestPos.getY(), chestPos.getZ() + 0.5D, 1.0D);
            return;
        }

        BlockEntity entity = companion.getLevel().getBlockEntity(chestPos);
        if (entity instanceof ChestBlockEntity chest) depositToChest(chest);
    }

    private void depositToChest(ChestBlockEntity chest) {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            int before = stack.getCount();
            CompanionUtils.depositToChest(chest, stack);
            if (stack.getCount() != before) companion.getInventory().setChanged();
        }
    }
}
