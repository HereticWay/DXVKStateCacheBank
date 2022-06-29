package com.dxvkstatecachebank.dxvkstatecachebank.integration;

import com.dxvkstatecachebank.dxvkstatecachebank.data.TestDataCreator;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    private final String USER_ENDPOINT_URL = "/user";

    private ResponseEntity<UserInfoDto> postUser(Resource profilePictureResource, UserCreateDto userCreateDto) {
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

    @Test
    void testCreateUserAndGetUser() {
        var profilePicResource = new ClassPathResource("sample/profile_pic_1.jpg");
        var userToPost = UserCreateDto.builder()
                .name("Some Body")
                .password("password12345")
                .email("abc@gmail.com")
                .build();

        ResponseEntity<UserInfoDto> creationResponse = postUser(profilePicResource, userToPost);
        assertThat(creationResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        UserInfoDto userInfoDto = creationResponse.getBody();
        assertThat(userInfoDto).isNotNull();
        assertThat(userInfoDto.getId()).isNotNull();
        assertThat(userInfoDto.getName()).isEqualTo(userToPost.getName());
        assertThat(userInfoDto.getEmail()).isEqualTo(userToPost.getEmail());
        assertThat(userInfoDto.getCacheFilesLink()).contains(String.valueOf(userInfoDto.getId()));
        assertThat(userInfoDto.getProfilePictureLink()).contains(String.valueOf(userInfoDto.getId()));

        ResponseEntity<UserInfoDto> getResponse = getUserById(userInfoDto.getId());
        assertThat(getResponse).isNotNull()
                .isEqualTo(creationResponse);
    }


}
