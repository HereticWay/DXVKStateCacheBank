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
public class CacheFileInfoDto {
    private Long id;
    private LocalDateTime uploadDateTime;
    //private User uploader;
    //private Game game;
    //private Blob data;
    private String uploaderLink;
    private String gameLink;
    private String dataLink;
}
