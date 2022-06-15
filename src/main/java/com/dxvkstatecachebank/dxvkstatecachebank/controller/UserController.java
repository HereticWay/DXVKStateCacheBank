package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.UserMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public List<UserInfoDto> listAllUsers() {
        return userService.findAll().stream()
                .map(user -> userMapper.toDto(user))
                .toList();
    }

    @GetMapping("/{userId}")
    public UserInfoDto findUserById(@PathVariable("userId") Long userId) {
        User userFound = userService.findById(userId);
        return userMapper.toDto(userFound);
    }

    @GetMapping("/{userId}/profile_picture")
    public ResponseEntity<Resource> getProfilePictureByUserId(@PathVariable("userId") Long userId) throws SQLException {
        User userFound = userService.findById(userId);
        Blob profilePictureBlob = userFound.getProfilePicture();

        InputStreamResource inputStreamResource = new InputStreamResource(profilePictureBlob.getBinaryStream());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(profilePictureBlob.length())
                .body(inputStreamResource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserInfoDto createUser(@RequestPart("userCreateDto") UserCreateDto userCreateDto, @RequestPart("file") MultipartFile multipartFile) throws IOException {
        User userCreated = userService.save(userMapper.toUser(userCreateDto, multipartFile));
        return userMapper.toDto(userCreated);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteById(userId);
    }
}
