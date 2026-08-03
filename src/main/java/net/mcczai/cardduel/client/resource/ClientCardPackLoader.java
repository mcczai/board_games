package net.mcczai.cardduel.client.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mcczai.cardduel.API.resource.ResourceManager;
import net.mcczai.cardduel.client.resource.loader.ClientCardIndexLoader;
import net.mcczai.cardduel.client.resource.loader.asset.LanguageLoader;
import net.mcczai.cardduel.client.resource.loader.asset.PackInfoLoader;
import net.mcczai.cardduel.client.resource.loader.asset.TextureLoader;
import net.mcczai.cardduel.client.resource.serialize.ItemStackSerializer;
import net.mcczai.cardduel.config.common.OtherConfig;
import net.mcczai.cardduel.resources.VersionChecker;
import net.mcczai.cardduel.util.CardPackTraverser;
import net.mcczai.cardduel.util.GetJarResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.mcczai.cardduel.resources.CommonCardPackLoader.FOLDER;

@OnlyIn(Dist.CLIENT)
public class ClientCardPackLoader {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
            .create();

    public static final Map<ResourceLocation, ClientCardIndex> CARD_INDEX = Maps.newHashMap();

    public static void init() {
        createFolder();
        checkDefaultPack();
    }

    public static void reloadAsset() {
        ClientAssetManager.INSTANCE.clearAll();

        CardPackTraverser.traverse(FOLDER.toFile(),
                ClientCardPackLoader::readDirAsset,
                ClientCardPackLoader::readZipAsset
        );
    }

    public static void reloadIndex() {
        CARD_INDEX.clear();
        ClientCardIndexLoader.loadCardIndex();
    }

    private static void readZipAsset(@NotNull File file) {
        try {
            if (VersionChecker.noneMatch(new java.util.zip.ZipFile(file), file.toPath())) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        CardPackTraverser.traverseZip(file, ctx -> {
            if (LanguageLoader.load(ctx.zipFile(), ctx.path())) {
                return;
            }
            if (PackInfoLoader.load(ctx.zipFile(), ctx.path())) {
                return;
            }
            TextureLoader.load(ctx.zipFile(), ctx.path());
        });
    }

    private static void readDirAsset(@NotNull File root) {
        if (!VersionChecker.match(root)) {
            return;
        }
        CardPackTraverser.traverseDir(root, dir -> {
            LanguageLoader.load(dir);
            PackInfoLoader.load(dir);
            TextureLoader.load(dir);
        });
    }

    private static void createFolder() {
        File folder = FOLDER.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkDefaultPack() {
        if (!OtherConfig.DEFAULT_PACK_DEBUG.get()) {
            for (ResourceManager.ExtraEntry entry : ResourceManager.EXTRA_ENTRIES) {
                GetJarResources.copyModDirectory(entry.modMainClass(), entry.srcPath(), FOLDER, entry.extraDirName());
            }
        }
    }

    @Contract(pure = true)
    public static @NotNull Set<Map.Entry<ResourceLocation, ClientCardIndex>> getAllCard() {
        return CARD_INDEX.entrySet();
    }

    public static Optional<ClientCardIndex> getCardIndex(ResourceLocation registryName) {
        return Optional.ofNullable(CARD_INDEX.get(registryName));
    }
}
