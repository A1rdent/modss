package com.example.companionmod.common.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CompanionEntity extends PathfinderMob {
    private UUID ownerUuid;
    private Player owner;
    private final CompanionInventory inventory;
    private final CompanionAI companionAI;

    private boolean isMining;
    private boolean isGathering;
    private boolean isDepositing;
    private boolean isFollowing = true;

    public CompanionEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.inventory = new CompanionInventory(36);
        this.companionAI = new CompanionAI(this);
        this.setPersistenceRequired();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.owner == null && this.ownerUuid != null
                && this.level() instanceof ServerLevel serverLevel) {
            this.owner = serverLevel.getServer().getPlayerList().getPlayer(this.ownerUuid);
        }

        if (!this.level().isClientSide && this.owner != null) {
            this.companionAI.tick();
        }
    }

    @Override
    protected void registerGoals() {
        // CompanionAI manages behavior explicitly so commands have deterministic priority.
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
        } else if (this.owner != null) {
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
            this.ownerUuid = tag.getUUID("Owner");
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
        return new ClientboundAddEntityPacket(this);
    }

    public Player getOwner() {
        return this.owner;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.ownerUuid = owner == null ? null : owner.getUUID();
    }

    public boolean isOwnedBy(Player player) {
        return player != null && this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    public CompanionInventory getInventory() {
        return this.inventory;
    }

    public boolean isMining() { return this.isMining; }
    public void setMining(boolean mining) { this.isMining = mining; }
    public boolean isGathering() { return this.isGathering; }
    public void setGathering(boolean gathering) { this.isGathering = gathering; }
    public boolean isDepositing() { return this.isDepositing; }
    public void setDepositing(boolean depositing) { this.isDepositing = depositing; }
    public boolean isFollowing() { return this.isFollowing; }
    public void setFollowing(boolean following) { this.isFollowing = following; }

    public String getModeName() {
        if (this.isMining) return "mining";
        if (this.isGathering) return "gathering";
        if (this.isDepositing) return "depositing";
        if (this.isFollowing) return "following";
        return "idle";
    }

    public void stopAll() {
        this.isMining = false;
        this.isGathering = false;
        this.isDepositing = false;
        this.isFollowing = false;
        this.getNavigation().stop();
    }
}
