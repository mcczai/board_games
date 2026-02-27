package net.mcczai.cardduel.client.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mcczai.cardduel.client.resource.pojo.PackInfoPOJO;
import net.mcczai.cardduel.client.resource.serialize.ItemStackSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public enum ClientAssetManager {
    INSTANCE;

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
            .create();
    //TODO:这里的GSON没写完
    /**
     * 存储卡包信息
     */
    private final Map<String, PackInfoPOJO> customInfos = Maps.newHashMap();

    /**
     * 存储语言信息
     */
    private final Map<String,Map<String,String>> languages = Maps.newHashMap();

    public void putPackInfo(String namespace, PackInfoPOJO info) {
        customInfos.put(namespace, info);
    }

    public void putLanguage(String region, Map<String, String> lang) {
        Map<String, String> languageMaps = languages.getOrDefault(region, Maps.newHashMap());
        languageMaps.putAll(lang);
        languages.put(region, languageMaps);
    }

    public Map<String, String> getLanguages(String region) {
        return languages.get(region);
    }

    public PackInfoPOJO getPackInfo(ResourceLocation id){
        return customInfos.get(id.getNamespace());
    }

    public void clearAll(){
        this.customInfos.clear();
        this.languages.clear();
    }
}
