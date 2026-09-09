package com.example.companionmod.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class CompanionModel<T extends LivingEntity> extends HumanoidModel<T> {
    public CompanionModel(ModelPart root) {
        super(root);
    }
}
