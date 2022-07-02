package com.dxvkstatecachebank.dxvkstatecachebank.data;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDateTime;

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

    public static final User SAMPLE_USER = User.builder()
            .id(644L)
            .name("Samantha1")
            .email("abc@def.com")
            .build();

    public static final Game SAMPLE_GAME = Game.builder()
            .id(3222L)
            .build();

    public static final Game SAMPLE_GAME_WITH_INCREMENTAL_CACHE =
            Game.builder()
                    .id(3222L)
                    .name("Some Game")
                    .cacheFileName("somegame1")
                    .incrementalCacheLastModified(LocalDateTime.now())
                    .steamId(453L)
                    .build();

    public static final Game SAMPLE_GAME_WITH_NO_INCREMENTAL_CACHE =
            Game.builder()
                    .id(3333L)
                    .name("Some Other Game")
                    .cacheFileName("someothergame")
                    .incrementalCacheLastModified(null)
                    .steamId(111L)
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
    public static final ClassPathResource SAMPLE_OVERWATCH_CACHE_FILE_1_RESOURCE = new ClassPathResource("sample/Overwatch.dxvk-cache");

    public static final ByteArrayResource SAMPLE_INVALID_CACHE_FILE_RESOURCE = new ByteArrayResource(new byte[]{0x12, 0xF, 0xA, 0x4, -1}) {
        // I must override this to be able to post as multipart file
        @Override
        public String getFilename() {
            return "invalid-cache.dxvk-cache";
        }
    };

    public static final ClassPathResource PROFILE_PIC_1_RESOURCE = new ClassPathResource("sample/profile_pic_1.jpg");
    public static final ClassPathResource PROFILE_PIC_2_RESOURCE = new ClassPathResource("sample/profile_pic_2.jpg");
}
