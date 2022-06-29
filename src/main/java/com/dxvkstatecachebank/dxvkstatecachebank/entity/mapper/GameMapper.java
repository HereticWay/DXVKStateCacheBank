package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class GameMapper {
    public GameInfoDto toDto(Game game) {
        Optional<LocalDateTime> incrementalCacheLastModified = Optional.ofNullable(game.getIncrementalCacheLastModified());

        return GameInfoDto.builder()
                .id(game.getId())
                .name(game.getName())
                .incrementalCacheFileLink(incrementalCacheLastModified.isPresent() ? "/game/%d/incremental_cache_file".formatted(game.getId()) : null)
                .incrementalCacheLastModified(incrementalCacheLastModified.orElse(null))
                .cacheFilesLink("/game/%d/cache_files".formatted(game.getId()))
                .steamId(game.getSteamId())
                .build();
    }

    public Game toGame(GameCreateDto gameCreateDto) {
        return Game.builder()
                .name(gameCreateDto.getName())
                .steamId(gameCreateDto.getSteamId())
                .cacheFileName(gameCreateDto.getCacheFileName())
                .incrementalCacheFile(null)
                .build();
    }
}
