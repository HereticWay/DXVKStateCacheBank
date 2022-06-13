package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.GameMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Blob;
import java.sql.SQLException;
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

    @GetMapping("/{gameId}/incremental_cache_file")
    public ResponseEntity<Resource> getLatestIncrementalCacheFile(@PathVariable("gameId") Long gameId) throws SQLException {
        Game game = gameService.findById(gameId);
        Blob cacheFileBlob = game.getIncrementalCache();
        String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(cacheFileName)
                        .build()
        );

        InputStreamResource inputStreamResource = new InputStreamResource(cacheFileBlob.getBinaryStream());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(cacheFileBlob.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(inputStreamResource);
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
