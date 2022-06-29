package com.dxvkstatecachebank.dxvkstatecachebank.integration;

import com.dxvkstatecachebank.dxvkstatecachebank.data.TestDataCreator;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.*;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import java.io.InputStream;
import java.util.Arrays;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles(profiles = {"test"})
@Slf4j
class UserControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheFileService cacheFileService;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @Autowired
    private TestDataCreator testDataCreator;

    private ResponseEntity<UserInfoDto> postUser(UserCreateDto userCreateDto, Resource profilePictureResource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", profilePictureResource);
        map.add("userCreateDto", userCreateDto);

        var request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);

        return restTemplate.postForEntity(USER_ENDPOINT_URL, request, UserInfoDto.class);
    }

    private ResponseEntity<UserInfoDto> getUserById(Long userId) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        return restTemplate.getForEntity( url, UserInfoDto.class);
    }

    private ResponseEntity<CacheFileInfoDto[]> getAllCacheFilesByUserId(long userId) {
        String url = "%s/%d/cache_files".formatted(USER_ENDPOINT_URL, userId);
        return restTemplate.getForEntity(url, CacheFileInfoDto[].class);
    }

    private ResponseEntity<CacheFileInfoDto> postCacheFile(CacheFileUploadDto cacheFileUploadDto, Resource cacheFileResource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", cacheFileResource);
        map.add("cacheFileUploadDto", cacheFileUploadDto);

        var request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);
        return restTemplate.postForEntity(CACHE_FILE_ENDPOINT_URL, request, CacheFileInfoDto.class);
    }

    private ResponseEntity<GameInfoDto> postGame(GameCreateDto gameCreateDto) {
        return restTemplate.postForEntity(GAME_ENDPOINT_URL, gameCreateDto, GameInfoDto.class);
    }

    @Test
    void emptyDatabase_addNewUser_shouldReturnAddedUser() {
        ResponseEntity<UserInfoDto> creationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        UserInfoDto userInfoDto = creationResponse.getBody();
        assertThat(userInfoDto).isNotNull();
        assertThat(userInfoDto.getId()).isNotNull();
        assertThat(userInfoDto.getName()).isEqualTo(SAMPLE_USER_CREATE_DTO_1.getName());
        assertThat(userInfoDto.getEmail()).isEqualTo(SAMPLE_USER_CREATE_DTO_1.getEmail());
        assertThat(userInfoDto.getCacheFilesLink()).contains(String.valueOf(userInfoDto.getId()));
        assertThat(userInfoDto.getProfilePictureLink()).contains(String.valueOf(userInfoDto.getId()));

        ResponseEntity<UserInfoDto> getResponse = getUserById(userInfoDto.getId());
        assertThat(getResponse).isNotNull()
                .isEqualTo(creationResponse);
    }

    @Test
    void emptyDatabase_getUserBySomeRandomId_shouldReturn404NotFound() {
        ResponseEntity<UserInfoDto> getResponse = getUserById(54321L);
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void oneUserStored_getUserProfilePicture_returnedProfilePictureShouldHaveCorrectFileLength() {
        ResponseEntity<UserInfoDto> creationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        UserInfoDto createdUserInfo = creationResponse.getBody();
        assertThat(createdUserInfo).isNotNull();

        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        ResponseExtractor<Void> responseExtractor = response -> {
            InputStream in = response.getBody();
            // Count the bytes received from the server
            long bytesReceived = 0L;
            while (in.available() > 0) {
                int available = in.available();
                in.skipNBytes(available);
                bytesReceived += available;
            }

            // Assert the length of the picture we got back
            assertThat(bytesReceived).isEqualTo(PROFILE_PIC_1_RESOURCE.contentLength());
            return null;
        };

        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, createdUserInfo.getId());
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    @Test
    void addOneUser_addOneGame_addTwoCacheFiles_getUserCacheFilesShouldReturnTwoCacheEntries() {
        ResponseEntity<UserInfoDto> userCreationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        ResponseEntity<GameInfoDto> gameCreationResponse = postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        long userId = userCreationResponse.getBody().getId();
        long gameId = gameCreationResponse.getBody().getId();

        CacheFileUploadDto cacheFileUploadDto = CacheFileUploadDto.builder()
                .uploaderId(userId)
                .gameId(gameId)
                .build();

        postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_2_RESOURCE);

        ResponseEntity<CacheFileInfoDto[]> cacheFilesListResponse = getAllCacheFilesByUserId(userId);
        assertThat(cacheFilesListResponse).isNotNull();
        assertThat(cacheFilesListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cacheFilesListResponse.getBody()).isNotNull().hasSize(2);
    }
}
