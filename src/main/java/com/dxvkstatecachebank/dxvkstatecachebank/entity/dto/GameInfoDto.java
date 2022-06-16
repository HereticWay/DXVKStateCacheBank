package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameInfoDto {
    private Long id;
    private String name;
    private String incrementalCacheFileLink;
    private String cacheFilesLink;
    private Long steamId;
}
