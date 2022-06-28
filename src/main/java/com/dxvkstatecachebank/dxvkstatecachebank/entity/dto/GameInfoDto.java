package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameInfoDto {
    private Long id;
    private String name;
    @JsonInclude(Include.NON_NULL)
    private String incrementalCacheFileLink;
    @JsonInclude(Include.NON_NULL)
    private LocalDateTime incrementalCacheLastModified;
    private String cacheFilesLink;
    private Long steamId;
}
