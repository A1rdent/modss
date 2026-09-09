package com.example.companionmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class CompanionInventory implements Container {
    private final ItemStack[] items;
    private final int size;

    public CompanionInventory(int size) {
        this.items = new ItemStack[size];
        this.size = size;
        clearContent();
    }

    @Override
    public int getContainerSize() { return this.size; }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.size ? this.items[slot] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= this.size || amount <= 0) return ItemStack.EMPTY;
        ItemStack stack = this.items[slot];
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = stack.split(amount);
        if (stack.isEmpty()) this.items[slot] = ItemStack.EMPTY;
        this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.size) return ItemStack.EMPTY;
        ItemStack result = this.items[slot];
        this.items[slot] = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.size) return;
        this.items[slot] = stack == null ? ItemStack.EMPTY : stack;
        if (!this.items[slot].isEmpty() && this.items[slot].getCount() > this.items[slot].getMaxStackSize()) {
            this.items[slot].setCount(this.items[slot].getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void setChanged() {
        // Entity persistence is handled by CompanionEntity NBT serialization.
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.size; i++) this.items[i] = ItemStack.EMPTY;
    }

    public boolean canAddItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        int remaining = stack.getCount();

        for (ItemStack existing : this.items) {
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameTags(existing, stack)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
                if (remaining <= 0) return true;
            }
        }
        return false;
    }

    /** Adds as much as possible and mutates the supplied stack by the transferred amount. */
    public void addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        for (int i = 0; i < this.size && !stack.isEmpty(); i++) {
            ItemStack existing = this.items[i];
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                int transfer = Math.min(space, stack.getCount());
                if (transfer > 0) {
                    existing.grow(transfer);
                    stack.shrink(transfer);
                    this.setChanged();
                }
            }
        }

        for (int i = 0; i < this.size && !stack.isEmpty(); i++) {
            if (this.items[i].isEmpty()) {
                int transfer = Math.min(stack.getCount(), stack.getMaxStackSize());
                this.items[i] = stack.copyWithCount(transfer);
                stack.shrink(transfer);
                this.setChanged();
            }
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < this.size; i++) {
            if (this.items[i].isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) i);
            this.items[i].save(itemTag);
            list.add(itemTag);
        }
        tag.put("Items", list);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        clearContent();
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < this.size) this.items[slot] = ItemStack.of(itemTag);
        }
    }
}
