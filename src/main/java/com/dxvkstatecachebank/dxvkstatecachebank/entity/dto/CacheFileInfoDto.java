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
public class CacheFileInfoDto {
    private Long id;
    private LocalDateTime uploadDateTime;
    @JsonInclude(Include.NON_NULL)
    private String uploaderLink;
    private String gameLink;
    private String dataLink;
}
