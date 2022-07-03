package com.dxvkstatecachebank.dxvkstatecachebank.integrationtest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.*;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@Slf4j
class UserControllerIntegrationTest {
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
    void cleanupBeforeEach() {
        cacheFileService.deleteAll();
        gameService.deleteAll();
        userService.deleteAll();
    }

    @Test
    void emptyDatabase_getAllUsers_shouldReturnEmptyArray() {
        ResponseEntity<UserInfoDto[]> response = requestUtils.getAllUsers();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void emptyDatabase_addTwoUsers_getAllUsers_shouldReturnSizeTwoArray() {
        requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        requestUtils.postUser(SAMPLE_USER_CREATE_DTO_2, PROFILE_PIC_2_RESOURCE);

        ResponseEntity<UserInfoDto[]> response = requestUtils.getAllUsers();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(2);
    }

    @Test
    void emptyDatabase_addNewUser_getUserByItsId_shouldReturnAddedUser() {
        ResponseEntity<UserInfoDto> creationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        UserInfoDto userInfoDto = creationResponse.getBody();
        assertThat(userInfoDto).isNotNull();
        assertThat(userInfoDto.getId()).isNotNull();
        assertThat(userInfoDto.getName()).isEqualTo(SAMPLE_USER_CREATE_DTO_1.getName());
        assertThat(userInfoDto.getEmail()).isEqualTo(SAMPLE_USER_CREATE_DTO_1.getEmail());
        assertThat(userInfoDto.getCacheFilesLink()).contains(String.valueOf(userInfoDto.getId()));
        assertThat(userInfoDto.getProfilePictureLink()).contains(String.valueOf(userInfoDto.getId()));

        ResponseEntity<UserInfoDto> getResponse = requestUtils.getUserById(userInfoDto.getId());
        assertThat(getResponse).isNotNull()
                .isEqualTo(creationResponse);
    }

    @Test
    void emptyDatabase_getUserBySomeRandomId_shouldReturn404NotFound() {
        ResponseEntity<UserInfoDto> getResponse = requestUtils.getUserById(54321L);
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void oneUserStored_getUserProfilePicture_shouldHaveCorrectFileLength() {
        ResponseEntity<UserInfoDto> creationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(creationResponse.getBody()).isNotNull();
        long userId = creationResponse.getBody().getId();

        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        ResponseExtractor<Void> responseExtractor = response -> {
            final long correctContentLength = PROFILE_PIC_1_RESOURCE.contentLength();
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

            // Assert the length of the picture we got back
            assertThat(sumOfBytesReceived).isEqualTo(correctContentLength);
            return null;
        };

        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    @Test
    void addTwoCacheFiles_listUserCacheFiles_shouldReturnTwoCacheEntries() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        ResponseEntity<GameInfoDto> gameCreationResponse = requestUtils.postGame(SAMPLE_GAME_CREATE_DTO_APEX);
        assertThat(userCreationResponse.getBody()).isNotNull();
        assertThat(gameCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();
        long gameId = gameCreationResponse.getBody().getId();

        CacheFileUploadDto cacheFileUploadDto = CacheFileUploadDto.builder()
                .uploaderId(userId)
                .gameId(gameId)
                .build();

        requestUtils.postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_1_RESOURCE);
        requestUtils.postCacheFile(cacheFileUploadDto, SAMPLE_APEX_CACHE_FILE_2_RESOURCE);

        ResponseEntity<CacheFileInfoDto[]> cacheFilesListResponse = requestUtils.getAllCacheFilesByUserId(userId);
        assertThat(cacheFilesListResponse).isNotNull();
        assertThat(cacheFilesListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cacheFilesListResponse.getBody()).isNotNull().hasSize(2);
    }

    @Test
    void oneUserStored_updateUser_getUser_shouldReturnUpdatedUser() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .name("Claus Holczer")
                .password("extraStrongPassword")
                .email("claus@holzhoff.co")
                .build();
        requestUtils.updateUser(userId, userUpdateDto);

        ResponseEntity<UserInfoDto> getUserResponse = requestUtils.getUserById(userId);
        assertThat(getUserResponse).isNotNull();
        UserInfoDto userInfoDto = getUserResponse.getBody();
        assertThat(userInfoDto).isNotNull();
        assertThat(userInfoDto.getName()).isEqualTo(userUpdateDto.getName());
        assertThat(userInfoDto.getEmail()).isEqualTo(userUpdateDto.getEmail());
    }

    @Test
    void oneUserStored_updateUserProfilePicture_getUserProfilePicture_shouldHaveCorrectFileLength() throws IOException {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();
        requestUtils.updateUserProfilePicture(userId, PROFILE_PIC_2_RESOURCE);

        final long correctContentLength = PROFILE_PIC_2_RESOURCE.contentLength();
        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, userId);
        FileStreamSizeDto fileStreamSize = requestUtils.getFileStreamSize(url);
        assertThat(fileStreamSize.getResponseStatus()).isEqualTo(HttpStatus.OK);
        assertThat(fileStreamSize.getContentLengthHeader()).isEqualTo(correctContentLength);
        assertThat(fileStreamSize.getRealFileLength()).isEqualTo(correctContentLength);
    }

    @Test
    void emptyDatabase_deleteUserByRandomId_shouldReturn404NotFound() {
        long randomId = 1234L;
        assertThat(requestUtils.deleteUser(randomId).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void oneUserStored_deleteUser_getAllUsers_shouldReturnEmptyArray() {
        ResponseEntity<UserInfoDto> userCreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        assertThat(userCreationResponse.getBody()).isNotNull();
        long userId = userCreationResponse.getBody().getId();
        requestUtils.deleteUser(userId);

        ResponseEntity<UserInfoDto[]> allUsersResponse = requestUtils.getAllUsers();
        assertThat(allUsersResponse).isNotNull();
        assertThat(allUsersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allUsersResponse.getBody()).isNotNull().isEmpty();
    }

    @Test
    void twoUsersStored_deleteFirstUser_getFirstUser_shouldReturn404NotFound_getSecondUser_shouldReturnSecondUser() {
        ResponseEntity<UserInfoDto> user1CreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_1, PROFILE_PIC_1_RESOURCE);
        ResponseEntity<UserInfoDto> user2CreationResponse = requestUtils.postUser(SAMPLE_USER_CREATE_DTO_2, PROFILE_PIC_2_RESOURCE);
        assertThat(user1CreationResponse.getBody()).isNotNull();
        assertThat(user2CreationResponse.getBody()).isNotNull();
        long user1Id = user1CreationResponse.getBody().getId();
        long user2Id = user2CreationResponse.getBody().getId();

        requestUtils.deleteUser(user1Id);

        ResponseEntity<UserInfoDto> firstUserResponse = requestUtils.getUserById(user1Id);
        assertThat(firstUserResponse).isNotNull();
        assertThat(firstUserResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<UserInfoDto> secondUserResponse = requestUtils.getUserById(user2Id);
        assertThat(secondUserResponse).isNotNull();
        assertThat(secondUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondUserResponse.getBody()).isEqualTo(user2CreationResponse.getBody());
    }
}
