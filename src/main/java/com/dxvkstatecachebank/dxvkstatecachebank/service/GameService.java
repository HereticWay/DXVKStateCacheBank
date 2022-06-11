package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    public Game findById(Long gameId) {
        return gameRepository.findById(gameId)
                .get();  // It's okay now to throw an exception here
    }

    public void deleteById(Long gameId) {
        gameRepository.deleteById(gameId);
    }
}
