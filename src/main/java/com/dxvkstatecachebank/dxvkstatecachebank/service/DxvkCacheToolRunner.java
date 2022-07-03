package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.DxvkCacheToolIsNotOnPathException;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.RegexDidNotMatchException;
import com.dxvkstatecachebank.dxvkstatecachebank.service.dto.MergeResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DxvkCacheToolRunner {
    // Regex for finding the number of new entries a cache file introduced (for dxvk-cache-tool's stdout)
    private static final Pattern sumOfMergedEntriesPattern = Pattern.compile("((?<=\\(2/2\\)\\.\\.\\. )|(?<=\\(1/1\\)\\.\\.\\. ))\\d+(?= new entries)");
    private static final String DXVK_CACHE_TOOL_NAME = "dxvk-cache-tool";

    public DxvkCacheToolRunner() throws IOException, InterruptedException {
        if (!isDxvkCacheToolsExecutableExistsOnPath()) {
            throw new DxvkCacheToolIsNotOnPathException("Cannot find '%s' on PATH or it is not executable!".formatted(DXVK_CACHE_TOOL_NAME));
        }
    }

    private boolean isDxvkCacheToolsExecutableExistsOnPath() throws InterruptedException, IOException {
        Process testProcess = new ProcessBuilder(DXVK_CACHE_TOOL_NAME, "-h").start();
        return testProcess.waitFor() == 0;
    }

    // Only does validation
    public MergeResultDto run(Path mergeableCacheFilePath, Path outputCacheFilePath) throws IOException {
        return runTool(new ProcessBuilder(
                DXVK_CACHE_TOOL_NAME,
                "-o", outputCacheFilePath.toRealPath().toString(),
                mergeableCacheFilePath.toRealPath().toString()
        ));
    }

    public MergeResultDto run(Path incrementalCacheFilePath, Path mergeableCacheFilePath, Path outputCacheFilePath) throws IOException {
        return runTool(new ProcessBuilder(
                DXVK_CACHE_TOOL_NAME,
                "-o", outputCacheFilePath.toRealPath().toString(),
                incrementalCacheFilePath.toRealPath().toString(),
                mergeableCacheFilePath.toRealPath().toString()
        ));
    }

    private MergeResultDto runTool(ProcessBuilder processBuilder) throws IOException {
        Process toolProcess = processBuilder.start();
        log.info("DXVK Cache Tool Started with the following PID: {}", toolProcess.pid());
        log.info("Waiting for it to finish...");
        int processExitCode;
        try {
            processExitCode = toolProcess.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Waiting for the DXVK Cache Tool(PID: {}) process to end has been interrupted!", toolProcess.pid());
            return MergeResultDto.builder()
                    .success(false)
                    .build();
        }

        if (processExitCode != 0) {
            log.error("DXVK Cache Tool(PID: {}) returned with a non-zero exit code: {}!", toolProcess.pid(), processExitCode);
            return MergeResultDto.builder()
                    .success(false)
                    .build();
        }

        Integer sumOfEntriesMerged = null;
        // Read stdout of process and extract the sum of cache entries that has been merged
        try (BufferedReader inputFromProcess = toolProcess.inputReader()) {
            String line;
            while ((line = inputFromProcess.readLine()) != null) {
                log.info(line);
                Matcher matcher = sumOfMergedEntriesPattern.matcher(line);
                if (matcher.find()) {
                    String match = matcher.group();
                    sumOfEntriesMerged = Integer.valueOf(match);
                }
            }
        }

        if (sumOfEntriesMerged == null) {
            log.error("Couldn't match the count of entries merged from DXVK Cache Tool's output. Check the regex used for that!");
            throw new RegexDidNotMatchException("Couldn't match count of entries merged from DXVK Cache Tool!");
        }
        log.info("DXVK Cache Tool successfully merged {} entries!", sumOfEntriesMerged);
        return MergeResultDto.builder()
                .success(true)
                .sumOfEntriesMerged(sumOfEntriesMerged)
                .build();
    }
}
