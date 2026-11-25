package com.gunmetalblack.guardianproject.common;

import com.gunmetalblack.guardianproject.GuardianProjectMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityClassification;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, GuardianProjectMod.MOD_ID);
    public static final RegistryObject<EntityType<MeteorEntity>> METEOR = ENTITIES.register("meteor", () ->
            EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, EntityClassification.MISC)
                    .sized(4F, 4F).clientTrackingRange(128).updateInterval(1).build("guardian_project:meteor"));
}
