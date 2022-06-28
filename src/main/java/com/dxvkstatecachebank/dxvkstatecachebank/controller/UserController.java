package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation.ExistingUserId;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.UserMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CacheFileService cacheFileService;

    @Autowired
    private CacheFileMapper cacheFileMapper;

    @Autowired
    private UserMapper userMapper;

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

    @PutMapping("/{userId}/name")
    public ResponseEntity<UserInfoDto> renameUser(@Valid @PathVariable("userId") @ExistingUserId Long userId, @Valid @RequestBody @NotEmpty String newName, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().toString());
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(userId)
                .orElseThrow();
        user.setName(newName);
        return ResponseEntity.ok(userMapper.toDto(userService.save(user)));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<UserInfoDto> changePasswordOfUser(@Valid @PathVariable("userId") @ExistingUserId Long userId, @Valid @RequestBody @NotEmpty String newPassword, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().toString());
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(userId)
                .orElseThrow();
        user.setPassword(newPassword);
        return ResponseEntity.ok(userMapper.toDto(userService.save(user)));
    }

    @PutMapping("/{userId}/email")
    public ResponseEntity<UserInfoDto> changeEmailOfUser(@Valid @PathVariable("userId") @ExistingUserId Long userId, @Valid @RequestBody @NotNull @Email String newEmail, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().toString());
            return ResponseEntity.badRequest().build();
        }

        User user = userService.findById(userId)
                .orElseThrow();
        user.setEmail(newEmail);
        return ResponseEntity.ok(userMapper.toDto(userService.save(user)));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteById(userId);
    }
}
