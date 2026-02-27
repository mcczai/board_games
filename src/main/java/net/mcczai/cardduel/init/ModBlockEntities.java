package net.mcczai.cardduel.init;

import net.mcczai.cardduel.block.entity.DuelTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.mcczai.cardduel.CardduelMod.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static RegistryObject<BlockEntityType<DuelTableBlockEntity>> DUELTABLE_ENTITY =
            BLOCK_ENTITIES.register("dueltable_block_entity",
            ()-> DuelTableBlockEntity.TYPE);
}
