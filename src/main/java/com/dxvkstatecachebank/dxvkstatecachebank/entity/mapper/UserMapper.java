package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class UserMapper {
    public UserInfoDto toDto(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePictureLink("/user/%d/profile_picture".formatted(user.getId()))
                .contributionsLink("/cachefile/user/%d".formatted(user.getId()))
                .build();
    }

    public User toUser(UserCreateDto userCreateDto, MultipartFile profilePicture) throws IOException {
        try (
                var profilePictureInputStream = new BufferedInputStream(profilePicture.getInputStream())
       ) {
            return User.builder()
                    .email(userCreateDto.getEmail())
                    .name(userCreateDto.getName())
                    .password(userCreateDto.getPassword())
                    .profilePicture(BlobProxy.generateProxy(profilePictureInputStream, profilePicture.getSize()))
                    .build();
        }
    }
}
