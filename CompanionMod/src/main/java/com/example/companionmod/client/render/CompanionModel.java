package com.example.companionmod.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class CompanionModel<T extends LivingEntity> extends HumanoidModel<T> {
    public CompanionModel(ModelPart root) {
        super(root);
    }

    public static net.minecraft.client.model.geom.ModelLayerLocation createBodyLayer() {
        return new net.minecraft.client.model.geom.ModelLayerLocation(
                new net.minecraft.resources.ResourceLocation("companionmod", "companion"),
                "main"
        );
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
    }
}
