package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameInfoDto toDto(Game game) {
        return GameInfoDto.builder()
                .id(game.getId())
                .name(game.getName())
                .incrementalCacheFileLink("/game/%d/incremental_cache_file".formatted(game.getId()))
                .cacheFilesLink("/game/%d/cache_files".formatted(game.getId()))
                .steamId(game.getSteamId())
                .build();
    }

    public Game toGame(GameCreateDto gameCreateDto) {
        return Game.builder()
                .name(gameCreateDto.getName())
                .steamId(gameCreateDto.getSteamId())
                .cacheFileName(gameCreateDto.getCacheFileName())
                .incrementalCacheFile(BlobProxy.generateProxy(new byte[0]))
                .build();
    }
}
