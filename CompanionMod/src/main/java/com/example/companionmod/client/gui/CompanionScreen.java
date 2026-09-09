package com.example.companionmod.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionScreenHandler> {
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 222;

    public CompanionScreen(CompanionScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int x = this.leftPos + 186;
        int y = this.topPos + 48;
        int w = 122;
        int h = 20;
        int gap = 25;

        this.addRenderableWidget(new Button(x, y, w, h, Component.literal("Mine"), b -> press(0)));
        this.addRenderableWidget(new Button(x, y + gap, w, h, Component.literal("Follow"), b -> press(1)));
        this.addRenderableWidget(new Button(x, y + gap * 2, w, h, Component.literal("Gather"), b -> press(2)));
        this.addRenderableWidget(new Button(x, y + gap * 3, w, h, Component.literal("Deposit"), b -> press(3)));
        this.addRenderableWidget(new Button(x, y + gap * 4, w, h, Component.literal("Stop"), b -> press(4)));
    }

    private void press(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        fill(poseStack, x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF202020);
        fill(poseStack, x + 4, y + 4, x + 176, y + 100, 0xFF303030);
        fill(poseStack, x + 4, y + 104, x + 176, y + 218, 0xFF303030);
        fill(poseStack, x + 180, y + 4, x + 316, y + 218, 0xFF303030);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, Component.literal("Companion"), 188, 12, 0xFFFFFF);
        this.font.draw(poseStack, Component.literal("Inventory"), 8, 7, 0xFFFFFF);
        this.font.draw(poseStack, Component.literal("Your inventory"), 8, 95, 0xFFFFFF);

        if (this.menu.getCompanion() != null) {
            var companion = this.menu.getCompanion();
            this.font.draw(poseStack,
                    Component.literal("Health: " + (int) companion.getHealth() + "/" + (int) companion.getMaxHealth()),
                    188, 28, 0xD0D0D0);
            this.font.draw(poseStack,
                    Component.literal("Mode: " + companion.getModeName()),
                    188, 38, 0xD0D0D0);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
