package com.example.companionmod.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionScreenHandler> {
    private static final int GUI_WIDTH = 364;
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

        int x = this.leftPos + 202;
        int y = this.topPos + 56;
        int w = 73;
        int h = 20;
        int gap = 24;

        addRenderableWidget(new Button(x, y, w, h, Component.translatable("screen.companionmod.button.mine"), b -> press(0)));
        addRenderableWidget(new Button(x + 77, y, w, h, Component.translatable("screen.companionmod.button.follow"), b -> press(1)));
        addRenderableWidget(new Button(x, y + gap, w, h, Component.translatable("screen.companionmod.button.gather"), b -> press(2)));
        addRenderableWidget(new Button(x + 77, y + gap, w, h, Component.translatable("screen.companionmod.button.woodcut"), b -> press(6)));
        addRenderableWidget(new Button(x, y + gap * 2, w, h, Component.translatable("screen.companionmod.button.deposit"), b -> press(3)));
        addRenderableWidget(new Button(x + 77, y + gap * 2, w, h, Component.translatable("screen.companionmod.button.stop"), b -> press(4)));
        addRenderableWidget(new Button(x, y + gap * 3, 150, h, Component.translatable("screen.companionmod.button.return"), b -> press(5)));
    }

    private void press(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Clean Minecraft-style panels. Item slots themselves are rendered by AbstractContainerScreen,
        // so we deliberately do not draw a custom texture over them.
        fill(poseStack, x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF101010);
        fill(poseStack, x + 3, y + 3, x + 193, y + 203, 0xFF3A2A1F);
        fill(poseStack, x + 196, y + 3, x + 361, y + 219, 0xFF251B16);

        // Inventory headers.
        fill(poseStack, x + 8, y + 23, x + 188, y + 24, 0xFF6C4C32);
        fill(poseStack, x + 8, y + 100, x + 188, y + 101, 0xFF6C4C32);
        drawSlotGrid(poseStack, x + 13, y + 28, 9, 4);
        drawSlotGrid(poseStack, x + 13, y + 104, 9, 3);
        drawSlotGrid(poseStack, x + 13, y + 164, 9, 1);

        // Status area separator.
        fill(poseStack, x + 202, y + 48, x + 354, y + 49, 0xFF6C4C32);
    }

    private void drawSlotGrid(PoseStack poseStack, int startX, int startY, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = startX + col * 18;
                int sy = startY + row * 18;
                // Opaque vanilla-like slot: dark outside, light border, dark center.
                fill(poseStack, sx - 1, sy - 1, sx + 18, sy + 18, 0xFF080808);
                fill(poseStack, sx, sy, sx + 17, sy + 17, 0xFF8B8B8B);
                fill(poseStack, sx + 1, sy + 1, sx + 16, sy + 16, 0xFF373737);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, Component.translatable("screen.companionmod.title"), 12, 8, 0xFFFFFF);
        font.draw(poseStack, Component.translatable("screen.companionmod.inventory"), 12, 17, 0xE8D7C4);
        font.draw(poseStack, Component.translatable("screen.companionmod.player_inventory"), 12, 92, 0xE8D7C4);

        if (menu.getCompanion() != null) {
            var companion = menu.getCompanion();

            font.draw(poseStack,
                    Component.translatable("screen.companionmod.health",
                            (int) companion.getHealth(), (int) companion.getMaxHealth()),
                    204, 10, 0xFFFFFF);

            font.draw(poseStack,
                    Component.translatable("screen.companionmod.mode", companion.getModeText()),
                    204, 26, 0xE8D7C4);

            font.draw(poseStack,
                    Component.translatable("screen.companionmod.hint"),
                    204, 41, 0xBFAE9B);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
