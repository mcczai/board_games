package net.mcczai.cardduel.client.resource;

import net.mcczai.cardduel.resources.CommonCardPackLoader;
import net.mcczai.cardduel.resources.VersionChecker;

public class ClientReloadManager {


    public static void reloadAllPack(){

        VersionChecker.clearCache();
        ClientCardPackLoader.init();

        CommonCardPackLoader.reloadAsset();
        ClientCardPackLoader.reloadAsset();

        CommonCardPackLoader.reloadIndex();
        ClientCardPackLoader.reloadIndex();
    }
}
