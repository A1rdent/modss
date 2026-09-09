package com.example.companionmod.client.render;

import com.example.companionmod.common.entity.CompanionEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.example.companionmod.fabric.FabricCompanionMod;

public class CompanionEntityRenderer extends LivingEntityRenderer<CompanionEntity, CompanionModel<CompanionEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FabricCompanionMod.MOD_ID, "textures/entity/companion/companion.png");

    public CompanionEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CompanionModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(CompanionEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
