package com.dxvkcachebank.dxvkcachebank.service;

import com.dxvkcachebank.dxvkcachebank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
