package com.example.companionmod.common.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public class CompanionEntity extends LivingEntity {
    private Player owner;
    private CompanionInventory inventory;
    private CompanionAI companionAI;

    // Command flags
    private boolean isMining = false;
    private boolean isGathering = false;
    private boolean isDepositing = false;
    private boolean isFollowing = true;

    public CompanionEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.inventory = new CompanionInventory(36);
        this.companionAI = new CompanionAI(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.owner != null) {
            this.companionAI.tick();
        }
    }

    @Override
    protected void registerGoals() {
        // Goals will be managed by CompanionAI
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) {
            tag.putUUID("Owner", this.owner.getUUID());
        }
        tag.put("Inventory", this.inventory.serializeNBT());
        tag.putBoolean("IsMining", this.isMining);
        tag.putBoolean("IsGathering", this.isGathering);
        tag.putBoolean("IsDepositing", this.isDepositing);
        tag.putBoolean("IsFollowing", this.isFollowing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            // Owner UUID stored for later recovery if needed
            tag.getUUID("Owner");
        }
        if (tag.contains("Inventory")) {
            this.inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        this.isMining = tag.getBoolean("IsMining");
        this.isGathering = tag.getBoolean("IsGathering");
        this.isDepositing = tag.getBoolean("IsDepositing");
        this.isFollowing = tag.getBoolean("IsFollowing");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // Getters and setters
    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public CompanionInventory getInventory() {
        return this.inventory;
    }

    public boolean isMining() {
        return this.isMining;
    }

    public void setMining(boolean mining) {
        this.isMining = mining;
    }

    public boolean isGathering() {
        return this.isGathering;
    }

    public void setGathering(boolean gathering) {
        this.isGathering = gathering;
    }

    public boolean isDepositing() {
        return this.isDepositing;
    }

    public void setDepositing(boolean depositing) {
        this.isDepositing = depositing;
    }

    public boolean isFollowing() {
        return this.isFollowing;
    }

    public void setFollowing(boolean following) {
        this.isFollowing = following;
    }

    public void stopAll() {
        this.isMining = false;
        this.isGathering = false;
        this.isDepositing = false;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide) {
            // Inventory will be handled by drops
        }
    }
}
