package com.dxvkstatecachebank.dxvkstatecachebank.service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class MergeResultDto {
    private final boolean success;
    private Integer sumOfEntriesMerged;
}
