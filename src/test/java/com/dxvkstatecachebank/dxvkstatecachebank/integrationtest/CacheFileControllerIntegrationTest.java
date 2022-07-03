package com.dxvkstatecachebank.dxvkstatecachebank.integrationtest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
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
import java.time.LocalDateTime;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@Slf4j
class CacheFileControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheFileService cacheFileService;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestUtils requestUtils;

    @BeforeEach
    @AfterEach
    void cleanup() {
        cacheFileService.deleteAll();
        gameService.deleteAll();
        userService.deleteAll();
    }

    @Test
    void emptyDatabase_getCacheFileByRandomId_shouldReturn404NotFound() {
        long randomId = 231L;
        ResponseEntity<CacheFileInfoDto> cacheFileGetResponse = requestUtils.getCacheFile(randomId);
        assertThat(cacheFileGetResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void oneCacheFileStored_getCacheFile_shouldReturnCorrectCacheFile() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody()
                .getId();

        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long gameId = gameCreationResponse.getBody()
                .getId();

        CacheFileUploadDto cacheFileUploadDto = CacheFileUploadDto.builder()
                .uploaderId(userId)
                .gameId(gameId)
                .build();

        ResponseEntity<CacheFileInfoDto> uploadResponse = requestUtils.postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(uploadResponse.getBody()).isNotNull();

        long uploadedCacheId = uploadResponse.getBody().getId();
        ResponseEntity<CacheFileInfoDto> cacheFileGetResponse = requestUtils.getCacheFile(uploadedCacheId);
        assertThat(cacheFileGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CacheFileInfoDto cacheFileInfoDto = cacheFileGetResponse.getBody();
        assertThat(cacheFileInfoDto).isNotNull();
        assertThat(cacheFileInfoDto.getId()).isEqualTo(uploadedCacheId);
        assertThat(cacheFileInfoDto.getUploaderLink()).contains(String.valueOf(userId));
        assertThat(cacheFileInfoDto.getGameLink()).contains(String.valueOf(gameId));
        assertThat(cacheFileInfoDto.getDataLink()).contains(String.valueOf(uploadedCacheId));
        assertThat(cacheFileGetResponse.getBody().getUploadDateTime())
                .isBetween(
                        LocalDateTime.now().minusMinutes(1L),
                        LocalDateTime.now()
                );
    }

    @Test
    void uploadCacheFileWithInvalidDto_shouldReturn422UnprocessableEntity() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long realUserId = userCreationResponse.getBody()
                .getId();

        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long realGameId = gameCreationResponse.getBody()
                .getId();

        CacheFileUploadDto dto = CacheFileUploadDto.builder()
                .uploaderId(null)
                .gameId(null)
                .build();
        ResponseEntity<CacheFileInfoDto> response = requestUtils.postCacheFile(dto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        dto = CacheFileUploadDto.builder()
                .uploaderId(realUserId)
                .gameId(null)
                .build();
        response = requestUtils.postCacheFile(dto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        dto = CacheFileUploadDto.builder()
                .uploaderId(null)
                .gameId(realGameId)
                .build();
        response = requestUtils.postCacheFile(dto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        long randomNonExistentGameId = 432L;
        long randomNonExistentUserId = 435L;
        dto = CacheFileUploadDto.builder()
                .uploaderId(realUserId)
                .gameId(randomNonExistentGameId)
                .build();
        response = requestUtils.postCacheFile(dto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        dto = CacheFileUploadDto.builder()
                .uploaderId(randomNonExistentUserId)
                .gameId(realGameId)
                .build();
        response = requestUtils.postCacheFile(dto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        dto = CacheFileUploadDto.builder()
                .uploaderId(randomNonExistentUserId)
                .gameId(randomNonExistentGameId)
                .build();
        response = requestUtils.postCacheFile(dto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void uploadInvalidCacheFile_shouldReturn422UnprocessableEntity() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody()
                .getId();

        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long gameId = gameCreationResponse.getBody()
                .getId();

        CacheFileUploadDto cacheFileUploadDto = CacheFileUploadDto.builder()
                .uploaderId(userId)
                .gameId(gameId)
                .build();

        ResponseEntity<CacheFileInfoDto> response = requestUtils.postCacheFile(cacheFileUploadDto, SAMPLE_INVALID_CACHE_FILE_RESOURCE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void oneCacheFileStored_getCacheFileData_returnedFileShouldBeTheCorrectLength() throws IOException {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();

        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_OVERWATCH);
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long gameId = gameCreationResponse.getBody()
                .getId();

        ResponseEntity<CacheFileInfoDto> cacheFileCreationResponse = requestUtils.postCacheFile(
                CacheFileUploadDto.builder()
                        .uploaderId(userId)
                        .gameId(gameId)
                        .build(),
                SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE
        );
        assertThat(cacheFileCreationResponse.getBody()).isNotNull();
        long uploadedCacheId = cacheFileCreationResponse.getBody()
                .getId();

        String url = "%s/%d/data".formatted(CACHE_FILE_ENDPOINT_URL, uploadedCacheId);
        final long correctContentLength = SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE.contentLength();
        FileStreamSizeDto fileStreamSize = requestUtils.getFileStreamSize(url);
        assertThat(fileStreamSize.getResponseStatus()).isEqualTo(HttpStatus.OK);
        assertThat(fileStreamSize.getContentLengthHeader()).isEqualTo(correctContentLength);
        assertThat(fileStreamSize.getRealFileLength()).isEqualTo(correctContentLength);
    }

    @Test
    void emptyDatabase_getRandomIdCacheFileData_shouldReturn404NotFound() {
        long randomId = 4242L;
        String url = "%s/%d/data".formatted(CACHE_FILE_ENDPOINT_URL, randomId);

        assertThat(restTemplate.getForEntity(url, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void emptyDatabase_deleteCacheFileByRandomId_shouldReturn404NotFound() {
        long randomId = 9831L;
        assertThat(requestUtils.deleteCacheFile(randomId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void twoCacheFilesStored_deleteSecondOne_getFirstOne_shoudlReturnFirstOne_getSecondOne_shouldReturn404NotFound() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody()
                .getId();

        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long gameId = gameCreationResponse.getBody()
                .getId();

        CacheFileUploadDto cacheFileUploadDto = CacheFileUploadDto.builder()
                .uploaderId(userId)
                .gameId(gameId)
                .build();

        ResponseEntity<CacheFileInfoDto> cacheFile1CreationResponse = requestUtils.postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        assertThat(cacheFile1CreationResponse.getBody()).isNotNull();
        long firstCacheFileId = cacheFile1CreationResponse.getBody().getId();

        ResponseEntity<CacheFileInfoDto> cacheFile2CreationResponse = requestUtils.postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_2_RESOURCE);
        assertThat(cacheFile2CreationResponse.getBody()).isNotNull();
        long secondCacheFileId = cacheFile2CreationResponse.getBody().getId();

        assertThat(requestUtils.deleteCacheFile(secondCacheFileId).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(requestUtils.getCacheFile(firstCacheFileId).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(requestUtils.getCacheFile(secondCacheFileId).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
