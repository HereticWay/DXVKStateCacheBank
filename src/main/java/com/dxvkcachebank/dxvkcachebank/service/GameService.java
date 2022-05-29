package com.dxvkcachebank.dxvkcachebank.service;

import com.dxvkcachebank.dxvkcachebank.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;
}
