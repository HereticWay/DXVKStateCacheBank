package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import org.springframework.stereotype.Component;

@Component
public class UserInfoMapper {
    public UserInfoDto toDto(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePictureLink("/user/%d/profile_picture".formatted(user.getId()))
                .contributionsLink("/cachefile/user/%d".formatted(user.getId()))
                .build();
    }
}
