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

    private void updateUser(long userId, UserUpdateDto userUpdateDto) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.put(url, userUpdateDto);
    }

    private void deleteUser(long userId) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.delete(url);
    }

    private void updateUserProfilePicture(long userId, Resource profilePictureResource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", profilePictureResource);

        var request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);
        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.put(url, request);
    }

    private ResponseEntity<UserInfoDto> getUserById(Long userId) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        return restTemplate.getForEntity(url, UserInfoDto.class);
    }

    private ResponseEntity<UserInfoDto[]> getAllUsers() {
        return restTemplate.getForEntity(USER_ENDPOINT_URL, UserInfoDto[].class);
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
    void emptyDatabase_getAllUsers_shouldReturnEmptyArray() {
        ResponseEntity<UserInfoDto[]> response = getAllUsers();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void emptyDatabase_addTwoUsers_getAllUsers_shouldReturnSizeTwoArray() {
        postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        postUser(SAMPLE_USER_CREATE_DTO_2, PROFILE_PIC_2_RESOURCE);

        ResponseEntity<UserInfoDto[]> response = getAllUsers();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(2);
    }

    @Test
    void emptyDatabase_addNewUser_getUserByItsId_shouldReturnAddedUser() {
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
    void oneUserStored_getUserProfilePicture_shouldHaveCorrectFileLength() {
        ResponseEntity<UserInfoDto> creationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(creationResponse.getBody()).isNotNull();
        long userId = creationResponse.getBody().getId();

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

        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    @Test
    void addOneUser_addOneGame_addTwoCacheFiles_listUserCacheFiles_shouldReturnTwoCacheEntries() {
        ResponseEntity<UserInfoDto> userCreationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        ResponseEntity<GameInfoDto> gameCreationResponse = postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(userCreationResponse.getBody()).isNotNull();
        assertThat(gameCreationResponse.getBody()).isNotNull();
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

    @Test
    void oneUserStored_updateUser_getUser_shouldReturnUpdatedUser() {
        ResponseEntity<UserInfoDto> userCreationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .name("Claus Holczer")
                .password("extraStrongPassword")
                .email("claus@holzhoff.co")
                .build();
        updateUser(userId, userUpdateDto);

        ResponseEntity<UserInfoDto> getUserResponse = getUserById(userId);
        assertThat(getUserResponse).isNotNull();
        UserInfoDto userInfoDto = getUserResponse.getBody();
        assertThat(userInfoDto).isNotNull();
        assertThat(userInfoDto.getName()).isEqualTo(userUpdateDto.getName());
        assertThat(userInfoDto.getEmail()).isEqualTo(userUpdateDto.getEmail());
    }

    @Test
    void oneUserStored_updateUserProfilePicture_getUserProfilePicture_shouldHaveCorrectFileLength() {
        ResponseEntity<UserInfoDto> userCreationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();
        updateUserProfilePicture(userId, PROFILE_PIC_2_RESOURCE);

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
            assertThat(bytesReceived).isEqualTo(PROFILE_PIC_2_RESOURCE.contentLength());
            return null;
        };

        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    @Test
    void oneUserStored_deleteUser_getAllUsers_shouldReturnEmptyArray() {
        ResponseEntity<UserInfoDto> userCreationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();
        deleteUser(userId);

        ResponseEntity<UserInfoDto[]> allUsersResponse = getAllUsers();
        assertThat(allUsersResponse).isNotNull();
        assertThat(allUsersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allUsersResponse.getBody()).isNotNull().isEmpty();
    }

    @Test
    void twoUsersStored_deleteFirstUser_getFirstUser_shouldReturn404NotFound_getSecondUser_shouldReturnSecondUser() {
        ResponseEntity<UserInfoDto> user1CreationResponse = postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        ResponseEntity<UserInfoDto> user2CreationResponse = postUser(SAMPLE_USER_CREATE_DTO_2, PROFILE_PIC_2_RESOURCE);
        assertThat(user1CreationResponse.getBody()).isNotNull();
        assertThat(user2CreationResponse.getBody()).isNotNull();
        long user1Id = user1CreationResponse.getBody().getId();
        long user2Id = user2CreationResponse.getBody().getId();

        deleteUser(user1Id);

        ResponseEntity<UserInfoDto> firstUserResponse = getUserById(user1Id);
        assertThat(firstUserResponse).isNotNull();
        assertThat(firstUserResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<UserInfoDto> secondUserResponse = getUserById(user2Id);
        assertThat(secondUserResponse).isNotNull();
        assertThat(secondUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondUserResponse.getBody()).isEqualTo(user2CreationResponse.getBody());
    }
}
