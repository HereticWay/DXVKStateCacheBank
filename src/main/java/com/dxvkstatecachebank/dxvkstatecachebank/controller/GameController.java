package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameUpdateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation.ExistingGameId;
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

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/game")
@Slf4j
public class GameController {
    private final GameService gameService;
    private final GameMapper gameMapper;
    private final CacheFileService cacheFileService;
    private final CacheFileMapper cacheFileMapper;

    @Autowired
    public GameController(GameService gameService, GameMapper gameMapper, CacheFileService cacheFileService, CacheFileMapper cacheFileMapper) {
        this.gameService = gameService;
        this.gameMapper = gameMapper;
        this.cacheFileService = cacheFileService;
        this.cacheFileMapper = cacheFileMapper;
    }

    @GetMapping
    public List<GameInfoDto> listAllGames() {
        return gameService.findAll().stream()
                .map(gameMapper::toDto)
                .toList();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameInfoDto> findGameById(@PathVariable("gameId") Long gameId) {
        if (!gameService.existsById(gameId)) {
            log.error("Game id: {} could not be found", gameId);
            return ResponseEntity.notFound().build();
        }

        Game game = gameService.findById(gameId)
                .orElseThrow();
        return ResponseEntity.ok(gameMapper.toDto(game));
    }

    @GetMapping("/{gameId}/incremental_cache_file")
    public ResponseEntity<Resource> getLatestIncrementalCacheFile(@PathVariable("gameId") Long gameId) {
        if (!gameService.existsById(gameId)) {
            log.error("Game id: {} could not be found", gameId);
            return ResponseEntity.notFound().build();
        }

        Game game = gameService.findById(gameId)
                .orElseThrow();
        Blob cacheFileBlob = game.getIncrementalCacheFile();
        InputStreamResource cacheFileStreamResource;
        long cacheFileLength;
        try {
            cacheFileStreamResource = new InputStreamResource(cacheFileBlob.getBinaryStream());
            cacheFileLength = cacheFileBlob.length();
        } catch (SQLException e) {
            log.error("Some unexpected error occurred!");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        if (cacheFileLength == 0) {
            return ResponseEntity.notFound().build();
        }

        String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(cacheFileName)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(cacheFileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .lastModified(game.getIncrementalCacheLastModified().toInstant(ZoneOffset.UTC))
                .body(cacheFileStreamResource);
    }

    @GetMapping("/{gameId}/cache_files")
    public ResponseEntity<List<CacheFileInfoDto>> listCacheFilesForGameId(@PathVariable("gameId") Long gameId) {
        if(!gameService.existsById(gameId)) {
            log.error("Game id: {} could not be found", gameId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                cacheFileService.findAllByGameId(gameId).stream()
                        .map(cacheFileMapper::toDto)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<GameInfoDto> createGame(@Valid @RequestBody GameCreateDto gameCreateDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            log.error("Validation error:");
            bindingResult.getAllErrors().forEach(err -> log.error(err.getDefaultMessage()));
            return ResponseEntity.unprocessableEntity().build();
        }

        Game gameCreated = gameService.save(gameMapper.toGame(gameCreateDto));
        return ResponseEntity.ok(gameMapper.toDto(gameCreated));
    }

    @PutMapping("/{gameId}")
    public ResponseEntity<GameInfoDto> updateGame(@PathVariable("gameId") Long gameId, @Valid @RequestBody GameUpdateDto gameUpdateDto, BindingResult bindingResult) {
        if(!gameService.existsById(gameId)) {
            log.error("Game id: {} could not be found", gameId);
            return ResponseEntity.notFound().build();
        }

        if(bindingResult.hasErrors()) {
            log.error("Validation error:");
            bindingResult.getAllErrors().forEach(err -> log.error(err.getDefaultMessage()));
            return ResponseEntity.unprocessableEntity().build();
        }

        Game game = gameService.findById(gameId)
                .orElseThrow();
        game.setName(gameUpdateDto.getName());
        game.setCacheFileName(gameUpdateDto.getCacheFileName());
        game.setSteamId(gameUpdateDto.getSteamId());
        return ResponseEntity.ok(gameMapper.toDto(gameService.save(game)));
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable("gameId") Long gameId) {
        if(!gameService.existsById(gameId)) {
            return ResponseEntity.notFound().build();
        }

        gameService.deleteById(gameId);
        return ResponseEntity.ok().build();
    }
}
