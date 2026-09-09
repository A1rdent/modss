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
        int y = this.topPos + 50;
        int w = 150;
        int h = 20;
        int gap = 24;

        addRenderableWidget(new Button(x, y, w, h,
                Component.translatable("screen.companionmod.button.mine"), b -> press(0)));
        addRenderableWidget(new Button(x, y + gap, w, h,
                Component.translatable("screen.companionmod.button.follow"), b -> press(1)));
        addRenderableWidget(new Button(x, y + gap * 2, w, h,
                Component.translatable("screen.companionmod.button.gather"), b -> press(2)));
        addRenderableWidget(new Button(x, y + gap * 3, w, h,
                Component.translatable("screen.companionmod.button.deposit"), b -> press(3)));
        addRenderableWidget(new Button(x, y + gap * 4, w, h,
                Component.translatable("screen.companionmod.button.stop"), b -> press(4)));
        addRenderableWidget(new Button(x, y + gap * 5, w, h,
                Component.translatable("screen.companionmod.button.return"), b -> press(5)));
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
        fill(poseStack, x + 198, y + 3, x + 357, y + 203, 0xFF251B16);

        // Inventory headers.
        fill(poseStack, x + 8, y + 22, x + 188, y + 23, 0xFF6C4C32);
        fill(poseStack, x + 8, y + 99, x + 188, y + 100, 0xFF6C4C32);

        // Status area separator.
        fill(poseStack, x + 204, y + 42, x + 351, y + 43, 0xFF6C4C32);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, Component.translatable("screen.companionmod.title"), 12, 7, 0xFFFFFF);
        font.draw(poseStack, Component.translatable("screen.companionmod.inventory"), 12, 9, 0xE8D7C4);
        font.draw(poseStack, Component.translatable("screen.companionmod.player_inventory"), 12, 91, 0xE8D7C4);

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
                    204, 40, 0xBFAE9B);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
