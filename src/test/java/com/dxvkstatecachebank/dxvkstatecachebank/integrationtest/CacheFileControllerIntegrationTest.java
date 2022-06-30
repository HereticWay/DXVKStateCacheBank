package com.dxvkstatecachebank.dxvkstatecachebank.integrationtest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.*;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import com.dxvkstatecachebank.dxvkstatecachebank.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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

    private final String URL = "/cache_file";

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
    void oneCacheFileStored_getCacheFileData_returnedFileShouldBeTheCorrectLength() {
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

        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        ResponseExtractor<Void> responseExtractor = response -> {
            final long correctContentLength = SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE.contentLength();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Assert that content length header is correct
            assertThat(response.getHeaders().getContentLength()).isEqualTo(correctContentLength);
            InputStream inputStream = response.getBody();

            // Count the bytes received from the server
            long sumOfBytesReceived = 0L;
            int bytesReceived;
            var readBuffer = new byte[4096];
            while ((bytesReceived = inputStream.read(readBuffer)) != -1) {
                sumOfBytesReceived += bytesReceived;
            }

            // Assert the real length of the cache file we got back
            assertThat(sumOfBytesReceived).isEqualTo(correctContentLength);
            return null;
        };

        String url = "%s/%d/data".formatted(CACHE_FILE_ENDPOINT_URL, uploadedCacheId);
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    @Test
    void emptyDatabase_getRandomIdCacheFileData_shouldReturn404NotFound() {
        long randomId = 4242L;
        String url = "%s/%d/data".formatted(CACHE_FILE_ENDPOINT_URL, randomId);

        assertThat(restTemplate.getForEntity(url, Object.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
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

        requestUtils.deleteCacheFile(secondCacheFileId);

        assertThat(requestUtils.getCacheFile(firstCacheFileId).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(requestUtils.getCacheFile(secondCacheFileId).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
