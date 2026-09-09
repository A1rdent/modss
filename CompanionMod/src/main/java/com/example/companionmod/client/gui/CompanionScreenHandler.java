package com.example.companionmod.client.gui;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.ScreenHandlerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CompanionScreenHandler extends AbstractContainerMenu {
    public static final int COMPANION_SLOTS = 36;

    private final CompanionEntity companion;
    private final Container companionInventory;

    public CompanionScreenHandler(int containerId, Inventory playerInventory, CompanionEntity companion) {
        super(ScreenHandlerRegistry.COMPANION, containerId);
        this.companion = companion;
        this.companionInventory = companion.getInventory();
        setupSlots(playerInventory);
    }

    public CompanionScreenHandler(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ScreenHandlerRegistry.COMPANION, containerId);
        this.companion = findCompanion(playerInventory, buf.readVarInt());
        this.companionInventory = this.companion != null
                ? this.companion.getInventory()
                : new SimpleContainer(COMPANION_SLOTS);
        setupSlots(playerInventory);
    }

    private static CompanionEntity findCompanion(Inventory playerInventory, int entityId) {
        if (playerInventory.player.getLevel().getEntity(entityId) instanceof CompanionEntity companion) {
            return companion;
        }
        return null;
    }

    private void setupSlots(Inventory playerInventory) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(companionInventory, row * 9 + col,
                        13 + col * 18, 28 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + col,
                        13 + col * 18, 104 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 13 + col * 18, 164));
        }
    }

    public CompanionEntity getCompanion() {
        return companion;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (companion == null || !companion.isOwnedBy(player)) return false;

        switch (buttonId) {
            case 0 -> setMode(Mode.MINE);
            case 1 -> setMode(Mode.FOLLOW);
            case 2 -> setMode(Mode.GATHER);
            case 3 -> setMode(Mode.DEPOSIT);
            case 4 -> companion.stopAll();
            case 5 -> companion.returnToOwner();
            case 6 -> setMode(Mode.WOODCUTTING);
            default -> { return false; }
        }
        return true;
    }

    private void setMode(Mode mode) {
        companion.stopAll();

        switch (mode) {
            case MINE -> companion.setMining(true);
            case FOLLOW -> companion.setFollowing(true);
            case GATHER -> companion.setGathering(true);
            case DEPOSIT -> companion.setDepositing(true);
            case WOODCUTTING -> companion.setWoodcutting(true);
        }
    }

    private enum Mode { MINE, FOLLOW, GATHER, DEPOSIT, WOODCUTTING }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;

        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();

        if (index < COMPANION_SLOTS) {
            if (!moveItemStackTo(stack, COMPANION_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, COMPANION_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return companion != null
                && companion.isAlive()
                && companion.isOwnedBy(player)
                && player.distanceToSqr(companion) < 64.0D * 64.0D;
    }
}
