package com.av.bookservice.dto;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BookPricesResponseDto {
    private String bookCode;
    private BigDecimal price;
}
