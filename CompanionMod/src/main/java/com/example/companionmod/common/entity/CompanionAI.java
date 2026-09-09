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
    private BlockPos stairTarget;
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


    public void reset() {
        workCooldown = 0;
        targetBlock = null;
        stairTarget = null;
        lastTreeLog = null;
        mineDirection = null;
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

        // The mining system is a deterministic state machine. It does not depend on
        // vanilla pathfinding to enter a one-block-lower tunnel step.
        if (stairTarget != null) {
            if (moveToMinePoint(stairTarget)) {
                stairTarget = null;
            } else {
                return;
            }
        }

        // Only chase ore that is exposed to the current tunnel.
        BlockPos ore = findExposedOreNearTunnel();
        if (ore != null && mineBlockWithTool(ore, pickaxeSlot, false)) {
            return;
        }

        // No exposed ore: immediately construct the next 2x2 stair step.
        buildNextStairStep(pickaxeSlot);
    }

    private BlockPos findExposedOreNearTunnel() {
        BlockPos base = companion.blockPosition();
        Direction forward = mineDirection;
        Direction side = forward.getClockWise();

        for (int fd = 0; fd <= 5; fd++) {
            for (int so = -2; so <= 2; so++) {
                for (int yo = -1; yo <= 2; yo++) {
                    BlockPos pos = base.relative(forward, fd).relative(side, so).above(yo);
                    if (!CompanionUtils.isOre(companion.getLevel().getBlockState(pos))) continue;

                    for (Direction d : new Direction[] {
                            forward, side, side.getOpposite(), Direction.UP, Direction.DOWN}) {
                        if (companion.getLevel().getBlockState(pos.relative(d)).isAir()) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void buildNextStairStep(int pickaxeSlot) {
        Direction forward = mineDirection;
        Direction side = forward.getClockWise();
        BlockPos current = companion.blockPosition();

        // Feet position of the next lower step.
        BlockPos nextA = current.relative(forward).below();
        BlockPos nextB = nextA.relative(side);

        // A 2x2 x 2-high chamber. Only this chamber is touched on this cycle.
        BlockPos nextHeadA = nextA.above();
        BlockPos nextHeadB = nextB.above();

        BlockPos[] chamber = {nextA, nextB, nextHeadA, nextHeadB};

        // If the floor below the future step is missing, choose a different forward
        // direction. We never deliberately dig into a ravine or make a free fall.
        if (!isSafeStepFloor(nextA) || !isSafeStepFloor(nextB)) {
            Direction turned = findSafeMiningDirection(current);
            if (turned == null) {
                companion.getNavigation().stop();
                return;
            }
            mineDirection = turned;
            return;
        }

        // Break exactly one block per action. This guarantees visible progress.
        for (BlockPos pos : chamber) {
            BlockState state = companion.getLevel().getBlockState(pos);
            if (state.isAir()) continue;

            if (!CompanionUtils.isMineableBlock(state)) {
                companion.getNavigation().stop();
                return;
            }

            if (workCooldown > 0) return;

            if (mineBlockWithTool(pos, pickaxeSlot, false)) {
                return;
            }
        }

        // Chamber is open. Move the entity one full block forward and one block down
        // using our own waypoint controller. No NavMesh/pathfinding is required.
        stairTarget = nextA.immutable();
        moveToMinePoint(stairTarget);
    }

    private boolean isSafeStepFloor(BlockPos feet) {
        BlockPos floor = feet.below();
        BlockState state = companion.getLevel().getBlockState(floor);
        return !state.isAir() && state.getFluidState().isEmpty()
                && state.isSolidRender(companion.getLevel(), floor);
    }

    private Direction findSafeMiningDirection(BlockPos current) {
        Direction original = mineDirection;
        Direction[] candidates = {
                original.getClockWise(),
                original.getCounterClockWise(),
                original.getOpposite()
        };

        for (Direction d : candidates) {
            BlockPos a = current.relative(d).below();
            BlockPos b = a.relative(d.getClockWise());
            if (isSafeStepFloor(a) && isSafeStepFloor(b)) {
                return d;
            }
        }
        return null;
    }

    private boolean moveToMinePoint(BlockPos target) {
        Vec3 destination = new Vec3(
                target.getX() + 0.5D,
                target.getY() + 0.02D,
                target.getZ() + 0.5D);

        Vec3 current = companion.position();
        Vec3 delta = destination.subtract(current);
        double distance = delta.length();

        if (distance <= 0.12D) {
            companion.setPos(destination.x, destination.y, destination.z);
            companion.setDeltaMovement(Vec3.ZERO);
            return true;
        }

        double speed = 0.12D;
        Vec3 step = delta.scale(Math.min(speed / distance, 1.0D));
        companion.setPos(current.x + step.x, current.y + step.y, current.z + step.z);
        companion.setDeltaMovement(Vec3.ZERO);
        companion.hasImpulse = true;
        companion.getLookControl().setLookAt(destination.x, destination.y + 0.8D, destination.z);
        return false;
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
