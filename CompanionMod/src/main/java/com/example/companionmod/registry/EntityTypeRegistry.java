package com.example.companionmod.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.fabric.FabricCompanionMod;

public final class EntityTypeRegistry {
    public static final EntityType<CompanionEntity> COMPANION = Registry.register(
            Registry.ENTITY_TYPE,
            new ResourceLocation(FabricCompanionMod.MOD_ID, "companion"),
            EntityType.Builder.of(CompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build("companion")
    );

    private EntityTypeRegistry() {}

    public static void init() {
        // Forces class initialization and registration.
    }
}
