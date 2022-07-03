package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.NoNewCacheEntryException;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.UnsuccessfulCacheMergeException;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jdk.jfr.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/cache_file")
@Slf4j
public class CacheFileController {
    private final CacheFileService cacheFileService;

    private final CacheFileMapper cacheFileMapper;

    @Autowired
    public CacheFileController(CacheFileService cacheFileService, CacheFileMapper cacheFileMapper) {
        this.cacheFileService = cacheFileService;
        this.cacheFileMapper = cacheFileMapper;
    }

    @GetMapping("/{cacheFileId}")
    @Operation(summary = "Find cache file by its id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CacheFileInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cache file not found",
                    content = @Content
            )
    })
    public ResponseEntity<CacheFileInfoDto> findCacheFileById(@PathVariable("cacheFileId") Long cacheFileId) {
        if (!cacheFileService.existsById(cacheFileId)) {
            log.error("Cache file id: {} could not be found", cacheFileId);
            return ResponseEntity.notFound().build();
        }

        CacheFile cacheFile = cacheFileService.findById(cacheFileId)
                .orElseThrow();

        return ResponseEntity.ok(cacheFileMapper.toDto(cacheFile));
    }

    @Transactional
    @GetMapping("/{cacheFileId}/data")
    @Operation(summary = "Get cache file data by cache file id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cache file not found",
                    content = @Content
            )
    })
    public void getCacheFileData(@PathVariable("cacheFileId") Long cacheFileId, HttpServletResponse response) {
        if (!cacheFileService.existsById(cacheFileId)) {
            log.error("Cache file id: {} could not be found", cacheFileId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        CacheFile cacheFile = cacheFileService.findById(cacheFileId)
                .orElseThrow();

        Blob cacheFileBlob = cacheFile.getData();
        Game game = cacheFile.getGame();
        String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cacheFileName + "\"");
            response.setContentLengthLong(cacheFileBlob.length());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.LAST_MODIFIED, cacheFile.getUploadDateTime().toInstant(ZoneOffset.UTC).toString());
            IOUtils.copyLarge(cacheFileBlob.getBinaryStream(), response.getOutputStream());
        } catch (SQLException | IOException e) {
            log.error("Some unexpected error occurred!");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload cache file")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CacheFileInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "The dto is not valid or the cache file is not valid or the cache file contains no new cache entries",
                    content = @Content
            )
    })
    public ResponseEntity<CacheFileInfoDto> uploadCacheFile(@RequestPart("file") MultipartFile multipartFile, @Valid @RequestPart("cacheFileUploadDto") CacheFileUploadDto cacheFileUploadDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            log.error("Validation error:");
            bindingResult.getAllErrors().forEach(err -> log.error(err.getDefaultMessage()));
            return ResponseEntity.unprocessableEntity().build();
        }

        try {
            InputStream fileInputStream = multipartFile.getInputStream();
            CacheFile savedCacheFile = cacheFileService.mergeCacheFileToIncrementalCacheAndSave(cacheFileUploadDto, fileInputStream, multipartFile.getSize());

            CacheFileInfoDto cacheFileInfoDto = cacheFileMapper.toDto(savedCacheFile);
            return ResponseEntity.ok(cacheFileInfoDto);
        } catch (UnsuccessfulCacheMergeException e) {
            log.error("Cache merge failed! Maybe posted an invalid cache file was posted?");
            return ResponseEntity.unprocessableEntity()
                    .build();
        } catch (NoNewCacheEntryException e) {
            log.error("Cache merge is unnecessary because it contains no new cache entries. Dropping it...");
            return ResponseEntity.unprocessableEntity()
                    .build();
        } catch (IOException | SQLException e) {
            log.error("Some unexpected error occurred!");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{cacheFileId}")
    @Operation(summary = "Delete cache file by its id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CacheFileInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cache file not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteCacheFile(@PathVariable("cacheFileId") Long cacheFileId) {
        if(!cacheFileService.existsById(cacheFileId)) {
            log.error("Cache file id: {} could not be found", cacheFileId);
            return ResponseEntity.notFound().build();
        }

        cacheFileService.deleteById(cacheFileId);
        return ResponseEntity.ok().build();
    }
}
