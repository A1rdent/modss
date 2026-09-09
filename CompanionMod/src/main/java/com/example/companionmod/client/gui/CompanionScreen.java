package com.example.companionmod.client.gui;

import com.example.companionmod.fabric.FabricCompanionMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionScreenHandler> {
    private static final int GUI_WIDTH = 360;
    private static final int GUI_HEIGHT = 206;

    private static final ResourceLocation GUI_TEXTURE =
            new ResourceLocation(FabricCompanionMod.MOD_ID, "textures/gui/companion_gui.png");

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
        if (minecraft == null) return;

        minecraft.getTextureManager().bindForSetup(GUI_TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, Component.translatable("screen.companionmod.title"), 12, 8, 0xFFFFFF);
        font.draw(poseStack, Component.translatable("screen.companionmod.inventory"), 12, 16, 0xE8D7C4);
        font.draw(poseStack, Component.translatable("screen.companionmod.player_inventory"), 12, 94, 0xE8D7C4);

        if (menu.getCompanion() != null) {
            var companion = menu.getCompanion();

            font.draw(poseStack,
                    Component.translatable("screen.companionmod.health",
                            (int) companion.getHealth(), (int) companion.getMaxHealth()),
                    204, 12, 0xFFFFFF);

            font.draw(poseStack,
                    Component.translatable("screen.companionmod.mode", companion.getModeText()),
                    204, 27, 0xE8D7C4);

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
