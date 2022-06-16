package com.dxvkstatecachebank.dxvkstatecachebank.component;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;

/*
*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
*
*     W O R K   I N    P R O G R E S S !
*
*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
*/

//@Component // Exclude from component scan. This component is not yet ready for use.
public class DxvkCacheMergerComponent {
    private final Path tempDirectoryPath;

    public DxvkCacheMergerComponent() throws InterruptedException, IOException {
        if(!isExecutableExistsOnPath()) {
            throw new RuntimeException("Cannot find 'dxvk-cache-tool' on PATH or it is not executable!");
        }

        tempDirectoryPath = createTempDirectory();
    }

    private boolean isExecutableExistsOnPath() throws InterruptedException, IOException {
        Process testProcess = new ProcessBuilder("dxvk-cache-tool", "-h").start();
        return testProcess.waitFor() == 0;
    }

    private Path createTempDirectory() throws IOException {
        Path tempDirectoryPath = Files.createTempDirectory("dxvk-cache-tmp");

        // Delete temp directory on VM exit
        File tempDirectoryFile = new File(tempDirectoryPath.toUri());
        tempDirectoryFile.deleteOnExit();

        return tempDirectoryPath;
    }

    private void writeBlobToFile(Blob blob, Path filePath) throws IOException, SQLException {
        try (
                var fileOutputStream = new BufferedOutputStream(Files.newOutputStream(filePath));
                var blobInputStream = new BufferedInputStream(blob.getBinaryStream())
        ) {
            blobInputStream.transferTo(fileOutputStream);
        }
    }

    // TODO: Write me!
    public Game mergeCacheFileToIncrementalCache(Game game, CacheFile cacheFileToMerge) throws IOException, SQLException {
        Path incrementalCacheFilePath = Files.createTempFile(tempDirectoryPath, "IncrementalCache", ".dxvk-cache");
        writeBlobToFile(game.getIncrementalCacheFile(), incrementalCacheFilePath);

        Path mergeableCacheFilePath = Files.createTempFile(tempDirectoryPath, "MergeableCache", ".dxvk-cache");
        writeBlobToFile(cacheFileToMerge.getData(), mergeableCacheFilePath);

        /*Process testProcess = new ProcessBuilder("dxvk-cache-tool", "-h").start();
        return testProcess.waitFor() == 0;*/
        return null;
    }
}
