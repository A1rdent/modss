package com.example.companionmod.client.gui;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.EntityTypeRegistry;
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

    // Server-side constructor.
    public CompanionScreenHandler(int containerId, Inventory playerInventory, CompanionEntity companion) {
        super(ScreenHandlerRegistry.COMPANION, containerId);
        this.companion = companion;
        this.companionInventory = companion.getInventory();
        setupSlots(playerInventory);
    }

    // Client-side constructor. The server sends the companion entity id when opening the GUI.
    public CompanionScreenHandler(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, findCompanion(playerInventory, buf.readVarInt()));
    }

    private static CompanionEntity findCompanion(Inventory playerInventory, int entityId) {
        if (playerInventory.player.level().getEntity(entityId) instanceof CompanionEntity companion) {
            return companion;
        }
        return null;
    }

    private CompanionScreenHandler(int containerId, Inventory playerInventory, CompanionEntity companion, boolean clientFallback) {
        super(ScreenHandlerRegistry.COMPANION, containerId);
        this.companion = companion;
        this.companionInventory = companion != null ? companion.getInventory() : new SimpleContainer(COMPANION_SLOTS);
        setupSlots(playerInventory);
    }

    private void setupSlots(Inventory playerInventory) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.companionInventory, row * 9 + col, 8 + col * 18, 20 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, 108 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 166));
        }
    }

    public CompanionEntity getCompanion() {
        return this.companion;
    }

    public String getModeName() {
        return this.companion == null ? "unknown" : this.companion.getModeName();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.companion == null || !this.companion.isOwnedBy(player)) {
            return false;
        }

        switch (buttonId) {
            case 0 -> setMode(Mode.MINE);
            case 1 -> setMode(Mode.FOLLOW);
            case 2 -> setMode(Mode.GATHER);
            case 3 -> setMode(Mode.DEPOSIT);
            case 4 -> this.companion.stopAll();
            default -> { return false; }
        }
        return true;
    }

    private void setMode(Mode mode) {
        this.companion.stopAll();
        switch (mode) {
            case MINE -> this.companion.setMining(true);
            case FOLLOW -> this.companion.setFollowing(true);
            case GATHER -> this.companion.setGathering(true);
            case DEPOSIT -> this.companion.setDepositing(true);
        }
    }

    private enum Mode { MINE, FOLLOW, GATHER, DEPOSIT }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < COMPANION_SLOTS) {
            if (!this.moveItemStackTo(stack, COMPANION_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, COMPANION_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.companion == null) return true;
        return this.companion.isAlive()
                && this.companion.isOwnedBy(player)
                && player.distanceToSqr(this.companion) < 64.0D * 64.0D;
    }
}
