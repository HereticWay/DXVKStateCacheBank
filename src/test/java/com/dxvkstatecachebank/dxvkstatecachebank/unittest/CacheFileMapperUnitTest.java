package com.dxvkstatecachebank.dxvkstatecachebank.unittest;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheFileMapperUnitTest {

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CacheFileMapper cacheFileMapper;

    private final User SAMPLE_UPLOADER =
            User.builder()
                    .id(644L)
                    .build();
    private final Game SAMPLE_GAME =
            Game.builder()
                    .id(3222L)
                    .build();

    @Test
    void uploaderExists_mapToDto_shouldContainCorrectData() {
        LocalDateTime dateTime = LocalDateTime.now();
        CacheFile cacheFile = CacheFile.builder()
                .id(1L)
                .uploader(SAMPLE_UPLOADER)
                .game(SAMPLE_GAME)
                .uploadDateTime(dateTime)
                .build();

        CacheFileInfoDto dto = cacheFileMapper.toDto(cacheFile);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(cacheFile.getId());
        assertThat(dto.getUploadDateTime()).isEqualTo(dateTime);
        assertThat(dto.getUploaderLink()).contains(String.valueOf(SAMPLE_UPLOADER.getId()));
        assertThat(dto.getGameLink()).contains(String.valueOf(SAMPLE_GAME.getId()));
        assertThat(dto.getDataLink()).contains(String.valueOf(cacheFile.getId()));
    }

    @Test
    void uploaderDoesNotExists_mapToDto_shouldContainCorrectData() {
        LocalDateTime dateTime = LocalDateTime.now();
        CacheFile cacheFile = CacheFile.builder()
                .id(1L)
                .uploader(null)
                .game(SAMPLE_GAME)
                .uploadDateTime(dateTime)
                .build();

        CacheFileInfoDto dto = cacheFileMapper.toDto(cacheFile);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(cacheFile.getId());
        assertThat(dto.getUploadDateTime()).isEqualTo(dateTime);
        assertThat(dto.getUploaderLink()).isNull();
        assertThat(dto.getGameLink()).contains(String.valueOf(SAMPLE_GAME.getId()));
        assertThat(dto.getDataLink()).contains(String.valueOf(cacheFile.getId()));
    }

    @Test
    void gameExists_mapToCacheFile_shouldReturnCorrectData() {
        CacheFileUploadDto uploadDto = CacheFileUploadDto.builder()
                .uploaderId(SAMPLE_UPLOADER.getId())
                .gameId(SAMPLE_GAME.getId())
                .build();

        when(gameService.findById(SAMPLE_GAME.getId()))
                .thenReturn(Optional.of(SAMPLE_GAME));
        when(userService.findById(SAMPLE_UPLOADER.getId()))
                .thenReturn(Optional.of(SAMPLE_UPLOADER));

        CacheFile cacheFile = cacheFileMapper.toCacheFile(
                uploadDto,
                InputStream.nullInputStream(),
                0L
        );
        assertThat(cacheFile).isNotNull();
        assertThat(cacheFile.getUploader()).isEqualTo(SAMPLE_UPLOADER);
        assertThat(cacheFile.getUploadDateTime()).isBetween(LocalDateTime.now().minusMinutes(1L), LocalDateTime.now());
        assertThat(cacheFile.getGame()).isEqualTo(SAMPLE_GAME);
        assertThat(cacheFile.getData()).isNotNull();
    }
}
