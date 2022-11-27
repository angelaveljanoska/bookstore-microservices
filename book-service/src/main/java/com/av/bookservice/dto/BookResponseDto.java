package com.av.bookservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseDto {
    private Long id;
    private String name;
    private String author;
    private String genre;
    private String description;
    private BigDecimal price;
}
