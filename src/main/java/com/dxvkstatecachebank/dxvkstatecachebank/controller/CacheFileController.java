package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/cachefile")
public class CacheFileController {
    @Autowired
    private CacheFileService cacheFileService;

    @Autowired
    private CacheFileMapper cacheFileMapper;

    @GetMapping("/game/{gameId}")
    public List<CacheFileInfoDto> listCacheFilesForGameId(@PathVariable("gameId") Long gameId) {
        return cacheFileService.findAllByGameId(gameId).stream()
                .map(cacheFile -> cacheFileMapper.toDto(cacheFile))
                .toList();
    }

    @GetMapping("/{cacheFileId}")
    public CacheFileInfoDto findCacheFileById(@PathVariable("cacheFileId") Long cacheFileId) {
        return cacheFileMapper.toDto(cacheFileService.findById(cacheFileId));
    }

    @Transactional
    @GetMapping("/{cacheFileId}/data")
    public ResponseEntity<StreamingResponseBody> getCacheFileData(@PathVariable("cacheFileId") Long cacheFileId, final HttpServletResponse response) {
        CacheFile cacheFile = cacheFileService.findById(cacheFileId);
        String cacheFileName = cacheFile.getGame()
                .getCacheFileName();

        response.setContentType("application/octet-stream");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=%s.dxvk-cache".formatted(cacheFileName)
        );

        StreamingResponseBody streamingResponseBody = outputStream -> {
            InputStream cacheFileBinaryStreamFromDb;
            try {
                cacheFileBinaryStreamFromDb = cacheFile.getData().getBinaryStream();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            cacheFileBinaryStreamFromDb.transferTo(outputStream);
            outputStream.close();
            cacheFileBinaryStreamFromDb.close();
        };

        return ResponseEntity.ok(streamingResponseBody);
    }

    @PostMapping
    public CacheFileInfoDto uploadCacheFile(@RequestBody CacheFileUploadDto cacheFileUploadDto) {
        CacheFile cacheFileCreated = cacheFileService.save(cacheFileMapper.toCacheFile(cacheFileUploadDto));
        return cacheFileMapper.toDto(cacheFileCreated);
    }

    @DeleteMapping("/{cacheFileId}")
    public void deleteCacheFile(@PathVariable("cacheFileId") Long cacheFileId) {
        cacheFileService.deleteById(cacheFileId);
    }
}
