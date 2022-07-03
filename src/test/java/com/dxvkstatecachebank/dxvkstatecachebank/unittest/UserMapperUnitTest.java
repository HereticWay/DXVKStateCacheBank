package com.dxvkstatecachebank.dxvkstatecachebank.unittest;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.dxvkstatecachebank.dxvkstatecachebank.data.TestData.SAMPLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperUnitTest {
    private final UserMapper userMapper = new UserMapper();

    @Test
    void testToDto() {
        UserInfoDto userInfoDto = userMapper.toDto(SAMPLE_USER);

        assertThat(userInfoDto.getId()).isEqualTo(SAMPLE_USER.getId());
        assertThat(userInfoDto.getName()).isEqualTo(SAMPLE_USER.getName());
        assertThat(userInfoDto.getEmail()).isEqualTo(SAMPLE_USER.getEmail());
        assertThat(userInfoDto.getProfilePictureLink()).contains(String.valueOf(SAMPLE_USER.getId()));
        assertThat(userInfoDto.getCacheFilesLink()).contains(String.valueOf(SAMPLE_USER.getId()));
    }

    @Test
    void testToUser() throws IOException {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("Samantha")
                .email("samantha.rodriguez@codecool.com")
                .password("pass")
                .build();

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getInputStream()).thenReturn(InputStream.nullInputStream());
        User user = userMapper.toUser(userCreateDto, multipartFile);
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(userCreateDto.getName());
        assertThat(user.getEmail()).isEqualTo(userCreateDto.getEmail());
        assertThat(user.getPassword()).isEqualTo(userCreateDto.getPassword());
        assertThat(user.getProfilePicture()).isNotNull();
    }
}
