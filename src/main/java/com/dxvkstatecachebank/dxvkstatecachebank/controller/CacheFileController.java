package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
