package com.dxvkstatecachebank.dxvkstatecachebank.component;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@DependsOn("sampleDataCreator")
public class DxvkCacheMergerComponent {
    private final Logger logger = LoggerFactory.getLogger(DxvkCacheMergerComponent.class);

    // Regex for finding the number of new entries a cache file introduced: "(?<=\(2\/2\)\.\.\. )\d+(?= new entries)"
    private final Pattern sumOfMergedEntriesPattern = Pattern.compile("(?<=\\(2/2\\)\\.\\.\\. )\\d+(?= new entries)");
    // Example program output:
    /*
        Merging files r5apex.dxvk-cache r5apex2.dxvk-cache
        Detected state cache version v10
        Merging r5apex.dxvk-cache (1/2)... 636 new entries
        Merging r5apex2.dxvk-cache (2/2)... 324 new entries  <-- This number is what we're trying to acquire with our regex
        Writing 636 entries to file out.dxvk-cache
        Finished
    * */
    private final Path tempDirectoryPath;

    private final GameService gameService;

    public DxvkCacheMergerComponent(GameService gameService) throws InterruptedException, IOException, SQLException {
        this.gameService = gameService;

        if(!isExecutableExistsOnPath()) {
            throw new RuntimeException("Cannot find 'dxvk-cache-tool' on PATH or it is not executable!");
        }

        tempDirectoryPath = createTempDirectory();
        logger.info("Temporary dir path: {}", tempDirectoryPath);
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

    private void deleteTemporaryFile(Path temporaryFilePath) {
        if(!temporaryFilePath.toFile().delete()) {
            logger.error("Temporary file {} couldn't be deleted!", temporaryFilePath);
        }
    }

    @Async
    public void mergeCacheFileToIncrementalCache(Game game, CacheFile cacheFileToMerge) throws IOException, SQLException {
        Path incrementalCacheFilePath = Files.createTempFile(tempDirectoryPath, "IncrementalCache", ".dxvk-cache");
        writeBlobToFile(game.getIncrementalCacheFile(), incrementalCacheFilePath);

        Path mergeableCacheFilePath = Files.createTempFile(tempDirectoryPath, "MergeableCache", ".dxvk-cache");
        writeBlobToFile(cacheFileToMerge.getData(), mergeableCacheFilePath);

        Path outputCacheFilePath = Files.createTempFile(tempDirectoryPath, "MergedCacheOutput", ".dxvk-cache");

        try {
            Process testProcess = new ProcessBuilder(
                    "dxvk-cache-tool",
                    "-o", outputCacheFilePath.toRealPath().toString(),
                    incrementalCacheFilePath.toRealPath().toString(),
                    mergeableCacheFilePath.toRealPath().toString()
            ).start();

            logger.info("DXVK Cache Tool Started with the following PID: {}", testProcess.pid());
            logger.info("Waiting for it to finish...");
            Integer processExitCode = null;
            try {
                processExitCode = testProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("Waiting for the DXVK Cache Tool(PID: {}) process to end has been interrupted!", testProcess.pid());
            }

            if (processExitCode == null || processExitCode != 0) {
                logger.error("DXVK Cache Tool(PID: {}) returned with a non-zero exit code: {}!", testProcess.pid(), processExitCode);
            } else {
                Integer sumOfEntriesMerged = null;
                // Read stdout of process and extract the sum of cache entries that has been merged
                try (BufferedReader inputFromProcess = testProcess.inputReader()) {
                    while (inputFromProcess.ready()) {
                        String line = inputFromProcess.readLine();
                        Matcher matcher = sumOfMergedEntriesPattern.matcher(line);
                        if (matcher.find()) {
                            String match = matcher.group();
                            sumOfEntriesMerged = Integer.valueOf(match);
                        }
                    }
                }

                if (sumOfEntriesMerged == null) {
                    logger.error("Couldn't match the count of entries merged from DXVK Cache Tool's output. Check the regex used for that!");
                    throw new RuntimeException("Couldn't match count of entries merged from DXVK Cache Tool!");
                }
                logger.info("DXVK Cache Tool successfully merged {} entries!", sumOfEntriesMerged);

                // Read back the merged file to Game::incrementalCacheFile
                File outputCacheFile = outputCacheFilePath.toFile();
                var reader = new BufferedInputStream(Files.newInputStream(outputCacheFilePath));
                game.setIncrementalCacheFile(BlobProxy.generateProxy(reader, outputCacheFile.length()));
                // Save it
                gameService.save(game);
            }
        } finally {
            // Cleanup
            deleteTemporaryFile(incrementalCacheFilePath);
            deleteTemporaryFile(mergeableCacheFilePath);
            deleteTemporaryFile(outputCacheFilePath);
        }
    }
}
