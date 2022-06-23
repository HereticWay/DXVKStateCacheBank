package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.GameMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/game")
@Slf4j
public class GameController {
    @Autowired
    private GameService gameService;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private CacheFileService cacheFileService;
    @Autowired
    private CacheFileMapper cacheFileMapper;

    @GetMapping
    public List<GameInfoDto> listAllGames() {
        return gameService.findAll().stream()
                .map(game -> gameMapper.toDto(game))
                .toList();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameInfoDto> findGameById(@PathVariable("gameId") Long gameId) {
        Optional<Game> gameFound = gameService.findById(gameId);
        if (gameFound.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(gameMapper.toDto(gameFound.get()));
    }

    @GetMapping("/{gameId}/incremental_cache_file")
    public ResponseEntity<Resource> getLatestIncrementalCacheFile(@PathVariable("gameId") Long gameId) throws SQLException {
        Optional<Game> gameFound = gameService.findById(gameId);
        if (gameFound.isEmpty()) {
            // TODO: Return more descriptive error messages here
            return ResponseEntity.notFound().build();
        }

        Game game = gameFound.get();
        Blob cacheFileBlob = game.getIncrementalCacheFile();
        if (cacheFileBlob == null) {
            return ResponseEntity.notFound().build();
        }

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

    @GetMapping("/{gameId}/cache_files")
    public List<CacheFileInfoDto> listCacheFilesForGameId(@PathVariable("gameId") Long gameId) {
        return cacheFileService.findAllByGameId(gameId).stream()
                .map(cacheFile -> cacheFileMapper.toDto(cacheFile))
                .toList();
    }

    @PostMapping
    public ResponseEntity<GameInfoDto> createGame(@Valid @RequestBody GameCreateDto gameCreateDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().toString());

            // TODO: Return more descriptive error messages here
            return ResponseEntity.unprocessableEntity().build();
        }

        Game gameCreated = gameService.save(gameMapper.toGame(gameCreateDto));
        return ResponseEntity.ok(gameMapper.toDto(gameCreated));
    }

    @DeleteMapping("/{gameId}")
    public void deleteGame(@PathVariable("gameId") Long gameId) {
        gameService.deleteById(gameId);
    }
}
