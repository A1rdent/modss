package com.example.companionmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompanionInventory implements Container {
    private final ItemStack[] items;
    private final int size;

    public CompanionInventory(int size) { this.items = new ItemStack[size]; this.size = size; clearContent(); }
    @Override public int getContainerSize() { return size; }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < size ? items[slot] : ItemStack.EMPTY; }
    @Override public boolean isEmpty() {
        for (ItemStack stack : items) if (!stack.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= size || amount <= 0) return ItemStack.EMPTY;
        ItemStack stack = items[slot];
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = stack.split(amount);
        if (stack.isEmpty()) items[slot] = ItemStack.EMPTY;
        setChanged(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= size) return ItemStack.EMPTY;
        ItemStack result = items[slot]; items[slot] = ItemStack.EMPTY; return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= size) return;
        items[slot] = stack == null ? ItemStack.EMPTY : stack;
        if (!items[slot].isEmpty() && items[slot].getCount() > items[slot].getMaxStackSize())
            items[slot].setCount(items[slot].getMaxStackSize());
        setChanged();
    }
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { for (int i=0;i<size;i++) items[i]=ItemStack.EMPTY; }

    public boolean canAddItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        int remaining = stack.getCount();
        for (ItemStack existing : items) {
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameTags(existing, stack)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
                if (remaining <= 0) return true;
            }
        }
        return false;
    }

    public void addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        for (int i=0;i<size && !stack.isEmpty();i++) {
            ItemStack existing=items[i];
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing,stack)) {
                int space=existing.getMaxStackSize()-existing.getCount();
                int transfer=Math.min(space,stack.getCount());
                if (transfer>0) { existing.grow(transfer); stack.shrink(transfer); setChanged(); }
            }
        }
        for (int i=0;i<size && !stack.isEmpty();i++) if(items[i].isEmpty()) {
            int transfer=Math.min(stack.getCount(),stack.getMaxStackSize());
            ItemStack copy=stack.copy(); copy.setCount(transfer); items[i]=copy; stack.shrink(transfer); setChanged();
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag=new CompoundTag(); ListTag list=new ListTag();
        for(int i=0;i<size;i++) if(!items[i].isEmpty()) {
            CompoundTag itemTag=new CompoundTag(); itemTag.putByte("Slot",(byte)i); items[i].save(itemTag); list.add(itemTag);
        }
        tag.put("Items",list); return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        clearContent(); ListTag list=tag.getList("Items",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){ CompoundTag itemTag=list.getCompound(i); int slot=itemTag.getByte("Slot")&255;
            if(slot<size) items[slot]=ItemStack.of(itemTag); }
    }
}
