package com.av.bookservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
public class StockRequestDto {
    private String bookCode;
    private BigDecimal price;
    private int quantity;
}
