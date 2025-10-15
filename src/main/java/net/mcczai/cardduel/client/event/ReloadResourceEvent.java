package net.mcczai.cardduel.client.event;


import net.mcczai.cardduel.client.resource.ClientReloadManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReloadResourceEvent {
    public static final ResourceLocation BLOCK_ATLAS_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");;

    @SubscribeEvent
    public static void onTextureStitchEventPost(@NotNull TextureStitchEvent.Post event) {
        if (BLOCK_ATLAS_TEXTURE != null && BLOCK_ATLAS_TEXTURE.equals(event.getAtlas().location())) {
            ClientReloadManager.reloadAllPack();
        }
    }

}
