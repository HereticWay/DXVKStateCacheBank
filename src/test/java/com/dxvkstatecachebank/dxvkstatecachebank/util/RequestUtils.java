package com.dxvkstatecachebank.dxvkstatecachebank.util;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.*;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.*;

@Component
public class RequestUtils {
    private final TestRestTemplate restTemplate;
    private final CacheFileService cacheFileService;
    private final GameService gameService;
    private final UserService userService;

    public RequestUtils(TestRestTemplate restTemplate, CacheFileService cacheFileService, GameService gameService, UserService userService) {
        this.restTemplate = restTemplate;
        this.cacheFileService = cacheFileService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public ResponseEntity<UserInfoDto> getUserById(Long userId) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        return restTemplate.getForEntity(url, UserInfoDto.class);
    }

    public ResponseEntity<GameInfoDto> getGameById(long gameId) {
        String url = "%s/%d".formatted(GAME_ENDPOINT_URL, gameId);
        return restTemplate.getForEntity(url, GameInfoDto.class);
    }

    public ResponseEntity<UserInfoDto[]> getAllUsers() {
        return restTemplate.getForEntity(USER_ENDPOINT_URL, UserInfoDto[].class);
    }

    public ResponseEntity<GameInfoDto[]> getAllGames() {
        return restTemplate.getForEntity(GAME_ENDPOINT_URL, GameInfoDto[].class);
    }

    public ResponseEntity<CacheFileInfoDto[]> getAllCacheFilesByUserId(long userId) {
        String url = "%s/%d/cache_files".formatted(USER_ENDPOINT_URL, userId);
        return restTemplate.getForEntity(url, CacheFileInfoDto[].class);
    }

    public ResponseEntity<CacheFileInfoDto> getCacheFile(Long cacheFileId) {
        String url = "%s/%d".formatted(CACHE_FILE_ENDPOINT_URL, cacheFileId);
        return restTemplate.getForEntity(url, CacheFileInfoDto.class);
    }

    public ResponseEntity<UserInfoDto> postUser(UserCreateDto userCreateDto, Resource profilePictureResource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", profilePictureResource);
        map.add("userCreateDto", userCreateDto);

        var request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);

        return restTemplate.postForEntity(USER_ENDPOINT_URL, request, UserInfoDto.class);
    }

    public ResponseEntity<CacheFileInfoDto> postCacheFile(CacheFileUploadDto cacheFileUploadDto, Resource cacheFileResource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", cacheFileResource);
        map.add("cacheFileUploadDto", cacheFileUploadDto);

        var request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);
        return restTemplate.postForEntity(CACHE_FILE_ENDPOINT_URL, request, CacheFileInfoDto.class);
    }

    public ResponseEntity<GameInfoDto> postGame(GameCreateDto gameCreateDto) {
        return restTemplate.postForEntity(GAME_ENDPOINT_URL, gameCreateDto, GameInfoDto.class);
    }

    public void updateUser(long userId, UserUpdateDto userUpdateDto) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.put(url, userUpdateDto);
    }

    public void updateUserProfilePicture(long userId, Resource profilePictureResource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", profilePictureResource);

        var request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);
        String url = "%s/%d/profile_picture".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.put(url, request);
    }

    public void deleteUser(long userId) {
        String url = "%s/%d".formatted(USER_ENDPOINT_URL, userId);
        restTemplate.delete(url);
    }

    public void deleteCacheFile(long cacheFileId) {
        String url = "%s/%d".formatted(CACHE_FILE_ENDPOINT_URL, cacheFileId);
        restTemplate.delete(url);
    }
}
