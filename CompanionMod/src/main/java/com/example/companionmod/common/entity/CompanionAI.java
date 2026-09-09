package com.example.companionmod.common.entity;

import com.example.companionmod.common.util.CompanionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
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
    private int workCooldown;
    private BlockPos targetBlock;
    private BlockPos lastTreeLog;
    private Direction mineDirection;

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
            stopForMissingTool("Для шахты положи кирку в инвентарь компаньона.");
            return;
        }

        if (companion.getInventory().isFull()) {
            companion.returnToOwner();
            return;
        }

        if (mineDirection == null || !mineDirection.getAxis().isHorizontal()) {
            mineDirection = companion.getDirection();
        }

        // First check the tunnel walls, floor and ceiling for nearby ore.
        // We intentionally do NOT chase a buried ore vein across the map, because that
        // can leave navigation stuck in solid rock.
        BlockPos ore = findExposedOreNearTunnel();
        if (ore != null) {
            if (mineBlockWithTool(ore, pickaxeSlot, false)) return;
        }

        // Always make measurable progress: build a continuous 2x2 descending tunnel.
        mineStaircaseStep(pickaxeSlot);
    }

    private BlockPos findExposedOreNearTunnel() {
        BlockPos base = companion.blockPosition();
        Direction forward = mineDirection;
        Direction side = forward.getClockWise();

        // Scan the current 2x2 tunnel area plus the four surrounding wall faces.
        // Ore is only selected when it is close enough to be reached from the tunnel.
        for (int forwardDistance = 0; forwardDistance <= 3; forwardDistance++) {
            for (int sideOffset = -2; sideOffset <= 2; sideOffset++) {
                for (int yOffset = -1; yOffset <= 2; yOffset++) {
                    BlockPos pos = base.relative(forward, forwardDistance)
                            .relative(side, sideOffset)
                            .above(yOffset);
                    BlockState state = companion.getLevel().getBlockState(pos);
                    if (!CompanionUtils.isOre(state)) continue;

                    // Only use ore at/near the tunnel surface. If another solid block
                    // is directly between the companion and the ore, keep tunnelling.
                    if (isExposedToTunnel(pos, base, forward, side)) return pos;
                }
            }
        }
        return null;
    }

    private boolean isExposedToTunnel(BlockPos ore, BlockPos base, Direction forward, Direction side) {
        for (Direction d : new Direction[] {forward, side, side.getOpposite(), Direction.UP, Direction.DOWN}) {
            BlockPos neighbor = ore.relative(d);
            double dx = neighbor.getX() - base.getX();
            double dy = neighbor.getY() - base.getY();
            double dz = neighbor.getZ() - base.getZ();
            if (dx * dx + dy * dy + dz * dz <= 6.0D
                    && companion.getLevel().getBlockState(neighbor).isAir()) {
                return true;
            }
        }
        return false;
    }

    private void mineStaircaseStep(int pickaxeSlot) {
        Direction forward = mineDirection;
        Direction side = forward.getClockWise();

        BlockPos current = companion.blockPosition();
        BlockPos next = current.relative(forward).below();
        BlockPos nextSide = next.relative(side);
        BlockPos nextHead = next.above();
        BlockPos nextHeadSide = nextSide.above();

        // Never dig below the companion's current feet. We clear ONLY the next step.
        BlockPos[] stepBlocks = {next, nextSide, nextHead, nextHeadSide};

        // If the next step has a solid floor, clear the two-wide step one block at a time.
        if (!CompanionUtils.hasSolidFloor(companion.getLevel(), next)
                || !CompanionUtils.hasSolidFloor(companion.getLevel(), nextSide)) {
            // Try a simple horizontal turn if terrain opens up there.
            Direction turn = forward.getClockWise();
            if (trySetAlternativeDirection(turn)) {
                mineDirection = turn;
                return;
            }
            companion.getNavigation().stop();
            return;
        }

        for (BlockPos pos : stepBlocks) {
            BlockState state = companion.getLevel().getBlockState(pos);
            if (state.isAir()) continue;

            if (!CompanionUtils.isMineableBlock(state)) {
                companion.getNavigation().stop();
                return;
            }

            if (workCooldown > 0) return;

            if (!mineBlockWithTool(pos, pickaxeSlot, false)) {
                return;
            }

            // Break one block per action; next tick continues the staircase.
            return;
        }

        // The next 2x2 step is completely open. Walk down and forward.
        companion.getNavigation().moveTo(
                next.getX() + 0.5D,
                next.getY(),
                next.getZ() + 0.5D,
                1.0D
        );
    }

    private boolean trySetAlternativeDirection(Direction direction) {
        if (!direction.getAxis().isHorizontal()) return false;
        Direction side = direction.getClockWise();
        BlockPos next = companion.blockPosition().relative(direction).below();
        BlockPos nextSide = next.relative(side);

        return CompanionUtils.hasSolidFloor(companion.getLevel(), next)
                && CompanionUtils.hasSolidFloor(companion.getLevel(), nextSide);
    }

    private void handleWoodcutting() {
        collectNearbyDrops();

        int axeSlot = findAxeSlot();
        if (axeSlot < 0) {
            stopForMissingTool("Для рубки леса положи топор в инвентарь компаньона.");
            return;
        }

        if (companion.getInventory().isFull()) {
            companion.returnToOwner();
            return;
        }

        // Continuously find the nearest log. This means every log in a large tree
        // remains a target after the first two blocks are removed.
        BlockPos log = findNearestLog();
        if (log == null) {
            companion.getNavigation().stop();
            lastTreeLog = null;
            return;
        }

        if (mineBlockWithTool(log, axeSlot, true)) {
            lastTreeLog = log;
        }
    }

    private BlockPos findNearestLog() {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;

        // Prefer a log connected to the previous target.
        if (lastTreeLog != null) {
            BlockPos connected = findConnectedLog(lastTreeLog);
            if (connected != null) return connected;
        }

        int range = 16;
        BlockPos center = companion.blockPosition();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = companion.getLevel().getBlockState(pos);

                    if (!CompanionUtils.isWoodBlock(state)) continue;

                    double d = companion.distanceToSqr(Vec3.atCenterOf(pos));
                    if (d < best) {
                        best = d;
                        nearest = pos;
                    }
                }
            }
        }

        return nearest;
    }

    private BlockPos findConnectedLog(BlockPos origin) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < 512) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) continue;

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (visited.contains(next)) continue;

                BlockState state = companion.getLevel().getBlockState(next);

                if (CompanionUtils.isWoodBlock(state)) {
                    return next;
                }

                if (state.is(BlockTags.LEAVES)) {
                    queue.addLast(next);
                }
            }
        }

        return null;
    }

    private boolean mineBlockWithTool(BlockPos pos, int toolSlot, boolean woodOnly) {
        BlockState state = companion.getLevel().getBlockState(pos);

        if (state.isAir()) return false;

        if (woodOnly) {
            if (!CompanionUtils.isWoodBlock(state)) return false;
        } else if (!CompanionUtils.isMineableBlock(state)) {
            return false;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(pos));
        if (distance > 20.0D) {
            companion.getNavigation().moveTo(
                    pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 1.0D);
            return true;
        }

        if (workCooldown > 0) return true;

        ItemStack tool = companion.getInventory().getItem(toolSlot);
        if (tool.isEmpty()) return false;

        if (woodOnly && !(tool.getItem() instanceof AxeItem)) return false;
        if (!woodOnly && !(tool.getItem() instanceof PickaxeItem)) return false;

        if (!(companion.getLevel() instanceof ServerLevel serverLevel)) return false;

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);

        // Real block drops go to the companion inventory.
        for (ItemStack drop : Block.getDrops(
                state, serverLevel, pos, blockEntity, companion, tool.copy())) {
            addDropToInventory(drop);
        }

        serverLevel.levelEvent(2001, pos, Block.getId(state));
        serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        // Real durability consumption.
        tool.hurtAndBreak(1, companion, ignored -> {});
        companion.getInventory().setItem(toolSlot, tool);

        float hardness = state.getDestroySpeed(serverLevel, pos);
        workCooldown = Math.max(3, Math.min(14, (int) (hardness * 3.0F)));

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
                companion.getLevel(), companion.blockPosition(), CompanionUtils.CHEST_RANGE,
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
