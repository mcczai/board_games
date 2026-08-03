package net.mcczai.cardduel.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class CardPackTraverser {

    private CardPackTraverser() {
    }

    public static void traverseDir(File root, Consumer<File> dirHandler) {
        File[] subFiles = root.listFiles((dir, name) -> true);
        if (subFiles == null) {
            return;
        }
        for (File subFile : subFiles) {
            if (subFile.isDirectory()) {
                dirHandler.accept(subFile);
            }
        }
    }

    public static void traverseZip(File file, Consumer<ZipEntryContext> entryHandler) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                String path = entries.nextElement().getName();
                entryHandler.accept(new ZipEntryContext(zipFile, path, file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void traverse(File folder, Consumer<File> dirHandler, Consumer<File> zipHandler) {
        File[] files = folder.listFiles((dir, name) -> true);
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                zipHandler.accept(file);
            }
            if (file.isDirectory()) {
                dirHandler.accept(file);
            }
        }
    }

    public record ZipEntryContext(ZipFile zipFile, String path, File zipFileRef) {
    }
}
