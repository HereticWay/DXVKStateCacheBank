package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation.ExistingGameId;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.validator.annotation.ExistingUserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CacheFileUploadDto {
    @NotNull
    @ExistingUserId
    private Long uploaderId;

    @NotNull
    @ExistingGameId
    private Long gameId;
}
