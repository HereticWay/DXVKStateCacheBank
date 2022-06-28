package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameUpdateDto {
    @NotEmpty
    private String name;

    @NotEmpty
    private String cacheFileName;

    @Positive
    private Long steamId;
}
