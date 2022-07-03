package com.dxvkstatecachebank.dxvkstatecachebank.unittest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.GameMapper;
import org.junit.jupiter.api.Test;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class GameMapperUnitTest {
    private final GameMapper gameMapper = new GameMapper();

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
        Game game = gameMapper.toGame(SAMPLE_GAME_CREATE_DTO_APEX);

        assertThat(game.getName()).isEqualTo(SAMPLE_GAME_CREATE_DTO_APEX.getName());
        assertThat(game.getSteamId()).isEqualTo(SAMPLE_GAME_CREATE_DTO_APEX.getSteamId());
        assertThat(game.getCacheFileName()).isEqualTo(SAMPLE_GAME_CREATE_DTO_APEX.getCacheFileName());
        assertThat(game.getIncrementalCacheFile()).isNull();
    }
}
