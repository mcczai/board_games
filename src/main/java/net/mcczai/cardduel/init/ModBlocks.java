package net.mcczai.cardduel.init;

import net.mcczai.cardduel.block.DuelTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.mcczai.cardduel.CardduelMod.MODID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);


    public static RegistryObject<Block> DUELTABLE_BLOCK = BLOCKS.register("dueltable_block", () -> new DuelTableBlock(BlockBehaviour.Properties.of()));

}
