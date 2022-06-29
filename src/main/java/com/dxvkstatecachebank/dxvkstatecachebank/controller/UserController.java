package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserUpdateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.UserMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;
    private final CacheFileService cacheFileService;
    private final CacheFileMapper cacheFileMapper;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, CacheFileService cacheFileService, CacheFileMapper cacheFileMapper, UserMapper userMapper) {
        this.userService = userService;
        this.cacheFileService = cacheFileService;
        this.cacheFileMapper = cacheFileMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserInfoDto> listAllUsers() {
        return userService.findAll().stream()
                .map(user -> userMapper.toDto(user))
                .toList();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> findUserById(@PathVariable("userId") Long userId) {
        Optional<User> userFound = userService.findById(userId);
        if (userFound.isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }

        User user = userFound.get();
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/{userId}/profile_picture")
    public ResponseEntity<Resource> getProfilePictureByUserId(@PathVariable("userId") Long userId) throws SQLException {
        Optional<User> userFound = userService.findById(userId);
        if (userFound.isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }

        User user = userFound.get();
        Blob profilePictureBlob = user.getProfilePicture();

        InputStreamResource inputStreamResource = new InputStreamResource(profilePictureBlob.getBinaryStream());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(profilePictureBlob.length())
                .body(inputStreamResource);
    }

    @GetMapping("/{userId}/cache_files")
    public List<CacheFileInfoDto> findCacheFileByUserId(@PathVariable("userId") Long userId) {
        return cacheFileService.findAllByUploaderId(userId).stream()
                .map(cacheFileMapper::toDto)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserInfoDto> createUser(@RequestPart("file") MultipartFile multipartFile, @Valid @RequestPart("userCreateDto") UserCreateDto userCreateDto, BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().toString());
            // TODO: Return more descriptive error messages here
            return ResponseEntity.unprocessableEntity().build();
        }

        User userCreated = userService.save(userMapper.toUser(userCreateDto, multipartFile));
        return ResponseEntity.ok(userMapper.toDto(userCreated));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserInfoDto> updateUser(@PathVariable("userId") Long userId, @Valid @RequestBody UserUpdateDto userUpdateDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            log.error("Validation error!");
            bindingResult.getAllErrors().forEach(err -> log.error(err.getDefaultMessage()));
            return ResponseEntity.badRequest().build();
        }
        if(!userService.existsById(userId)) {
            log.error("User id doesn't exists: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(userId)
                .orElseThrow();
        user.setName(userUpdateDto.getName());
        user.setPassword(userUpdateDto.getPassword());
        user.setEmail(userUpdateDto.getEmail());
        return ResponseEntity.ok(userMapper.toDto(userService.save(user)));
    }

    @PutMapping(value = "/{userId}/profile_picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserInfoDto> updateUserProfilePicture(@PathVariable("userId") Long userId, @RequestPart("file") MultipartFile multipartFile) throws IOException {
        if(!userService.existsById(userId)) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(userId)
                .orElseThrow();
        user.setProfilePicture(BlobProxy.generateProxy(multipartFile.getInputStream(), multipartFile.getSize()));
        return ResponseEntity.ok(userMapper.toDto(userService.save(user)));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteById(userId);
    }
}
