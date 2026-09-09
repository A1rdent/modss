package com.example.companionmod.client.gui;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.registry.ScreenHandlerRegistry;

public class CompanionScreenHandler extends AbstractContainerMenu {
    private final CompanionEntity companion;
    private final Container companionInventory;
    public static final int COMPANION_SLOTS = 36;
    public static final int PLAYER_INVENTORY_ROWS = 3;
    public static final int PLAYER_INVENTORY_COLS = 9;

    // Server-side constructor (passes companion entity)
    public CompanionScreenHandler(int containerId, PlayerInventory playerInventory, CompanionEntity companion) {
        super(ScreenHandlerRegistry.COMPANION, containerId);
        this.companion = companion;
        this.companionInventory = (companion != null) ? companion.getInventory() : new SimpleContainer(COMPANION_SLOTS);
        setupSlots(playerInventory);
    }

    // Client-side constructor (no companion entity available)
    public CompanionScreenHandler(int containerId, PlayerInventory playerInventory) {
        super(ScreenHandlerRegistry.COMPANION, containerId);
        this.companion = null;
        this.companionInventory = new SimpleContainer(COMPANION_SLOTS);
        setupSlots(playerInventory);
    }

    private void setupSlots(PlayerInventory playerInventory) {
        // Add companion inventory slots
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLS; col++) {
                int slotIndex = row * PLAYER_INVENTORY_COLS + col;
                if (slotIndex < COMPANION_SLOTS) {
                    this.addSlot(new Slot(this.companionInventory, slotIndex, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        // Add player inventory
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLS; col++) {
                this.addSlot(new Slot(playerInventory, 9 + row * PLAYER_INVENTORY_COLS + col, 8 + col * 18, 140 + row * 18));
            }
        }

        // Add player hotbar
        for (int col = 0; col < PLAYER_INVENTORY_COLS; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    public CompanionEntity getCompanion() {
        return this.companion;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotItemStack = slot.getItem();
            itemStack = slotItemStack.copy();

            if (pIndex < COMPANION_SLOTS) {
                if (!this.moveItemStackTo(slotItemStack, COMPANION_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItemStack, 0, COMPANION_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItemStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
