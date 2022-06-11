package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    public GameInfoDto toDto(Game game) {
        Long incrementalCacheFileId = game.getLatestCacheFile().getId();
        return GameInfoDto.builder()
                .id(game.getId())
                .name(game.getName())
                .incrementalCacheFileLink("/cachefile/%d/data".formatted(incrementalCacheFileId))
                .contributionsLink("/cachefile/game/%d".formatted(game.getId()))
                .steamId(game.getSteamId())
                .build();
    }

    public Game toGame(GameCreateDto gameCreateDto) {
        return Game.builder()
                .name(gameCreateDto.getName())
                .steamId(gameCreateDto.getSteamId())
                .build();
    }
}
