package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.UserInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.UserMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public UserInfoDto createUser(@RequestBody UserCreateDto userCreateDto) {
        User userCreated = userService.save(userMapper.toUser(userCreateDto));
        return userMapper.toDto(userCreated);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteById(userId);
    }
}
