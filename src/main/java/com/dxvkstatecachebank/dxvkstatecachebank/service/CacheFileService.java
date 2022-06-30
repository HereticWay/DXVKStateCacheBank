package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.NoNewCacheEntryException;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.UnsuccessfulCacheMergeException;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.CacheFileRepository;
import com.dxvkstatecachebank.dxvkstatecachebank.service.dto.MergeResultDto;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CacheFileService {
    private final CacheFileRepository cacheFileRepository;
    private final Path tempDirectoryPath;
    private final GameService gameService;
    private final CacheFileMapper cacheFileMapper;
    private final DxvkCacheToolRunner dxvkCacheToolRunner;

    @Autowired
    public CacheFileService(CacheFileRepository cacheFileRepository, GameService gameService, CacheFileMapper cacheFileMapper, DxvkCacheToolRunner dxvkCacheToolRunner) throws IOException {
        this.cacheFileRepository = cacheFileRepository;
        this.gameService = gameService;
        this.cacheFileMapper = cacheFileMapper;
        this.dxvkCacheToolRunner = dxvkCacheToolRunner;

        tempDirectoryPath = createTempDirectory();
        log.info("Temporary dir path: {}", tempDirectoryPath);
    }

    public List<CacheFile> findAllByGameId(Long gameId) {
        return cacheFileRepository.findAllByGameId(gameId);
    }

    public List<CacheFile> findAllByUploaderId(Long uploaderId) {
        return cacheFileRepository.findAllByUploaderId(uploaderId);
    }

    public CacheFile save(CacheFile cacheFile) {
        return cacheFileRepository.save(cacheFile);
    }

    public Optional<CacheFile> findById(Long cacheFileId) {
        return cacheFileRepository.findById(cacheFileId);
    }

    public void deleteById(Long cacheFileId) {
        cacheFileRepository.deleteById(cacheFileId);
    }

    private Path createTempDirectory() throws IOException {
        Path tempDirectoryPath = Files.createTempDirectory("dxvk-cache-tmp");

        // Delete temp directory on VM exit
        File tempDirectoryFile = new File(tempDirectoryPath.toUri());
        tempDirectoryFile.deleteOnExit();

        return tempDirectoryPath;
    }

    private void writeStreamToFile(InputStream inputStream, Path filePath) throws IOException {
        try (
                var outputStream = new BufferedOutputStream(Files.newOutputStream(filePath));
                inputStream
        ) {
            inputStream.transferTo(outputStream);
        }
    }

    private void writeBlobToFile(Blob blob, Path filePath) throws IOException, SQLException {
        try (var blobInputStream = new BufferedInputStream(blob.getBinaryStream())) {
            writeStreamToFile(blobInputStream, filePath);
        }
    }

    private Blob readFileToBlob(Path filePath) throws IOException {
        File outputCacheFile = filePath.toFile();
        var reader = readFileToInputStream(filePath);
        return BlobProxy.generateProxy(reader, outputCacheFile.length());
    }

    private InputStream readFileToInputStream(Path filePath) throws IOException {
        return Files.newInputStream(filePath);
    }

    @Transactional
    public CacheFile mergeCacheFileToIncrementalCacheAndSave(CacheFileUploadDto cacheFileUploadDto, InputStream mergeableCacheFileInputStream, Long mergeableCacheFileSize) throws IOException, SQLException, UnsuccessfulCacheMergeException, NoNewCacheEntryException {
        Game game = gameService.findById(cacheFileUploadDto.getGameId())
                .orElseThrow();

        boolean incrementalCacheExists = game.getIncrementalCacheFile() != null && mergeableCacheFileSize > 0;
        // If we have incremental cache then merging is required
        Path incrementalCacheFilePath = Files.createTempFile(tempDirectoryPath, "IncrementalCache", ".dxvk-cache");
        if (incrementalCacheExists) {
            writeBlobToFile(game.getIncrementalCacheFile(), incrementalCacheFilePath);
        }

        Path mergeableCacheFilePath = Files.createTempFile(tempDirectoryPath, "MergeableCache", ".dxvk-cache");
        writeStreamToFile(mergeableCacheFileInputStream, mergeableCacheFilePath);

        Path outputCacheFilePath = Files.createTempFile(tempDirectoryPath, "MergedCacheOutput", ".dxvk-cache");

        try {
            MergeResultDto mergeResult;
            if (incrementalCacheExists) {
                mergeResult = dxvkCacheToolRunner.run(incrementalCacheFilePath, mergeableCacheFilePath, outputCacheFilePath);
            } else {
                mergeResult = dxvkCacheToolRunner.run(mergeableCacheFilePath, outputCacheFilePath);
            }

            if (!mergeResult.isSuccess()) {
                throw new UnsuccessfulCacheMergeException();
            }

            if (mergeResult.getSumOfEntriesMerged() <= 0) {
                throw new NoNewCacheEntryException();
            }

            // Read the merge result to Game::incrementalCacheFile
            Blob resultCacheFile = readFileToBlob(outputCacheFilePath);
            game.setIncrementalCacheFile(resultCacheFile);
            // Save it
            gameService.save(game);

            // We must read data from the file because the data stream we used before has been drained, so we cannot use it again
            BufferedInputStream mergedCacheFileInputStream = new BufferedInputStream(readFileToInputStream(mergeableCacheFilePath));
            CacheFile cacheFileToMerge = cacheFileMapper.toCacheFile(cacheFileUploadDto, mergedCacheFileInputStream, mergeableCacheFileSize);
            // Save the contributed cache file too and return the result
            return save(cacheFileToMerge);
        } finally {
            // Cleanup
            Files.deleteIfExists(incrementalCacheFilePath);
            Files.deleteIfExists(mergeableCacheFilePath);
            Files.deleteIfExists(outputCacheFilePath);
        }
    }

    public boolean existsById(Long cacheFileId) {
        return cacheFileRepository.existsById(cacheFileId);
    }

    public void flush() {
        cacheFileRepository.flush();
    }

    public void disownAllFromUploaderId(Long uploaderId) {
        cacheFileRepository.disownAllFromUploaderId(uploaderId);
    }

    public void deleteAllByGameId(Long gameId) {
        cacheFileRepository.deleteAllByGameId(gameId);
    }

    public void deleteAll() {
        cacheFileRepository.deleteAll();
    }
}
