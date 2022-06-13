package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileUploadDto;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class CacheFileMapper {
    public CacheFileInfoDto toDto(CacheFile cacheFile) {
        Long uploaderId = cacheFile.getUploader().getId();
        Long gameId = cacheFile.getGame().getId();

        return CacheFileInfoDto.builder()
                .id(cacheFile.getId())
                .uploadDateTime(cacheFile.getUploadDateTime())
                .uploaderLink("/user/%d".formatted(uploaderId))
                .gameLink("/game/%d".formatted(gameId))
                .dataLink("/cachefile/%d/data")
                .build();
    }

    public CacheFile toCacheFile(Long uploaderId, Long gameId, MultipartFile multipartFile) throws IOException {

        return CacheFile.builder()
                .uploader(User.builder().id(uploaderId).build())
                .uploadDateTime(LocalDateTime.now())
                .game(Game.builder().id(gameId).build())
                .data(BlobProxy.generateProxy(multipartFile.getInputStream(), multipartFile.getSize()))
                .build();
    }
}
