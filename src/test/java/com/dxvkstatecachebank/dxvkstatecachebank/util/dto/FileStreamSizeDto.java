package com.dxvkstatecachebank.dxvkstatecachebank.util.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileStreamSizeDto {
    private HttpStatus responseStatus;
    private Long contentLengthHeader;
    private Long realFileLength;
}
