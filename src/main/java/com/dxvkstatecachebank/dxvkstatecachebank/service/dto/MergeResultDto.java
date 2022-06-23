package com.dxvkstatecachebank.dxvkstatecachebank.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class MergeResultDto {
    private final boolean success;
    private Integer sumOfEntriesMerged;
}
