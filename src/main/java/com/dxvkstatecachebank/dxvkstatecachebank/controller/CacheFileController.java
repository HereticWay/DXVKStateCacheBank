package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper.CacheFileMapper;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.NoNewCacheEntryException;
import com.dxvkstatecachebank.dxvkstatecachebank.exceptions.UnsuccessfulCacheMergeException;
import com.dxvkstatecachebank.dxvkstatecachebank.service.CacheFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
import java.sql.Blob;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.Optional;

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
    public ResponseEntity<CacheFileInfoDto> findCacheFileById(@PathVariable("cacheFileId") Long cacheFileId) {
        Optional<CacheFile> cacheFileFound = cacheFileService.findById(cacheFileId);
        if (cacheFileFound.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CacheFile cacheFile = cacheFileFound.get();
        return ResponseEntity.ok(cacheFileMapper.toDto(cacheFile));
    }

    @Transactional
    @GetMapping("/{cacheFileId}/data")
    public void getCacheFileData(@PathVariable("cacheFileId") Long cacheFileId, HttpServletResponse response) throws SQLException, IOException {
        Optional<CacheFile> cacheFileFound = cacheFileService.findById(cacheFileId);
        if (cacheFileFound.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        CacheFile cacheFile = cacheFileFound.get();
        Blob cacheFileBlob = cacheFile.getData();
        Game game = cacheFile.getGame();
        String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(cacheFileName)
                        .build()
        );

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cacheFileName + "\"");
        response.setContentLengthLong(cacheFileBlob.length());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.LAST_MODIFIED, cacheFile.getUploadDateTime().toInstant(ZoneOffset.UTC).toString());
        IOUtils.copyLarge(cacheFileBlob.getBinaryStream(), response.getOutputStream());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CacheFileInfoDto> uploadCacheFile(@RequestPart("file") MultipartFile multipartFile, @Valid @RequestPart("cacheFileUploadDto") CacheFileUploadDto cacheFileUploadDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().toString());
            // TODO: Return more descriptive error messages here
            return ResponseEntity.unprocessableEntity()
                    .build();
        }

        try {
            InputStream fileInputStream = multipartFile.getInputStream();
            CacheFile savedCacheFile = cacheFileService.mergeCacheFileToIncrementalCacheAndSave(cacheFileUploadDto, fileInputStream, multipartFile.getSize());

            CacheFileInfoDto cacheFileInfoDto = cacheFileMapper.toDto(savedCacheFile);
            return ResponseEntity.ok(cacheFileInfoDto);
        } catch (UnsuccessfulCacheMergeException | NoNewCacheEntryException e) {
            // TODO: Return more descriptive error messages here
            return ResponseEntity.unprocessableEntity()
                    .build();
        } catch (IOException | SQLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{cacheFileId}")
    public void deleteCacheFile(@PathVariable("cacheFileId") Long cacheFileId) {
        cacheFileService.deleteById(cacheFileId);
    }
}
