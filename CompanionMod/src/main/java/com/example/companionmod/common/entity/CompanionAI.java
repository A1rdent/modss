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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class CompanionAI {
    private final CompanionEntity companion;
    private int tickCounter;
    private BlockPos targetBlock;
    private BlockPos lastTreeLog;
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

        int pickaxeSlot = findPickaxeSlot();
        if (pickaxeSlot < 0) {
            stopForMissingTool("Для шахты нужна кирка в инвентаре.");
            return;
        }

        if (companion.getInventory().isFull()) {
            companion.returnToOwner();
            return;
        }

        if (mineDirection == null || !mineDirection.getAxis().isHorizontal()) {
            mineDirection = companion.getDirection();
        }

        // First look for ore close to the current tunnel. Ore is always the priority.
        BlockPos ore = CompanionUtils.findNearestOre(
                companion.getLevel(), companion.blockPosition(), 10);
        if (ore != null) {
            targetBlock = ore;
            if (mineBlockWithTool(ore, pickaxeSlot, false)) {
                return;
            }
        }

        // No ore: always continue the 2x2 staircase instead of standing still.
        targetBlock = null;
        mineStaircaseStep(pickaxeSlot);
    }

    private void mineStaircaseStep(int pickaxeSlot) {
        Direction direction = mineDirection;
        Direction width = direction.getClockWise();

        // The next walkable position is exactly one block lower and one block forward.
        BlockPos nextFloor = companion.blockPosition().relative(direction).below();
        BlockPos nextFloorSide = nextFloor.relative(width);
        BlockPos nextHead = nextFloor.above();
        BlockPos nextHeadSide = nextFloorSide.above();

        // We need a real floor one block below the next step. Never dig ourselves into empty space.
        if (!CompanionUtils.hasSolidFloor(companion.getLevel(), nextFloor)
                || !CompanionUtils.hasSolidFloor(companion.getLevel(), nextFloorSide)) {
            // Try turning the corridor rather than digging straight down into a cave.
            Direction alternate = direction.getClockWise();
            BlockPos alt = companion.blockPosition().relative(alternate).below();
            BlockPos altSide = alt.relative(alternate.getClockWise());
            if (CompanionUtils.hasSolidFloor(companion.getLevel(), alt)
                    && CompanionUtils.hasSolidFloor(companion.getLevel(), altSide)) {
                mineDirection = alternate;
                direction = alternate;
                width = direction.getClockWise();
                nextFloor = companion.blockPosition().relative(direction).below();
                nextFloorSide = nextFloor.relative(width);
                nextHead = nextFloor.above();
                nextHeadSide = nextFloorSide.above();
            } else {
                companion.getNavigation().stop();
                return;
            }
        }

        // If any of the four 2x2 corridor blocks are solid, remove one at a time.
        BlockPos[] blocks = {nextFloor, nextFloorSide, nextHead, nextHeadSide};
        for (BlockPos pos : blocks) {
            BlockState state = companion.getLevel().getBlockState(pos);
            if (state.isAir()) continue;
            if (!CompanionUtils.isMineableBlock(state)) {
                companion.getNavigation().stop();
                return;
            }
            if (workCooldown > 0) return;
            if (mineBlockWithTool(pos, pickaxeSlot, false)) return;
        }

        // The 2x2 step is open. Walk down into it.
        companion.getNavigation().moveTo(
                nextFloor.getX() + 0.5D,
                nextFloor.getY(),
                nextFloor.getZ() + 0.5D,
                1.0D
        );
    }

    private void handleWoodcutting() {
        collectNearbyDrops();

        int axeSlot = findAxeSlot();
        if (axeSlot < 0) {
            stopForMissingTool("Для рубки леса нужен топор в инвентаре.");
            return;
        }

        if (companion.getInventory().isFull()) {
            companion.returnToOwner();
            return;
        }

        // Keep working on the same tree instead of selecting an unrelated log every tick.
        BlockPos log = findNextTreeLog();
        if (log == null) {
            companion.getNavigation().stop();
            lastTreeLog = null;
            return;
        }

        if (mineBlockWithTool(log, axeSlot, true)) {
            lastTreeLog = log;
        }
    }

    private BlockPos findNextTreeLog() {
        if (lastTreeLog != null && CompanionUtils.isWoodBlock(
                companion.getLevel().getBlockState(lastTreeLog))) {
            return lastTreeLog;
        }

        // Search for the nearest log. After breaking one, nearby connected logs are picked first.
        if (lastTreeLog != null) {
            BlockPos connected = findConnectedLog(lastTreeLog);
            if (connected != null) return connected;
        }

        return CompanionUtils.findNearestBlock(
                companion.getLevel(),
                companion.blockPosition(),
                16,
                CompanionUtils::isWoodBlock);
    }

    private BlockPos findConnectedLog(BlockPos origin) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < 256) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) continue;

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (visited.contains(next)) continue;

                BlockState state = companion.getLevel().getBlockState(next);
                if (CompanionUtils.isWoodBlock(state)) {
                    return next;
                }

                // Leaves are allowed as a bridge, but we limit the search to a small tree area.
                if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
                        || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.JUNGLE_LEAVES)
                        || state.is(Blocks.ACACIA_LEAVES) || state.is(Blocks.DARK_OAK_LEAVES)) {
                    queue.addLast(next);
                }
            }
        }

        return null;
    }

    private boolean mineBlockWithTool(BlockPos pos, int toolSlot, boolean woodOnly) {
        BlockState state = companion.getLevel().getBlockState(pos);

        if (state.isAir()) {
            if (pos.equals(targetBlock)) targetBlock = null;
            return false;
        }

        if (woodOnly) {
            if (!CompanionUtils.isWoodBlock(state)) {
                lastTreeLog = null;
                return false;
            }
        } else if (!CompanionUtils.isMineableBlock(state)) {
            if (pos.equals(targetBlock)) targetBlock = null;
            return false;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(pos));

        // A worker can reach above itself without jumping into the block.
        if (distance > 16.0D) {
            companion.getNavigation().moveTo(
                    pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 1.0D);
            return true;
        }

        ItemStack tool = companion.getInventory().getItem(toolSlot);
        if (tool.isEmpty()) return false;

        if (woodOnly && !(tool.getItem() instanceof AxeItem)) return false;
        if (!woodOnly && !(tool.getItem() instanceof PickaxeItem)) return false;

        if (workCooldown > 0) return true;

        if (!(companion.getLevel() instanceof ServerLevel serverLevel)) return false;

        float hardness = state.getDestroySpeed(serverLevel, pos);
        if (hardness < 0.0F) return false;

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);

        // Drop the real block loot into the companion inventory.
        for (ItemStack drop : Block.getDrops(
                state, serverLevel, pos, blockEntity, companion, tool.copy())) {
            addDropToInventory(drop);
        }

        serverLevel.levelEvent(2001, pos, Block.getId(state));
        serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        // Real tool durability + break, instead of creative-style infinite tools.
        tool.hurtAndBreak(1, companion, entity -> { });
        companion.getInventory().setItem(toolSlot, tool);

        // Harder blocks take longer. This makes the worker feel like a real miner/woodcutter.
        workCooldown = Math.max(4, Math.min(18, (int) (hardness * 4.0F)));

        return true;
    }

    private void addDropToInventory(ItemStack drop) {
        if (drop.isEmpty()) return;

        ItemStack remaining = drop.copy();
        companion.getInventory().addItem(remaining);

        if (!remaining.isEmpty()) {
            companion.getLevel().addFreshEntity(new ItemEntity(
                    companion.getLevel(),
                    companion.getX(),
                    companion.getY() + 0.4D,
                    companion.getZ(),
                    remaining));
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
            if (stack.getItem() instanceof PickaxeItem
                    && stack.getMaxDamage() > 0
                    && stack.getDamageValue() < stack.getMaxDamage() - 1) {
                return i;
            }
        }
        return -1;
    }

    private int findAxeSlot() {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.getItem() instanceof AxeItem
                    && stack.getMaxDamage() > 0
                    && stack.getDamageValue() < stack.getMaxDamage() - 1) {
                return i;
            }
        }
        return -1;
    }

    private void stopForMissingTool(String message) {
        companion.stopAll();
        Player owner = companion.getOwner();

        if (owner != null) {
            owner.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(message), true);
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
                    1.0D);
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
