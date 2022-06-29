package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    @Lazy
    private CacheFileService cacheFileService;

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    public Optional<Game> findById(Long gameId) {
        return gameRepository.findById(gameId);
    }

    public boolean existsById(Long gameId) {
        return gameRepository.existsById(gameId);
    }

    public void deleteById(Long gameId) {
        cacheFileService.deleteAllByGameId(gameId);
        gameRepository.deleteById(gameId);
    }

    public void flush() {
        gameRepository.flush();
    }

    public void deleteAll() {
        gameRepository.deleteAll();
    }
}
