package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.service.dto.MergeResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DxvkCacheToolRunner {
    private final Logger logger = LoggerFactory.getLogger(DxvkCacheToolRunner.class);

    // Regex for finding the number of new entries a cache file introduced: "(?<=\(2\/2\)\.\.\. )\d+(?= new entries)"
    private final Pattern sumOfMergedEntriesPattern = Pattern.compile("(?<=\\(2/2\\)\\.\\.\\. )\\d+(?= new entries)");
    private final String DXVK_CACHE_TOOL_NAME = "dxvk-cache-tool";
    // Example program output:
    /*
        Merging files r5apex.dxvk-cache r5apex2.dxvk-cache
        Detected state cache version v10
        Merging r5apex.dxvk-cache (1/2)... 636 new entries
        Merging r5apex2.dxvk-cache (2/2)... 324 new entries  <-- This number is what we're trying to acquire with our regex
        Writing 636 entries to file out.dxvk-cache
        Finished
    * */

    public DxvkCacheToolRunner() throws IOException, InterruptedException {
        if(!isDxvkCacheToolsExecutableExistsOnPath()) {
            throw new RuntimeException("Cannot find '%s' on PATH or it is not executable!".formatted(DXVK_CACHE_TOOL_NAME));
        }
    }

    private boolean isDxvkCacheToolsExecutableExistsOnPath() throws InterruptedException, IOException {
        Process testProcess = new ProcessBuilder("sh", "-c", "%s -h".formatted(DXVK_CACHE_TOOL_NAME)).start();
        return testProcess.waitFor() == 0;
    }

    public MergeResultDto run(Path incrementalCacheFilePath, Path mergeableCacheFilePath, Path outputCacheFilePath) throws IOException {
        Process testProcess = new ProcessBuilder(
                DXVK_CACHE_TOOL_NAME,
                "-o", outputCacheFilePath.toRealPath().toString(),
                incrementalCacheFilePath.toRealPath().toString(),
                mergeableCacheFilePath.toRealPath().toString()
        ).start();

        logger.info("DXVK Cache Tool Started with the following PID: {}", testProcess.pid());
        logger.info("Waiting for it to finish...");
        int processExitCode;
        try {
            processExitCode = testProcess.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("Waiting for the DXVK Cache Tool(PID: {}) process to end has been interrupted!", testProcess.pid());
            return MergeResultDto.builder()
                    .success(false)
                    .build();
        }

        if (processExitCode != 0) {
            logger.error("DXVK Cache Tool(PID: {}) returned with a non-zero exit code: {}!", testProcess.pid(), processExitCode);
            return MergeResultDto.builder()
                    .success(false)
                    .build();
        }

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
        return MergeResultDto.builder()
                        .success(true)
                        .sumOfEntriesMerged(sumOfEntriesMerged)
                        .build();
    }
}
