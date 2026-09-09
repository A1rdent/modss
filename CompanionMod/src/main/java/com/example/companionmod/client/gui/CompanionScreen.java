package com.example.companionmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Minecraft;

import com.example.companionmod.fabric.FabricCompanionMod;
import com.example.companionmod.common.entity.CompanionEntity;

public class CompanionScreen extends AbstractContainerScreen<CompanionScreenHandler> {
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 222;
    private static final ResourceLocation TEXTURE = new ResourceLocation(FabricCompanionMod.MOD_ID, "textures/gui/companion_gui.png");

    private final CompanionEntity companion;
    private Button mineButton;
    private Button followButton;
    private Button stopButton;
    private Button gatherButton;

    public CompanionScreen(CompanionScreenHandler handler, Inventory playerInventory, Component component) {
        super(handler, playerInventory, component);
        this.companion = handler != null ? handler.getCompanion() : null;
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Mine button
        this.mineButton = this.addRenderableWidget(new Button(
                this.leftPos + 10, this.topPos + 20, 70, 20,
                Component.literal("Mine"), (button) -> this.sendCommand("mine")
        ));

        // Follow button
        this.followButton = this.addRenderableWidget(new Button(
                this.leftPos + 96, this.topPos + 20, 70, 20,
                Component.literal("Follow"), (button) -> this.sendCommand("follow")
        ));

        // Gather button
        this.gatherButton = this.addRenderableWidget(new Button(
                this.leftPos + 10, this.topPos + 50, 70, 20,
                Component.literal("Gather"), (button) -> this.sendCommand("gather")
        ));

        // Stop button
        this.stopButton = this.addRenderableWidget(new Button(
                this.leftPos + 96, this.topPos + 50, 70, 20,
                Component.literal("Stop"), (button) -> this.sendCommand("stop")
        ));
    }

    private void sendCommand(String command) {
        if (this.companion != null) {
            switch (command) {
                case "mine":
                    this.companion.setMining(!this.companion.isMining());
                    break;
                case "follow":
                    this.companion.setFollowing(!this.companion.isFollowing());
                    break;
                case "gather":
                    this.companion.setGathering(!this.companion.isGathering());
                    break;
                case "stop":
                    this.companion.stopAll();
                    break;
            }
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBg(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    protected void renderBg(PoseStack pPoseStack) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        pPoseStack.pushPose();
        pPoseStack.translate(this.leftPos, this.topPos, 0);

        // Render companion info
        if (this.companion != null) {
            this.font.draw(pPoseStack, Component.literal("Companion"), 8, 6, 4210752);
            this.font.draw(pPoseStack, Component.literal("Health: " + (int)this.companion.getHealth() + "/" + (int)this.companion.getMaxHealth()), 8, 90, 4210752);
            this.font.draw(pPoseStack, Component.literal("Inventory: " + this.companion.getInventory().getContainerSize()), 8, 100, 4210752);
            
            // Status display
            String status = "Status: ";
            if (this.companion.isMining()) status += "Mining ";
            if (this.companion.isGathering()) status += "Gathering ";
            if (this.companion.isFollowing()) status += "Following ";
            if (status.equals("Status: ")) status += "Idle";
            
            this.font.draw(pPoseStack, Component.literal(status), 8, 110, 4210752);
        }

        pPoseStack.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
