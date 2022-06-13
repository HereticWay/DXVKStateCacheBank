package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.transaction.Transactional;
import java.sql.Blob;
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
    public ResponseEntity<Resource> getCacheFileData(@PathVariable("cacheFileId") Long cacheFileId) throws SQLException {
        CacheFile cacheFile = cacheFileService.findById(cacheFileId);
        Blob cacheFileBlob = cacheFile.getData();
        Game game = cacheFile.getGame();
        String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(cacheFileName)
                        .build()
        );

        InputStreamResource inputStreamResource = new InputStreamResource(cacheFileBlob.getBinaryStream());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(cacheFileBlob.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(inputStreamResource);
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
