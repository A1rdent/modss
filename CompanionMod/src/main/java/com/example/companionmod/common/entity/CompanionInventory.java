package com.example.companionmod.common.entity;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class CompanionInventory implements Container {
    private final ItemStack[] items;
    private final int size;

    public CompanionInventory(int size) {
        this.items = new ItemStack[size];
        this.size = size;
        for (int i = 0; i < size; i++) {
            this.items[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY;
        }
        return this.items[slot];
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = this.items[slot];
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (itemStack.getCount() <= amount) {
            this.items[slot] = ItemStack.EMPTY;
            return itemStack;
        }

        ItemStack result = itemStack.split(amount);
        this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = this.items[slot];
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.items[slot] = ItemStack.EMPTY;
        return itemStack;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (slot < 0 || slot >= this.size) {
            return;
        }
        this.items[slot] = itemStack;
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void setChanged() {
        // Notify changes
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.size; i++) {
            this.items[i] = ItemStack.EMPTY;
        }
    }

    public boolean canAddItem(ItemStack itemStack) {
        for (int i = 0; i < this.size; i++) {
            if (this.items[i].isEmpty() || 
                (ItemStack.isSameItemSameTags(this.items[i], itemStack) && 
                 this.items[i].getCount() < this.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    public void addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;

        for (int i = 0; i < this.size; i++) {
            if (this.items[i].isEmpty()) {
                this.items[i] = itemStack.copy();
                this.setChanged();
                return;
            }
        }

        for (int i = 0; i < this.size; i++) {
            if (ItemStack.isSameItemSameTags(this.items[i], itemStack)) {
                int space = this.getMaxStackSize() - this.items[i].getCount();
                if (space > 0) {
                    int transfer = Math.min(space, itemStack.getCount());
                    this.items[i].grow(transfer);
                    itemStack.shrink(transfer);
                    this.setChanged();
                    if (itemStack.isEmpty()) return;
                }
            }
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();

        for (int i = 0; i < this.size; i++) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) i);
            this.items[i].save(itemTag);
            listTag.add(itemTag);
        }

        tag.put("Items", listTag);
        tag.putInt("Size", this.size);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < this.size) {
                this.items[slot] = ItemStack.of(itemTag);
            }
        }
    }
}
