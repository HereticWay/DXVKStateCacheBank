package com.dxvkstatecachebank.dxvkstatecachebank.integrationtest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import com.dxvkstatecachebank.dxvkstatecachebank.util.RequestUtils;
import com.dxvkstatecachebank.dxvkstatecachebank.util.dto.FileStreamSizeDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@Slf4j
public class GameControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RequestUtils requestUtils;

    @Autowired
    private CacheFileService cacheFileService;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @BeforeEach
    @AfterEach
    void cleanup() {
        cacheFileService.deleteAll();
        gameService.deleteAll();
        userService.deleteAll();
    }

    @Test
    void emptyDatabase_getAllGames_shouldReturnEmptyArray() {
        ResponseEntity<GameInfoDto[]> response = requestUtils.getAllGames();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void emptyDatabase_addTwoGames_getAllGames_shouldReturnTwoSizedArray() {
        requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_OVERWATCH);

        ResponseEntity<GameInfoDto[]> response = requestUtils.getAllGames();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(2);
    }

    @Test
    void emptyDatabase_getGameBySomeRandomId_shouldReturn404NotFound() {
        ResponseEntity<GameInfoDto> getResponse = requestUtils.getGameById(54321L);
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void emptyDatabase_addNewGame_getGameByItsId_shouldReturnAddedGame() {
        ResponseEntity<GameInfoDto> creationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        GameInfoDto gameInfoDto = creationResponse.getBody();
        assertThat(gameInfoDto).isNotNull();
        assertThat(gameInfoDto.getId()).isNotNull();
        assertThat(gameInfoDto.getName()).isEqualTo(SAMPLE_GAME_CREATE_DTO_APEX.getName());
        assertThat(gameInfoDto.getIncrementalCacheFileLink()).isNull();
        assertThat(gameInfoDto.getIncrementalCacheLastModified()).isNull();
        assertThat(gameInfoDto.getCacheFilesLink()).contains(String.valueOf(gameInfoDto.getId()));
        assertThat(gameInfoDto.getSteamId()).isEqualTo(SAMPLE_GAME_CREATE_DTO_APEX.getSteamId());

        ResponseEntity<GameInfoDto> getResponse = requestUtils.getGameById(gameInfoDto.getId());
        assertThat(getResponse.getBody()).isNotNull()
                .isEqualTo(creationResponse.getBody());
    }

    @Test
    void emptyDatabase_getGamesIncrementalCacheFileBySomeRandomId_shouldReturn404NotFound() {
        long randomGameId = 34321546L;
        String url = "%s/%d/incremental_cache_file".formatted(GAME_ENDPOINT_URL, randomGameId);
        ResponseEntity<Object> getResponse = restTemplate.getForEntity(url, Object.class);
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void oneGameStoredWithNoIncrementalCacheFile_getGamesIncrementalCacheFile_shouldReturn404NotFound() {
        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_OVERWATCH);
        assertThat(gameCreationResponse.getBody()).isNotNull();

        long gameId = gameCreationResponse.getBody().getId();
        String url = "%s/%d/incremental_cache_file".formatted(GAME_ENDPOINT_URL, gameId);
        ResponseEntity<Object> getResponse = restTemplate.getForEntity(url, Object.class);
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void oneGameStoredWithIncrementalCacheFile_getGamesIncrementalCacheFile_returnedFileShouldBeTheCorrectLength() throws IOException {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();

        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_OVERWATCH);
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long gameId = gameCreationResponse.getBody()
                .getId();

        requestUtils.postCacheFile(
                CacheFileUploadDto.builder()
                        .uploaderId(userId)
                        .gameId(gameId)
                        .build(),
                SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE
        );

        long correctContentLength = SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE.contentLength();
        String url = "%s/%d/incremental_cache_file".formatted(GAME_ENDPOINT_URL, gameId);
        FileStreamSizeDto fileStreamSize = requestUtils.getFileStreamSize(url);
        assertThat(fileStreamSize.getResponseStatus()).isEqualTo(HttpStatus.OK);
        assertThat(fileStreamSize.getContentLengthHeader()).isEqualTo(correctContentLength);
        assertThat(fileStreamSize.getRealFileLength()).isEqualTo(correctContentLength);
    }
}
