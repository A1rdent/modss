package com.example.companionmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import com.example.companionmod.fabric.FabricCompanionMod;
import com.example.companionmod.common.entity.CompanionEntity;

public class CompanionEntityRenderer<T extends CompanionEntity> extends LivingEntityRenderer<T, CompanionModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FabricCompanionMod.MOD_ID, "textures/entity/companion/companion.png");

    public CompanionEntityRenderer(EntityRendererProvider.Context context) {
        super(context, createModel(context.getModelSet()), 0.5f);
    }

    private static CompanionModel<?> createModel(EntityModelSet modelSet) {
        return new CompanionModel<>(modelSet.bakeLayer(CompanionModel.createBodyLayer()));
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity pEntity) {
        return TEXTURE;
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
}
