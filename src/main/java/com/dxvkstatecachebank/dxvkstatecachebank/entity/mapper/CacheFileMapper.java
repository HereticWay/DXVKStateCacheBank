package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;

@Component
public class CacheFileMapper {
    @Autowired
    private GameService gameService;

    public CacheFileInfoDto toDto(CacheFile cacheFile) {
        Long uploaderId = cacheFile.getUploader().getId();
        Long gameId = cacheFile.getGame().getId();

        return CacheFileInfoDto.builder()
                .id(cacheFile.getId())
                .uploadDateTime(cacheFile.getUploadDateTime())
                .uploaderLink("/user/%d".formatted(uploaderId))
                .gameLink("/game/%d".formatted(gameId))
                .dataLink("/cache_file/%d/data".formatted(cacheFile.getId()))
                .build();
    }

    public CacheFile toCacheFile(CacheFileUploadDto cacheFileUploadDto, InputStream cacheFileInputStream, Long cacheFileSize) {
        Long uploaderId = cacheFileUploadDto.getUploaderId();
        Long gameId = cacheFileUploadDto.getGameId();
        Game game = gameService.findById(gameId)
                .orElseThrow();

        return CacheFile.builder()
                .uploader(User.builder().id(uploaderId).build())
                .uploadDateTime(LocalDateTime.now())
                .game(game)
                .data(BlobProxy.generateProxy(cacheFileInputStream, cacheFileSize))
                .build();
    }
}
