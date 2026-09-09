package com.example.companionmod.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionScreenHandler> {
    private static final int GUI_WIDTH = 360;
    private static final int GUI_HEIGHT = 206;

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

        int x = this.leftPos + 198;
        int y = this.topPos + 52;
        int w = 150;
        int h = 20;
        int gap = 23;

        this.addRenderableWidget(new Button(x, y, w, h, Component.translatable("screen.companionmod.button.mine"), b -> press(0)));
        this.addRenderableWidget(new Button(x, y + gap, w, h, Component.translatable("screen.companionmod.button.follow"), b -> press(1)));
        this.addRenderableWidget(new Button(x, y + gap * 2, w, h, Component.translatable("screen.companionmod.button.gather"), b -> press(2)));
        this.addRenderableWidget(new Button(x, y + gap * 3, w, h, Component.translatable("screen.companionmod.button.deposit"), b -> press(3)));
        this.addRenderableWidget(new Button(x, y + gap * 4, w, h, Component.translatable("screen.companionmod.button.stop"), b -> press(4)));
        this.addRenderableWidget(new Button(x, y + gap * 5, w, h, Component.translatable("screen.companionmod.button.return"), b -> press(5)));
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

        // Main panels.
        fill(poseStack, x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF171310);
        fill(poseStack, x + 4, y + 4, x + 192, y + 202, 0xFF6E4C2F);
        fill(poseStack, x + 198, y + 4, x + 356, y + 202, 0xFF39271D);
        fill(poseStack, x + 8, y + 8, x + 188, y + 198, 0xFF3A2A21);
        fill(poseStack, x + 202, y + 8, x + 352, y + 198, 0xFF241914);

        // Companion inventory: 4 rows.
        drawSlots(poseStack, x + 13, y + 26, 9, 4);
        // Player inventory: 3 rows + hotbar.
        drawSlots(poseStack, x + 13, y + 102, 9, 3);
        drawSlots(poseStack, x + 13, y + 162, 9, 1);

        // Status separator and accents.
        fill(poseStack, x + 202, y + 42, x + 352, y + 43, 0xFF8B6A44);
    }

    private void drawSlots(PoseStack poseStack, int startX, int startY, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = startX + col * 18;
                int sy = startY + row * 18;
                fill(poseStack, sx, sy, sx + 18, sy + 18, 0xFF211915);
                fill(poseStack, sx + 1, sy + 1, sx + 17, sy + 17, 0xFF514035);
                fill(poseStack, sx + 2, sy + 2, sx + 16, sy + 16, 0xFF2E241F);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, Component.translatable("screen.companionmod.title"), 12, 10, 0xFFFFFF);
        this.font.draw(poseStack, Component.translatable("screen.companionmod.inventory"), 12, 16, 0xE8D7C4);
        this.font.draw(poseStack, Component.translatable("screen.companionmod.player_inventory"), 12, 94, 0xE8D7C4);

        if (this.menu.getCompanion() != null) {
            var companion = this.menu.getCompanion();
            this.font.draw(poseStack,
                    Component.translatable("screen.companionmod.health", (int) companion.getHealth(), (int) companion.getMaxHealth()),
                    204, 12, 0xFFFFFF);
            this.font.draw(poseStack,
                    Component.translatable("screen.companionmod.mode", companion.getModeText()),
                    204, 27, 0xE8D7C4);
            this.font.draw(poseStack,
                    Component.translatable("screen.companionmod.hint"),
                    204, 40, 0xBFAE9B);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
