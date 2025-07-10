package net.mcczai.cardduel.resources;

public class DedicatedServerReloadManager {

    public static void loadCardPack(){

        VersionChecker.clearCache();
        CommonCardPackLoader.init();
        CommonCardPackLoader.reloadAsset();
        CommonCardPackLoader.reloadIndex();
    }

}
