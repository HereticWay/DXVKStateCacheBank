package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncrementalCacheFileInfoDto {
    private Long id;
    private LocalDateTime lastUpdateTime;
    // private Game game;
    // private Blob data;
    private String gameLink; // TODO: Write a mapper that makes a proper link to get the game
    private String dataLink; // TODO: Write a mapper that makes a proper link to get the latest cache file data
}
