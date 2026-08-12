package net.mcczai.cardduel.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import net.mcczai.cardduel.API.resource.ResourceManager;
import net.mcczai.cardduel.CardduelMod;
import net.mcczai.cardduel.config.common.OtherConfig;
import net.mcczai.cardduel.resources.loader.CommonCardDataLoader;
import net.mcczai.cardduel.resources.loader.CommonCardIndexLoader;
import net.mcczai.cardduel.util.CardPackTraverser;
import net.mcczai.cardduel.util.GetJarResources;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CommonCardPackLoader {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, (JsonDeserializer<ResourceLocation>) (json, typeOfT, context) -> {
                try {
                    return ResourceLocation.parse(json.getAsString());
                } catch (RuntimeException e) {
                    throw new JsonParseException("Invalid ResourceLocation: " + json, e);
                }
            })
            .create();

    public static final Path FOLDER = Paths.get("config", CardduelMod.MODID, "custom");

    public static final Map<ResourceLocation, CommonCardIndex> CARD_INDEX = Maps.newHashMap();

    public static void init() {
        createFolder();
        checkDefaultPack();
    }

    public static void createFolder() {
        File folder = FOLDER.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void reloadAsset() {
        CardAssetManager.INSTANCE.clearAll();
        CardPackTraverser.traverse(FOLDER.toFile(),
                dir -> CardPackTraverser.traverseDir(dir, CommonCardDataLoader::load),
                zip -> CardPackTraverser.traverseZip(zip,
                        ctx -> CommonCardDataLoader.load(ctx.zipFile(), ctx.path()))
        );
    }

    public static void reloadIndex() {
        CARD_INDEX.clear();
        CardPackTraverser.traverse(FOLDER.toFile(),
                dir -> CardPackTraverser.traverseDir(dir, root -> {
                    try {
                        CommonCardIndexLoader.loadCardIndex(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }),
                zip -> CardPackTraverser.traverseZip(zip,
                        ctx -> {
                            try {
                                CommonCardIndexLoader.loadCardIndex(ctx.path(), ctx.zipFile());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    private static void checkDefaultPack() {
        if (!OtherConfig.DEFAULT_PACK_DEBUG.get()) {
            for (ResourceManager.ExtraEntry entry : ResourceManager.EXTRA_ENTRIES) {
                GetJarResources.copyModDirectory(entry.modMainClass(), entry.srcPath(), FOLDER, entry.extraDirName());
            }
        }
    }

    public static Optional<CommonCardIndex> getCardIndex(ResourceLocation registryName) {
        return Optional.ofNullable(CARD_INDEX.get(registryName));
    }

    public static Set<Map.Entry<ResourceLocation, CommonCardIndex>> getAllCards() {
        return CARD_INDEX.entrySet();
    }
}
