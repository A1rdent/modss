package com.example.companionmod.common.entity;

import com.example.companionmod.client.gui.CompanionScreenHandler;
import com.example.companionmod.registry.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CompanionEntity extends PathfinderMob {
    private UUID ownerUuid;
    private Player owner;
    private final CompanionInventory inventory;
    private final CompanionAI companionAI;
    private boolean isMining, isGathering, isDepositing;
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
        if (!this.getLevel().isClientSide && this.owner == null && this.ownerUuid != null
                && this.getLevel() instanceof ServerLevel serverLevel) {
            this.owner = serverLevel.getServer().getPlayerList().getPlayer(this.ownerUuid);
        }
        if (!this.getLevel().isClientSide && this.owner != null) this.companionAI.tick();
    }

    @Override protected void registerGoals() {}

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.isOwnedBy(player)) {
            if (!this.getLevel().isClientSide) player.displayClientMessage(
                    Component.literal("This companion belongs to another player."), true);
            return InteractionResult.sidedSuccess(this.getLevel().isClientSide);
        }
        if (!this.getLevel().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new CompanionMenuProvider(this));
        }
        return InteractionResult.sidedSuccess(this.getLevel().isClientSide);
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
        if (this.ownerUuid != null) tag.putUUID("Owner", this.ownerUuid);
        else if (this.owner != null) tag.putUUID("Owner", this.owner.getUUID());
        tag.put("Inventory", this.inventory.serializeNBT());
        tag.putBoolean("IsMining", this.isMining);
        tag.putBoolean("IsGathering", this.isGathering);
        tag.putBoolean("IsDepositing", this.isDepositing);
        tag.putBoolean("IsFollowing", this.isFollowing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.ownerUuid = tag.getUUID("Owner");
        if (tag.contains("Inventory")) this.inventory.deserializeNBT(tag.getCompound("Inventory"));
        this.isMining = tag.getBoolean("IsMining");
        this.isGathering = tag.getBoolean("IsGathering");
        this.isDepositing = tag.getBoolean("IsDepositing");
        this.isFollowing = tag.getBoolean("IsFollowing");
    }

    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }

    public Player getOwner() { return this.owner; }
    public UUID getOwnerUuid() { return this.ownerUuid; }
    public void setOwner(Player owner) { this.owner = owner; this.ownerUuid = owner == null ? null : owner.getUUID(); }
    public boolean isOwnedBy(Player player) { return player != null && this.ownerUuid != null && this.ownerUuid.equals(player.getUUID()); }
    public CompanionInventory getInventory() { return this.inventory; }

    public boolean isMining() { return isMining; }
    public void setMining(boolean value) { isMining = value; }
    public boolean isGathering() { return isGathering; }
    public void setGathering(boolean value) { isGathering = value; }
    public boolean isDepositing() { return isDepositing; }
    public void setDepositing(boolean value) { isDepositing = value; }
    public boolean isFollowing() { return isFollowing; }
    public void setFollowing(boolean value) { isFollowing = value; }

    public String getModeName() {
        if (isMining) return "mining";
        if (isGathering) return "gathering";
        if (isDepositing) return "depositing";
        if (isFollowing) return "following";
        return "idle";
    }

    public void stopAll() {
        isMining = false; isGathering = false; isDepositing = false; isFollowing = false;
        getNavigation().stop();
    }

    private static final class CompanionMenuProvider implements ExtendedScreenHandlerFactory {
        private final CompanionEntity companion;
        private CompanionMenuProvider(CompanionEntity companion) { this.companion = companion; }
        @Override public Component getDisplayName() { return Component.literal("Companion"); }
        @Override public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new CompanionScreenHandler(containerId, playerInventory, companion);
        }
        @Override public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
            buf.writeVarInt(companion.getId());
        }
    }
}
