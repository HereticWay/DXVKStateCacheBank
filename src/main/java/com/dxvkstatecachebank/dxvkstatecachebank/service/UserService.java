package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheFileService cacheFileService;

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        User user = findById(id).orElseThrow();
        cacheFileService.disownAllFromUploaderId(user.getId());
        userRepository.deleteById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    public void flush() {
        userRepository.flush();
    }
}
