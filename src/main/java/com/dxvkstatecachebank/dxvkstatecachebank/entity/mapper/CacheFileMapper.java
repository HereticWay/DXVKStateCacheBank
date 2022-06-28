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
import java.util.Objects;
import java.util.Optional;

@Component
public class CacheFileMapper {
    @Autowired
    private GameService gameService;

    public CacheFileInfoDto toDto(CacheFile cacheFile) {
        Game game = Objects.requireNonNull(cacheFile.getGame());
        Optional<User> uploader = Optional.ofNullable(cacheFile.getUploader());

        Long gameId = game.getId();
        Long uploaderId = uploader.map(User::getId)
                .orElse(null);

        return CacheFileInfoDto.builder()
                .id(cacheFile.getId())
                .uploadDateTime(cacheFile.getUploadDateTime())
                .uploaderLink(uploaderId == null ? null : "/user/%d".formatted(uploaderId))
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
