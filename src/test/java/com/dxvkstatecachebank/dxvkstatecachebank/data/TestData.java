package com.dxvkstatecachebank.dxvkstatecachebank.data;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import org.springframework.core.io.ClassPathResource;

public class TestData {
    public static final String USER_ENDPOINT_URL = "/user";
    public static final String CACHE_FILE_ENDPOINT_URL = "/cache_file";
    public static final String GAME_ENDPOINT_URL = "/game";

    public static final UserCreateDto SAMPLE_USER_CREATE_DTO_1 = UserCreateDto.builder()
            .name("Some Body")
            .password("password12345")
            .email("abc@gmail.com")
            .build();
    public static final UserCreateDto SAMPLE_USER_CREATE_DTO_2 = UserCreateDto.builder()
            .name("Spider Murphy")
            .password("12345")
            .email("spider.murphy@citromail.hu")
            .build();

    public static final GameCreateDto SAMPLE_GAME_CREATE_DTO_APEX = GameCreateDto.builder()
            .name("Apex Legends")
            .cacheFileName("r5apex")
            .steamId(1172470L)
            .build();
    public static final GameCreateDto SAMPLE_GAME_CREATE_DTO_OVERWATCH = GameCreateDto.builder()
            .name("Overwatch")
            .cacheFileName("Overwatch")
            .steamId(null)
            .build();

    public static final ClassPathResource SAMPLE_APEX_CACHE_FILE_1_RESOURCE = new ClassPathResource("sample/r5apex-barely-populated.dxvk-cache");
    public static final ClassPathResource SAMPLE_APEX_CACHE_FILE_2_RESOURCE = new ClassPathResource("sample/r5apex-highly-populated.dxvk-cache");
    public static final ClassPathResource SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE = new ClassPathResource("sample/overwatch.dxvk-cache");

    public static final ClassPathResource PROFILE_PIC_1_RESOURCE = new ClassPathResource("sample/profile_pic_1.jpg");
}
