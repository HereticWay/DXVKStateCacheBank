package com.dxvkstatecachebank.dxvkstatecachebank.unittest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.GameMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class GameMapperUnitTest {
    private GameMapper gameMapper = new GameMapper();

    private final Game SAMPLE_GAME_WITH_INCREMENTAL_CACHE =
            Game.builder()
                    .id(3222L)
                    .name("Some Game")
                    .cacheFileName("somegame1")
                    .incrementalCacheLastModified(LocalDateTime.now())
                    .steamId(453L)
                    .build();

    private final Game SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE =
            Game.builder()
                    .id(3333L)
                    .name("Some Other Game")
                    .cacheFileName("someothergame")
                    .incrementalCacheLastModified(null)
                    .steamId(111L)
                    .build();

    private final GameCreateDto SAMPLE_GAME_CREATE_DTO =
            GameCreateDto.builder()
                    .name("Game")
                    .steamId(null)
                    .cacheFileName("gamecachefile")
                    .build();

    @Test
    void testToDtoWithGameThatHasIncrementalCache() {
        GameInfoDto gameInfoDto = gameMapper.toDto(SAMPLE_GAME_WITH_INCREMENTAL_CACHE);

        assertThat(gameInfoDto.getId()).isEqualTo(SAMPLE_GAME_WITH_INCREMENTAL_CACHE.getId());
        assertThat(gameInfoDto.getName()).isEqualTo(SAMPLE_GAME_WITH_INCREMENTAL_CACHE.getName());
        assertThat(gameInfoDto.getCacheFileName()).isEqualTo(SAMPLE_GAME_WITH_INCREMENTAL_CACHE.getCacheFileName());
        assertThat(gameInfoDto.getIncrementalCacheFileLink()).contains(String.valueOf(SAMPLE_GAME_WITH_INCREMENTAL_CACHE.getId()));
        assertThat(gameInfoDto.getIncrementalCacheLastModified()).isEqualTo(SAMPLE_GAME_WITH_INCREMENTAL_CACHE.getIncrementalCacheLastModified());
        assertThat(gameInfoDto.getCacheFilesLink()).contains(String.valueOf(SAMPLE_GAME_WITH_INCREMENTAL_CACHE.getId()));
    }

    @Test
    void testToDtoWithGameThatHasNoIncrementalCache() {
        GameInfoDto gameInfoDto = gameMapper.toDto(SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE);

        assertThat(gameInfoDto.getId()).isEqualTo(SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE.getId());
        assertThat(gameInfoDto.getName()).isEqualTo(SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE.getName());
        assertThat(gameInfoDto.getCacheFileName()).isEqualTo(SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE.getCacheFileName());
        assertThat(gameInfoDto.getIncrementalCacheFileLink()).isNull();
        assertThat(gameInfoDto.getIncrementalCacheLastModified()).isNull();
        assertThat(gameInfoDto.getCacheFilesLink()).contains(String.valueOf(SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE.getId()));
    }

    @Test
    void testToGame() {
        Game game = gameMapper.toGame(SAMPLE_GAME_CREATE_DTO);

        assertThat(game.getName()).isEqualTo(SAMPLE_GAME_CREATE_DTO.getName());
        assertThat(game.getSteamId()).isEqualTo(SAMPLE_GAME_CREATE_DTO.getSteamId());
        assertThat(game.getCacheFileName()).isEqualTo(SAMPLE_GAME_CREATE_DTO.getCacheFileName());
        assertThat(game.getIncrementalCacheFile()).isNull();
    }
}
