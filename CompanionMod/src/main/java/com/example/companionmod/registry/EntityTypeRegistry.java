package com.example.companionmod.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import com.example.companionmod.common.entity.CompanionEntity;
import com.example.companionmod.fabric.FabricCompanionMod;

public class EntityTypeRegistry {
    public static final EntityType<CompanionEntity> COMPANION = register("companion",
            EntityType.Builder.of(CompanionEntity::new, MobCategory.CREATURE).sized(0.6f, 1.95f).build(new ResourceLocation(FabricCompanionMod.MOD_ID, "companion").toString()));
n    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String id, EntityType<T> type) {
        return Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(FabricCompanionMod.MOD_ID, id), type);
    }
n    public static void init() {
        // called from FabricCompanionMod onInitialize to ensure class loads and entity types register
    }
}
