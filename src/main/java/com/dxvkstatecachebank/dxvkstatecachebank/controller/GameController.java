package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameCreateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.GameUpdateDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.GameMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import com.dxvkstatecachebank.dxvkstatecachebank.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;

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
    @Operation(summary = "Get all Games")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GameInfoDto[].class))
            )
    })
    public List<GameInfoDto> listAllGames() {
        return gameService.findAll().stream()
                .map(gameMapper::toDto)
                .toList();
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Get game by id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GameInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content = @Content
            )
    })
    public ResponseEntity<GameInfoDto> findGameById(@PathVariable("gameId") Long gameId) {
        if (!gameService.existsById(gameId)) {
            log.error("Game id: {} could not be found", gameId);
            return ResponseEntity.notFound().build();
        }

        Game game = gameService.findById(gameId)
                .orElseThrow();
        return ResponseEntity.ok(gameMapper.toDto(game));
    }

    @Transactional
    @GetMapping("/{gameId}/incremental_cache_file")
    @Operation(summary = "Get game's incremental cache file by game id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content = @Content
            )
    })
    public void getLatestIncrementalCacheFile(@PathVariable("gameId") Long gameId, HttpServletResponse response) {
        if (!gameService.existsById(gameId)) {
            log.error("Game id: {} could not be found", gameId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Game game = gameService.findById(gameId)
                .orElseThrow();
        Blob cacheFileBlob = game.getIncrementalCacheFile();
        try {
            if(cacheFileBlob == null || cacheFileBlob.length() == 0) {
                log.error("There's no incremental cache for the following game id {}", gameId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cacheFileName + "\"");
            response.setContentLengthLong(cacheFileBlob.length());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.LAST_MODIFIED, game.getIncrementalCacheLastModified().toInstant(ZoneOffset.UTC).toString());
            IOUtils.copyLarge(cacheFileBlob.getBinaryStream(), response.getOutputStream());
        } catch (SQLException | IOException e) {
            log.error("Some unexpected error occurred!");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{gameId}/cache_files")
    @Operation(summary = "Get contributed cache files for game by game id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CacheFileInfoDto[].class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content = @Content
            )
    })
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
    @Operation(summary = "Create new Game")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GameInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "The dto is not valid",
                    content = @Content
            )
    })
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
    @Operation(summary = "Update Game")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GameInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "The dto is not valid",
                    content = @Content
            )
    })
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
    @Operation(summary = "Delete game by its id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteGame(@PathVariable("gameId") Long gameId) {
        if(!gameService.existsById(gameId)) {
            return ResponseEntity.notFound().build();
        }

        gameService.deleteById(gameId);
        return ResponseEntity.ok().build();
    }
}
