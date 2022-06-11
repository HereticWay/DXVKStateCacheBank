package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.GameMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameService gameService;
    @Autowired
    private GameMapper gameMapper;

    @GetMapping
    public List<GameInfoDto> listAllGames() {
        return gameService.findAll().stream()
                .map(game -> gameMapper.toDto(game))
                .toList();
    }

    @GetMapping("/{gameId}")
    public GameInfoDto findGameById(@PathVariable("gameId") Long gameId) {
        return gameMapper.toDto(gameService.findById(gameId));
    }

    @PostMapping
    public GameInfoDto createGame(@RequestBody GameCreateDto gameCreateDto) {
        Game gameCreated = gameService.save(gameMapper.toGame(gameCreateDto));
        return gameMapper.toDto(gameCreated);
    }

    @DeleteMapping("/{gameId}")
    public void deleteGame(@PathVariable("gameId") Long gameId) {
        gameService.deleteById(gameId);
    }
}
